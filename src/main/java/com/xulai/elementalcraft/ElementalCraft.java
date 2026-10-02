package com.xulai.elementalcraft;

import com.mojang.logging.LogUtils;
import com.xulai.elementalcraft.client.ModParticles;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.config.ElementalVisualConfig;
import com.xulai.elementalcraft.config.ForcedItemConfig;
import com.xulai.elementalcraft.network.FireCounterLockPacket;
import com.xulai.elementalcraft.potion.ModMobEffects;
import com.xulai.elementalcraft.sound.ModSounds;
import com.xulai.elementalcraft.util.CustomBiomeBias;
import com.xulai.elementalcraft.util.ForcedAttributeHelper;
import com.xulai.elementalcraft.util.ForcedItemHelper;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

@Mod(ElementalCraft.MODID)
public class ElementalCraft {
    public static final String MODID = "elementalcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ElementalCraft(IEventBus modEventBus, ModContainer modContainer) {
        migrateConfigFiles();

        modContainer.registerConfig(ModConfig.Type.COMMON, ElementalConfig.SPEC, "Elemental_Reactions/elemental-common.toml");
        ForcedItemConfig.register(modContainer, "Elemental_Reactions/elemental-forced-items.toml");
        ElementalFireNatureReactionsConfig.register(modContainer, "Elemental_Reactions/elemental-fire-nature-reactions.toml");
        ElementalVisualConfig.register(modContainer, "Elemental_Reactions/elemental-visuals.toml");
        ElementalThunderFrostReactionsConfig.register(modContainer, "Elemental_Reactions/elemental-thunder-frost-reactions.toml");

        ModMobEffects.register(modEventBus);
        ModSounds.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigReload);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);

        LOGGER.info("[ElementalCraft] Mod Constructed!");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(FireCounterLockPacket.TYPE, FireCounterLockPacket.STREAM_CODEC, FireCounterLockPacket::handle);
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
                    LOGGER.info("[ElementalCraft] 旧配置目录已重命名为 Elemental_Reactions");
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
                Files.move(oldPath, newDir.resolve(file), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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
            Files.move(oldPath, newDir.resolve(file), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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
                LOGGER.info("[ElementalCraft] 旧配置文件 {} 已删除（新文件已存在）", legacyFiles[i]);
            } else {
                Files.move(oldPath, newPath);
                LOGGER.info("[ElementalCraft] 旧配置文件已重命名为 {}", newFiles[i]);
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ElementalConfig.refreshCache();
        ElementalFireNatureReactionsConfig.refreshCache();
        ElementalVisualConfig.refreshCache();
        ElementalThunderFrostReactionsConfig.refreshCache();
        LOGGER.info("[ElementalCraft] Common Setup: Config cache initialized.");
    }

    public void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ElementalConfig.SPEC) {
            ElementalConfig.refreshCache();
            CustomBiomeBias.clearCache();
            ForcedAttributeHelper.clearCache();
            LOGGER.info("[ElementalCraft] Config Loaded: elemental-common.toml");
        }
        if (event.getConfig().getSpec() == ForcedItemConfig.SPEC) {
            ForcedItemHelper.clearCache();
            LOGGER.info("[ElementalCraft] Config Loaded: elemental-forced-items.toml");
        }
        if (event.getConfig().getSpec() == ElementalFireNatureReactionsConfig.SPEC) {
            ElementalFireNatureReactionsConfig.refreshCache();
            LOGGER.info("[ElementalCraft] Config Loaded: elemental-fire-nature-reactions.toml");
        }
        if (event.getConfig().getSpec() == ElementalVisualConfig.SPEC) {
            ElementalVisualConfig.refreshCache();
        }
        if (event.getConfig().getSpec() == ElementalThunderFrostReactionsConfig.SPEC) {
            ElementalThunderFrostReactionsConfig.refreshCache();
            LOGGER.info("[ElementalCraft] Config Loaded: elemental-thunder-frost-reactions.toml");
        }
    }

    public void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ElementalConfig.SPEC) {
            ElementalConfig.refreshCache();
            CustomBiomeBias.clearCache();
            ForcedAttributeHelper.clearCache();
            LOGGER.info("[ElementalCraft] Config reloaded from file: elemental-common.toml");
        }
        if (event.getConfig().getSpec() == ForcedItemConfig.SPEC) {
            ForcedItemHelper.clearCache();
            LOGGER.info("[ElementalCraft] Config reloaded from file: elemental-forced-items.toml");
        }
        if (event.getConfig().getSpec() == ElementalFireNatureReactionsConfig.SPEC) {
            ElementalFireNatureReactionsConfig.refreshCache();
            LOGGER.info("[ElementalCraft] Config reloaded from file: elemental-fire-nature-reactions.toml");
        }
        if (event.getConfig().getSpec() == ElementalVisualConfig.SPEC) {
            ElementalVisualConfig.refreshCache();
        }
        if (event.getConfig().getSpec() == ElementalThunderFrostReactionsConfig.SPEC) {
            ElementalThunderFrostReactionsConfig.refreshCache();
            LOGGER.info("[ElementalCraft] Config reloaded from file: elemental-thunder-frost-reactions.toml");
        }
    }

    public void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(
                net.minecraft.resources.Identifier.fromNamespaceAndPath(MODID, "config_cache_refresh"),
                new ResourceManagerReloadListener() {
                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        ElementalConfig.refreshCache();
                        ElementalFireNatureReactionsConfig.refreshCache();
                        ElementalVisualConfig.refreshCache();
                        ElementalThunderFrostReactionsConfig.refreshCache();
                        CustomBiomeBias.clearCache();
                        ForcedAttributeHelper.clearCache();
                        ForcedItemHelper.clearCache();
                    }
                });
    }
}
