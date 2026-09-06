package com.xulai.elementalcraft.init;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ModDamageTypes {

    public static final ResourceKey<DamageType> STEAM_SCALDING = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(ElementalCraft.MODID, "steam_scalding")
    );
    public static final ResourceKey<DamageType> STEAM_SPORE_COMBUSTION = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(ElementalCraft.MODID, "steam_spore_combustion")
    );

    public static final ResourceKey<DamageType> LAVA_MAGIC = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(ElementalCraft.MODID, "lava_magic")
    );

    public static final ResourceKey<DamageType> SPORES = ResourceKey.create(
           Registries.DAMAGE_TYPE,
           Identifier.fromNamespaceAndPath(ElementalCraft.MODID, "spores")
    );

    public static final ResourceKey<DamageType> STATIC_SHOCK = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(ElementalCraft.MODID, "static_shock")
    );

    public static final ResourceKey<DamageType> FROSTBITE_THERMAL_SHOCK = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(ElementalCraft.MODID, "frostbite_thermal_shock")
    );

    public static final ResourceKey<DamageType> FROSTBITE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(ElementalCraft.MODID, "frostbite")
    );

    public static final ResourceKey<DamageType> TOXIC_BLAST = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(ElementalCraft.MODID, "toxic_blast")
    );

    public static DamageSource source(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(key),
                null,
                null
        );
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> key, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(key),
                null,
                attacker
        );
    }
}
