package com.xulai.elementalcraft.client;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalVisualConfig;
import com.xulai.elementalcraft.util.ElementType;
import com.xulai.elementalcraft.util.ElementUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public class ThunderMobVisuals {

    private ThunderMobVisuals() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!ElementalVisualConfig.thunderMobEnabled) return;

        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        Level level = entity.level();
        if (!level.isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!isValidTarget(entity, mc.player)) return;

        int interval = ElementalVisualConfig.thunderMobSpawnInterval;
        if (interval <= 0 || entity.tickCount % interval != 0) return;

        int tier = ThunderVisuals.calculateVisualTier(entity, ElementType.THUNDER);
        if (tier <= 0) return;

        int count = ElementalVisualConfig.thunderMobParticlesPerTier * tier;
        double horizontalRadius = ElementalVisualConfig.thunderMobRadius;
        double verticalTop = entity.getBbHeight() + ElementalVisualConfig.thunderMobHeightOffset;
        double baseX = entity.getX();
        double baseY = entity.getY();
        double baseZ = entity.getZ();
        SimpleParticleType particle = ModParticles.THUNDER_SPARK_PERSISTENT.get();

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.sqrt(Math.random()) * horizontalRadius;
            double px = baseX + Math.cos(angle) * distance;
            double py = baseY + Math.random() * verticalTop;
            double pz = baseZ + Math.sin(angle) * distance;
            level.addParticle(particle, px, py, pz, 0, 0, 0);
        }
    }

    private static boolean isValidTarget(LivingEntity entity, Player localPlayer) {
        if (ElementalVisualConfig.thunderMobHideWhenInvisible && entity.hasEffect(MobEffects.INVISIBILITY)) {
            return false;
        }
        if (entity == localPlayer && ElementalVisualConfig.thunderMobHideSelfFirstPerson && isFirstPerson()) {
            return false;
        }
        return ElementUtils.getConsistentAttackElement(entity) == ElementType.THUNDER;
    }

    private static boolean isFirstPerson() {
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        return cameraType == CameraType.FIRST_PERSON;
    }
}