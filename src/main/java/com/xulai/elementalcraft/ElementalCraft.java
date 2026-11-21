package com.xulai.elementalcraft;

import com.mojang.logging.LogUtils;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.xulai.elementalcraft.client.ModParticles;
import com.xulai.elementalcraft.command.ModCommands;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.config.ElementalISSIntegrationConfig;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.config.ElementalVisualConfig;
import com.xulai.elementalcraft.config.ForcedItemConfig;
import com.xulai.elementalcraft.enchantment.ModEnchantments;
import com.xulai.elementalcraft.event.PotionStackHandler;
import com.xulai.elementalcraft.event.TooltipEvents;
import com.xulai.elementalcraft.potion.ModMobEffects;
import com.xulai.elementalcraft.sound.ModSounds;
import com.xulai.elementalcraft.util.ConfigReloadRegistry;
import com.xulai.elementalcraft.util.CustomBiomeBias;
import com.xulai.elementalcraft.network.FireCounterLockPacket;
import com.xulai.elementalcraft.util.ForcedAttributeHelper;
import com.xulai.elementalcraft.util.ForcedItemHelper;
import com.xulai.elementalcraft.util.ModCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Mod(ElementalCraft.MODID)
public class ElementalCraft {
    public static final String MODID = "elementalcraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            ElementalCraft::channelVersion,
            NetworkRegistry.acceptMissingOr((java.util.function.Predicate<String>) ElementalCraft::sameChannelVersion),
            NetworkRegistry.acceptMissingOr((java.util.function.Predicate<String>) ElementalCraft::sameChannelVersion));

    public ElementalCraft() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        migrateConfigFiles();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ElementalConfig.SPEC, ConfigReloadRegistry.COMMON);
        ForcedItemConfig.register(ConfigReloadRegistry.FORCED_ITEMS);
        ElementalFireNatureReactionsConfig.register(ConfigReloadRegistry.FIRE_NATURE);
        ElementalVisualConfig.register(ConfigReloadRegistry.VISUALS);
        ElementalThunderFrostReactionsConfig.register(ConfigReloadRegistry.THUNDER_FROST);

        if (ModCompat.iss()) {
            ElementalISSIntegrationConfig.register(ConfigReloadRegistry.ISS_INTEGRATION);
        }

        registerReloadActions();

        ModEnchantments.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModSounds.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigReload);
        modEventBus.addListener(this::onConfigLoad);

        CHANNEL.registerMessage(0, FireCounterLockPacket.class,
                FireCounterLockPacket::encode, FireCounterLockPacket::decode,
                FireCounterLockPacket::handle);

        MinecraftForge.EVENT_BUS.register(TooltipEvents.class);
        MinecraftForge.EVENT_BUS.register(ModCommands.class);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    public static String channelVersion() {
        return ModList.get().getModContainerById(MODID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("");
    }

    private static boolean sameChannelVersion(String remoteVersion) {
        String local = channelVersion();
        return local.isEmpty() || remoteVersion.equals(local);
    }

    private void registerReloadActions() {
        ConfigReloadRegistry.register(ElementalConfig.SPEC, ConfigReloadRegistry.COMMON, () -> {
            ElementalConfig.refreshCache();
            CustomBiomeBias.clearCache();
            ForcedAttributeHelper.clearCache();
            PotionStackHandler.applyCurrentValue();
        });
        ConfigReloadRegistry.register(ForcedItemConfig.SPEC, ConfigReloadRegistry.FORCED_ITEMS, ForcedItemHelper::clearCache);
        ConfigReloadRegistry.register(ElementalFireNatureReactionsConfig.SPEC, ConfigReloadRegistry.FIRE_NATURE,
                ElementalFireNatureReactionsConfig::refreshCache);
        ConfigReloadRegistry.register(ElementalVisualConfig.SPEC, ConfigReloadRegistry.VISUALS,
                ElementalVisualConfig::refreshCache);
        ConfigReloadRegistry.register(ElementalThunderFrostReactionsConfig.SPEC, ConfigReloadRegistry.THUNDER_FROST,
                ElementalThunderFrostReactionsConfig::refreshCache);
        if (ModCompat.iss()) {
            ConfigReloadRegistry.register(ElementalISSIntegrationConfig.SPEC, ConfigReloadRegistry.ISS_INTEGRATION,
                    ElementalISSIntegrationConfig::refreshCache);
        }
    }

    private void migrateConfigFiles() {
        try {
            Path configRoot = FMLPaths.CONFIGDIR.get();
            Path oldDir = configRoot.resolve("ElementalCraft");
            Path newDir = configRoot.resolve("Elemental_Reactions");

            if (Files.exists(oldDir)) {
                if (Files.exists(newDir)) {
                    migrateLegacyFilesInside(oldDir, newDir);
                } else {
                    Files.move(oldDir, newDir);
                }
                renameLegacyFiles(newDir);
                return;
            }

            String[] legacyFiles = {
                "elementalcraft-common.toml",
                "elementalcraft-forced-items.toml",
                "elementalcraft-reactions.toml",
                "elementalcraft-fire-nature-reactions.toml",
                "elementalcraft-visuals.toml",
                "elementalcraft-thunderfrost-reactions.toml",
                "elementalcraft-thunder-frost-reactions.toml",
                "elementalcraft-iss-integration.toml"
            };

            if (!Files.exists(newDir)) Files.createDirectories(newDir);
            for (String file : legacyFiles) {
                Path oldPath = configRoot.resolve(file);
                if (!Files.exists(oldPath)) continue;
                Files.move(oldPath, newDir.resolve(file), StandardCopyOption.REPLACE_EXISTING);
            }
            renameLegacyFiles(newDir);

        } catch (Exception e) {
            LOGGER.warn("[ElementalCraft] 迁移旧配置文件失败: {}", e.getMessage());
        }
    }

    private void migrateLegacyFilesInside(Path oldDir, Path newDir) throws java.io.IOException {
        String[] legacyFiles = {
            "elementalcraft-common.toml",
            "elementalcraft-forced-items.toml",
            "elementalcraft-reactions.toml",
            "elementalcraft-fire-nature-reactions.toml",
            "elementalcraft-visuals.toml",
            "elementalcraft-thunderfrost-reactions.toml",
            "elementalcraft-thunder-frost-reactions.toml",
            "elementalcraft-iss-integration.toml"
        };
        for (String file : legacyFiles) {
            Path oldPath = oldDir.resolve(file);
            if (!Files.exists(oldPath)) continue;
            Files.move(oldPath, newDir.resolve(file), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void renameLegacyFiles(Path dir) throws java.io.IOException {
        String[] legacyFiles = {
            "elementalcraft-common.toml",
            "elementalcraft-forced-items.toml",
            "elementalcraft-reactions.toml",
            "elementalcraft-fire-nature-reactions.toml",
            "elementalcraft-visuals.toml",
            "elementalcraft-thunderfrost-reactions.toml",
            "elementalcraft-thunder-frost-reactions.toml",
            "elementalcraft-iss-integration.toml"
        };
        String[] newFiles = {
            "elemental-common.toml",
            "elemental-forced-items.toml",
            "elemental-reactions.toml",
            "elemental-fire-nature-reactions.toml",
            "elemental-visuals.toml",
            "elemental-thunderfrost-reactions.toml",
            "elemental-thunder-frost-reactions.toml",
            "elemental-iss-integration.toml"
        };
        for (int i = 0; i < legacyFiles.length; i++) {
            Path oldPath = dir.resolve(legacyFiles[i]);
            if (!Files.exists(oldPath)) continue;
            Path newPath = dir.resolve(newFiles[i]);
            if (Files.exists(newPath)) {
                Files.delete(oldPath);
            } else {
                Files.move(oldPath, newPath);
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ConfigReloadRegistry.reloadAll();
    }

    public void onConfigLoad(ModConfigEvent.Loading event) {
        String path = ConfigReloadRegistry.pathOf(event.getConfig().getSpec());
        if (path == null) return;
        ConfigReloadRegistry.reloadByPath(path);
    }

    public void onConfigReload(ModConfigEvent.Reloading event) {
        String path = ConfigReloadRegistry.pathOf(event.getConfig().getSpec());
        if (path == null) return;
        ConfigReloadRegistry.reloadByPath(path);
    }

    public static void reloadAllConfigs() {
        for (String path : ConfigReloadRegistry.paths()) {
            ModConfig modConfig = ConfigTracker.INSTANCE.fileMap().get(path);
            if (modConfig == null || modConfig.getConfigData() == null) continue;
            try {
                ((CommentedFileConfig) modConfig.getConfigData()).load();
                modConfig.getSpec().afterReload();
                ModList.get().getModContainerById(MODID).ifPresent(container ->
                        container.dispatchConfigEvent(IConfigEvent.reloading(modConfig)));
            } catch (Exception e) {
                LOGGER.warn("[ElementalCraft] 配置重载失败: {}", path);
            }
        }
        ConfigReloadRegistry.reloadAll();
    }

    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ResourceManagerReloadListener() {
            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                ConfigReloadRegistry.reloadAll();
            }
        });
    }
}
