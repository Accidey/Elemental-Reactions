package com.xulai.elementalcraft;

import com.mojang.logging.LogUtils;
import com.xulai.elementalcraft.client.ModParticles;
import com.xulai.elementalcraft.command.ModCommands;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.config.ElementalVisualConfig;
import com.xulai.elementalcraft.config.ForcedItemConfig;
import com.xulai.elementalcraft.enchantment.ModEnchantments;
import com.xulai.elementalcraft.event.TooltipEvents;
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

        modContainer.registerConfig(ModConfig.Type.COMMON, ElementalConfig.SPEC, "ElementalCraft/elementalcraft-common.toml");
        ForcedItemConfig.register(modContainer, "ElementalCraft/elementalcraft-forced-items.toml");
        ElementalFireNatureReactionsConfig.register(modContainer, "ElementalCraft/elementalcraft-fire-nature-reactions.toml");
        ElementalVisualConfig.register(modContainer, "ElementalCraft/elementalcraft-visuals.toml");
        ElementalThunderFrostReactionsConfig.register(modContainer, "ElementalCraft/elementalcraft-thunder-frost-reactions.toml");

        ModMobEffects.register(modEventBus);
        ModSounds.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigReload);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);

        LOGGER.info("§a[ElementalCraft] Mod Constructed!");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(FireCounterLockPacket.TYPE, FireCounterLockPacket.STREAM_CODEC, FireCounterLockPacket::handle);
    }

    private void migrateConfigFiles() {
        try {
            Path configRoot = FMLPaths.CONFIGDIR.get();

            String[] filesToDelete = {
                "elementalcraft-common.toml",
                "elementalcraft-forced-items.toml",
                "elementalcraft-reactions.toml",
                "elementalcraft-fire-nature-reactions.toml",
                "elementalcraft-visuals.toml",
                "elementalcraft-thunderfrost-reactions.toml",
                "elementalcraft-thunder-frost-reactions.toml",
                "elementalcraft-iss-integration.toml"
            };

            for (String file : filesToDelete) {
                Path oldPath = configRoot.resolve(file);
                if (Files.exists(oldPath)) {
                    Files.delete(oldPath);
                    LOGGER.info("[ElementalCraft] 删除旧配置文件: {}", file);
                }
            }

        } catch (Exception e) {
            LOGGER.warn("[ElementalCraft] 清理旧配置文件失败: {}", e.getMessage());
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
            LOGGER.info("[ElementalCraft] Config Loaded: elementalcraft-common.toml");
        }
        if (event.getConfig().getSpec() == ElementalFireNatureReactionsConfig.SPEC) {
            ElementalFireNatureReactionsConfig.refreshCache();
            LOGGER.info("[ElementalCraft] Config Loaded: elementalcraft-fire-nature-reactions.toml");
        }
        if (event.getConfig().getSpec() == ElementalVisualConfig.SPEC) {
            ElementalVisualConfig.refreshCache();
        }
        if (event.getConfig().getSpec() == ElementalThunderFrostReactionsConfig.SPEC) {
            ElementalThunderFrostReactionsConfig.refreshCache();
            LOGGER.info("[ElementalCraft] Config Loaded: elementalcraft-thunder-frost-reactions.toml");
        }
    }

    public void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ElementalConfig.SPEC) {
            ElementalConfig.refreshCache();
            CustomBiomeBias.clearCache();
            ForcedAttributeHelper.clearCache();
            LOGGER.info("[ElementalCraft] Config reloaded from file: elementalcraft-common.toml");
        }
        if (event.getConfig().getSpec() == ForcedItemConfig.SPEC) {
            ForcedItemHelper.clearCache();
            LOGGER.info("[ElementalCraft] Config reloaded from file: elementalcraft-forced-items.toml");
        }
        if (event.getConfig().getSpec() == ElementalFireNatureReactionsConfig.SPEC) {
            ElementalFireNatureReactionsConfig.refreshCache();
            LOGGER.info("[ElementalCraft] Config reloaded from file: elementalcraft-fire-nature-reactions.toml");
        }
        if (event.getConfig().getSpec() == ElementalVisualConfig.SPEC) {
            ElementalVisualConfig.refreshCache();
        }
        if (event.getConfig().getSpec() == ElementalThunderFrostReactionsConfig.SPEC) {
            ElementalThunderFrostReactionsConfig.refreshCache();
            LOGGER.info("[ElementalCraft] Config reloaded from file: elementalcraft-thunder-frost-reactions.toml");
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
