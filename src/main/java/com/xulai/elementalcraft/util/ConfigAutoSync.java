package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.config.ElementalVisualConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class ConfigAutoSync {

    private static final Map<String, Long> FILE_TIMESTAMPS = new ConcurrentHashMap<>();

    private static int tickCounter = 0;

    private static final int CHECK_INTERVAL = 100;

    private static final String COMMON = "Elemental_Reactions/elemental-common.toml";
    private static final String FORCED_ITEMS = "Elemental_Reactions/elemental-forced-items.toml";
    private static final String FIRE_NATURE = "Elemental_Reactions/elemental-fire-nature-reactions.toml";
    private static final String VISUALS = "Elemental_Reactions/elemental-visuals.toml";
    private static final String THUNDER_FROST = "Elemental_Reactions/elemental-thunder-frost-reactions.toml";

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

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elemental-common.toml, caches refreshed automatically.");
        });

        checkConfig(FORCED_ITEMS, () -> {
            ForcedItemHelper.clearCache();

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elemental-forced-items.toml, caches refreshed automatically.");
        });

        checkConfig(FIRE_NATURE, () -> {
            ElementalFireNatureReactionsConfig.refreshCache();

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elemental-fire-nature-reactions.toml, caches refreshed automatically.");
        });

        checkConfig(VISUALS, () -> {
            ElementalVisualConfig.refreshCache();

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elemental-visuals.toml, caches refreshed automatically.");
        });

        checkConfig(THUNDER_FROST, () -> {
            ElementalThunderFrostReactionsConfig.refreshCache();

            ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in elemental-thunder-frost-reactions.toml, caches refreshed automatically.");
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
