package com.xulai.elementalcraft.client;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = ElementalCraft.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ElementalCraft.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> THUNDER_SPARK_PERSISTENT =
            PARTICLE_TYPES.register("thunder_spark_persistent", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST_SNOWFLAKE =
            PARTICLE_TYPES.register("frost_snowflake", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST_SNOWFLAKE_ISS =
            PARTICLE_TYPES.register("frost_snowflake_iss", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST_ICE_RUNE =
            PARTICLE_TYPES.register("frost_ice_rune", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STEAM_CLOUD =
            PARTICLE_TYPES.register("steam_cloud", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TOXIC_BLAST =
            PARTICLE_TYPES.register("toxic_blast", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STORM_CLOUD =
            PARTICLE_TYPES.register("storm_cloud", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CHERRY_BLOSSOM =
            PARTICLE_TYPES.register("cherry_blossom", () -> new SimpleParticleType(false));

    private static final boolean ISS_LOADED = ModList.get().isLoaded("irons_spellbooks");

    public static SimpleParticleType frostSnowflake() {
        return ISS_LOADED ? FROST_SNOWFLAKE_ISS.get() : FROST_SNOWFLAKE.get();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                THUNDER_SPARK_PERSISTENT.get(),
                PersistentSparkParticle.Factory::new
        );

        event.registerSpriteSet(
                FROST_SNOWFLAKE.get(),
                FrostSnowflakeParticle.Factory::new
        );

        event.registerSpriteSet(
                FROST_SNOWFLAKE_ISS.get(),
                FrostSnowflakeParticle.Factory::new
        );

        event.registerSpriteSet(
                STEAM_CLOUD.get(),
                SteamCloudParticle.Factory::new
        );

        event.registerSpriteSet(
                TOXIC_BLAST.get(),
                ToxicBlastParticle.Factory::new
        );

        event.registerSpriteSet(
                STORM_CLOUD.get(),
                StormCloudParticle.Factory::new
        );

        event.registerSpriteSet(
                CHERRY_BLOSSOM.get(),
                CherryBlossomParticle.Factory::new
        );

        event.registerSpriteSet(
                FROST_ICE_RUNE.get(),
                FrostIceRuneParticle.Factory::new
        );
    }
}
