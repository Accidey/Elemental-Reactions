package com.xulai.elementalcraft.event;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;

public final class PotionStackHandler {

    private static final Field ITEM_MAX_STACK_FIELD;

    static {
        Field field = null;
        for (String name : new String[]{"maxStackSize", "f_41370_"}) {
            try {
                field = Item.class.getDeclaredField(name);
                field.setAccessible(true);
                break;
            } catch (NoSuchFieldException ignored) {
            }
        }
        if (field == null) {
            ElementalCraft.LOGGER.error("Failed to find Item.maxStackSize field");
        }
        ITEM_MAX_STACK_FIELD = field;
    }

    public static void applyCurrentValue() {
        int limit = ElementalConfig.potionStack64Enabled ? 64 : 1;
        setStackLimit(Items.POTION, limit);
        setStackLimit(Items.SPLASH_POTION, limit);
        setStackLimit(Items.LINGERING_POTION, limit);
    }

    private static void setStackLimit(Item item, int limit) {
        if (ITEM_MAX_STACK_FIELD == null) return;
        try {
            ITEM_MAX_STACK_FIELD.setInt(item, limit);
        } catch (IllegalAccessException e) {
            ElementalCraft.LOGGER.error("Failed to set maxStackSize for {}", item, e);
        }
    }

    private PotionStackHandler() {
    }
}
