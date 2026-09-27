package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.config.ElementalVisualConfig;
import com.xulai.elementalcraft.config.ForcedItemConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class ConfigAutoSync {

    private static final Map<String, Long> FILE_TIMESTAMPS = new HashMap<>();

    private static int tickCounter = 0;

    private static final int CHECK_INTERVAL = 100;

    private static final String COMMON = "ElementalCraft/elementalcraft-common.toml";
    private static final String FORCED_ITEMS = "ElementalCraft/elementalcraft-forced-items.toml";
    private static final String FIRE_NATURE = "ElementalCraft/elementalcraft-fire-nature-reactions.toml";
    private static final String VISUALS = "ElementalCraft/elementalcraft-visuals.toml";
    private static final String THUNDER_FROST = "ElementalCraft/elementalcraft-thunder-frost-reactions.toml";

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        checkConfig(COMMON, () -> {
            ElementalConfig.refreshCache();
            CustomBiomeBias.clearCache();
            ForcedAttributeHelper.clearCache();

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elementalcraft-common.toml, caches refreshed automatically.");
        });

        checkConfig(FORCED_ITEMS, () -> {
            ForcedItemHelper.clearCache();

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elementalcraft-forced-items.toml, caches refreshed automatically.");
        });

        checkConfig(FIRE_NATURE, () -> {
            ElementalFireNatureReactionsConfig.refreshCache();

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elementalcraft-fire-nature-reactions.toml, caches refreshed automatically.");
        });

        checkConfig(VISUALS, () -> {
            ElementalVisualConfig.refreshCache();

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elementalcraft-visuals.toml, caches refreshed automatically.");
        });

        checkConfig(THUNDER_FROST, () -> {
            ElementalThunderFrostReactionsConfig.refreshCache();

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elementalcraft-thunder-frost-reactions.toml, caches refreshed automatically.");
        });

    }

    private static void checkConfig(String fileName, Runnable onReload) {
        File file = FMLPaths.CONFIGDIR.get().resolve(fileName).toFile();
        if (!file.exists()) return;

        long currentModified = file.lastModified();
        Long lastModified = FILE_TIMESTAMPS.get(fileName);

        if (lastModified == null) {
            FILE_TIMESTAMPS.put(fileName, currentModified);

            try {
                if (fileName.equals(COMMON)) ElementalConfig.refreshCache();
                if (fileName.equals(FORCED_ITEMS)) ForcedItemHelper.clearCache();
                if (fileName.equals(FIRE_NATURE)) ElementalFireNatureReactionsConfig.refreshCache();
                if (fileName.equals(VISUALS)) ElementalVisualConfig.refreshCache();
                if (fileName.equals(THUNDER_FROST)) ElementalThunderFrostReactionsConfig.refreshCache();
            } catch (Exception e) {
                ElementalCraft.LOGGER.warn("[ElementalCraft] Config not ready yet: {}", fileName);
            }

            return;
        }

        if (currentModified > lastModified) {
            FILE_TIMESTAMPS.put(fileName, currentModified);

            try {
                ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, FMLPaths.CONFIGDIR.get());
                onReload.run();
            } catch (Exception e) {
                ElementalCraft.LOGGER.error("[ElementalCraft] Failed to auto-reload config: {}", fileName, e);
            }
        }
    }

    public static void reloadAll() {
        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, FMLPaths.CONFIGDIR.get());

        ElementalConfig.refreshCache();
        CustomBiomeBias.clearCache();
        ForcedAttributeHelper.clearCache();
        ForcedItemHelper.clearCache();
        ElementalFireNatureReactionsConfig.refreshCache();
        ElementalVisualConfig.refreshCache();
        ElementalThunderFrostReactionsConfig.refreshCache();
        FILE_TIMESTAMPS.clear();
    }
}
