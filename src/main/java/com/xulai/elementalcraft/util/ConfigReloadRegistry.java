package com.xulai.elementalcraft.util;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigReloadRegistry {

    public static final String COMMON = "ElementalCraft/elementalcraft-common.toml";
    public static final String FORCED_ITEMS = "ElementalCraft/elementalcraft-forced-items.toml";
    public static final String FIRE_NATURE = "ElementalCraft/elementalcraft-fire-nature-reactions.toml";
    public static final String VISUALS = "ElementalCraft/elementalcraft-visuals.toml";
    public static final String THUNDER_FROST = "ElementalCraft/elementalcraft-thunder-frost-reactions.toml";
    public static final String ISS_INTEGRATION = "ElementalCraft/elementalcraft-iss-integration.toml";

    private static final Map<IConfigSpec<?>, String> PATH_BY_SPEC = new LinkedHashMap<>();
    private static final Map<String, Runnable> RELOAD_BY_PATH = new LinkedHashMap<>();

    public static void register(ForgeConfigSpec spec, String path, Runnable reload) {
        PATH_BY_SPEC.put(spec, path);
        RELOAD_BY_PATH.put(path, reload);
    }

    public static void reloadByPath(String path) {
        Runnable reload = RELOAD_BY_PATH.get(path);
        if (reload != null) reload.run();
    }

    public static String pathOf(IConfigSpec<?> spec) {
        return PATH_BY_SPEC.get(spec);
    }

    public static void reloadAll() {
        for (Runnable reload : RELOAD_BY_PATH.values()) {
            reload.run();
        }
    }

    public static java.util.Set<String> paths() {
        return RELOAD_BY_PATH.keySet();
    }

    private ConfigReloadRegistry() {
    }
}
