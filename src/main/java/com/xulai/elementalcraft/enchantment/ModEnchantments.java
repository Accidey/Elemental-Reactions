package com.xulai.elementalcraft.enchantment;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.function.Supplier;

public class ModEnchantments {

    public static final Supplier<Holder<Enchantment>> FIRE_STRIKE = holder("fire_strike");
    public static final Supplier<Holder<Enchantment>> NATURE_STRIKE = holder("nature_strike");
    public static final Supplier<Holder<Enchantment>> FROST_STRIKE = holder("frost_strike");
    public static final Supplier<Holder<Enchantment>> THUNDER_STRIKE = holder("thunder_strike");

    public static final Supplier<Holder<Enchantment>> FIRE_ENHANCE = holder("fire_enhancement");
    public static final Supplier<Holder<Enchantment>> NATURE_ENHANCE = holder("nature_enhancement");
    public static final Supplier<Holder<Enchantment>> FROST_ENHANCE = holder("frost_enhancement");
    public static final Supplier<Holder<Enchantment>> THUNDER_ENHANCE = holder("thunder_enhancement");

    public static final Supplier<Holder<Enchantment>> FIRE_RESIST = holder("fire_resistance");
    public static final Supplier<Holder<Enchantment>> NATURE_RESIST = holder("nature_resistance");
    public static final Supplier<Holder<Enchantment>> FROST_RESIST = holder("frost_resistance");
    public static final Supplier<Holder<Enchantment>> THUNDER_RESIST = holder("thunder_resistance");

    private static Supplier<Holder<Enchantment>> holder(String name) {
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(ElementalCraft.MODID, name));
        return () -> vanilla(key);
    }

    public static Holder<Enchantment> vanilla(ResourceKey<Enchantment> key) {
        HolderLookup.RegistryLookup<Enchantment> lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
        if (lookup == null) throw new IllegalStateException("Enchantment registry not ready");
        return lookup.get(key).orElseThrow(() -> new IllegalStateException("Missing enchantment " + key.location()));
    }
}
