package com.xulai.elementalcraft.client;

import com.xulai.elementalcraft.ElementalCraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = ElementalCraft.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModParticleProviders {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                ModParticles.THUNDER_SPARK_PERSISTENT.get(),
                PersistentSparkParticle.Factory::new
        );

        event.registerSpriteSet(
                ModParticles.FROST_SNOWFLAKE.get(),
                FrostSnowflakeParticle.Factory::new
        );

        event.registerSpriteSet(
                ModParticles.FROST_SNOWFLAKE_ISS.get(),
                FrostSnowflakeParticle.LongLivedFactory::new
        );

        event.registerSpriteSet(
                ModParticles.STEAM_CLOUD.get(),
                SteamCloudParticle.Factory::new
        );

        event.registerSpriteSet(
                ModParticles.TOXIC_BLAST.get(),
                ToxicBlastParticle.Factory::new
        );

        event.registerSpriteSet(
                ModParticles.STORM_CLOUD.get(),
                StormCloudParticle.Factory::new
        );

        event.registerSpriteSet(
                ModParticles.CHERRY_BLOSSOM.get(),
                CherryBlossomParticle.Factory::new
        );

        event.registerSpriteSet(
                ModParticles.FROST_ICE_RUNE.get(),
                FrostIceRuneParticle.Factory::new
        );
    }
}
