package com.xulai.elementalcraft.util;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.xulai.elementalcraft.ElementalCraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ElementalCraft.MODID)
public class ConfigAutoSync {

    private static final Map<String, Long> FILE_TIMESTAMPS = new HashMap<>();

    private static int tickCounter = 0;

    private static final int CHECK_INTERVAL = 100;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        for (String path : ConfigReloadRegistry.paths()) {
            checkConfig(path);
        }
    }

    private static void dispatchReload(ModConfig modConfig) {
        net.minecraftforge.fml.ModList.get().getModContainerById(ElementalCraft.MODID)
                .ifPresent(container -> container.dispatchConfigEvent(
                        net.minecraftforge.fml.config.IConfigEvent.reloading(modConfig)));
    }

    private static void checkConfig(String fileName) {
        ModConfig modConfig = ConfigTracker.INSTANCE.fileMap().get(fileName);
        if (modConfig == null || modConfig.getConfigData() == null) return;

        File file = modConfig.getFullPath().toFile();
        if (!file.exists()) return;

        long currentModified = file.lastModified();
        Long lastModified = FILE_TIMESTAMPS.get(fileName);

        if (lastModified == null) {
            FILE_TIMESTAMPS.put(fileName, currentModified);
            ConfigReloadRegistry.reloadByPath(fileName);
            return;
        }

        if (currentModified > lastModified) {
            FILE_TIMESTAMPS.put(fileName, currentModified);

            try {
                ((CommentedFileConfig) modConfig.getConfigData()).load();
                modConfig.getSpec().afterReload();
                dispatchReload(modConfig);
                ConfigReloadRegistry.reloadByPath(fileName);
                ElementalCraft.LOGGER.info("[ElementalCraft] Detected change in {}, caches refreshed automatically.", fileName);
            } catch (Exception e) {
                ElementalCraft.LOGGER.error("[ElementalCraft] Failed to auto-reload config: {}", fileName, e);
            }
        }
    }

    public static void reloadAll() {
        for (String path : ConfigReloadRegistry.paths()) {
            ModConfig modConfig = ConfigTracker.INSTANCE.fileMap().get(path);
            if (modConfig == null || modConfig.getConfigData() == null) continue;
            try {
                ((CommentedFileConfig) modConfig.getConfigData()).load();
                modConfig.getSpec().afterReload();
                dispatchReload(modConfig);
            } catch (Exception e) {
                ElementalCraft.LOGGER.error("[ElementalCraft] Failed to reload config: {}", path, e);
            }
        }
        ConfigReloadRegistry.reloadAll();
        FILE_TIMESTAMPS.clear();
    }
}
