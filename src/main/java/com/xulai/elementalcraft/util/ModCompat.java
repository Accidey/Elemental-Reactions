package com.xulai.elementalcraft.util;

import net.minecraftforge.fml.ModList;

public final class ModCompat {

    private static final boolean ISS_LOADED = computeIss();

    private static boolean computeIss() {
        ModList modList = ModList.get();
        return modList != null && modList.isLoaded("irons_spellbooks");
    }

    public static boolean iss() {
        return ISS_LOADED;
    }

    private ModCompat() {
    }
}
