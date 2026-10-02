package com.xulai.elementalcraft.client;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalVisualConfig;
import com.xulai.elementalcraft.util.ElementType;
import com.xulai.elementalcraft.util.ElementUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public class NatureMobVisuals {

    private NatureMobVisuals() {}

    private static final double MOVEMENT_THRESHOLD_SQ = 1.0E-6;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!ElementalVisualConfig.natureMobEnabled) return;

        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (!level.isClientSide) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!isValidTarget(entity, mc.player)) return;

        double dx = entity.xo - entity.xOld;
        double dz = entity.zo - entity.zOld;
        if (dx * dx + dz * dz < MOVEMENT_THRESHOLD_SQ) return;

        int tier = NatureVisuals.calculateVisualTier(entity, ElementType.NATURE);
        if (tier <= 0) return;

        int count = ElementalVisualConfig.natureMobParticlesPerTier * tier;
        double spawnHeightOffset = ElementalVisualConfig.natureMobSpawnHeightOffset;
        double horizontalSpeed = ElementalVisualConfig.natureMobHorizontalSpeed;
        double bodyWidth = entity.getBbWidth();
        double bodyHeight = entity.getBbHeight();
        double baseX = entity.getX();
        double baseY = entity.getY();
        double baseZ = entity.getZ();

        for (int i = 0; i < count; i++) {
            double px = baseX + (Math.random() * 2 - 1) * bodyWidth * 0.5;
            double py = baseY + Math.random() * (bodyHeight + spawnHeightOffset);
            double pz = baseZ + (Math.random() * 2 - 1) * bodyWidth * 0.5;
            double vx = (Math.random() * 2 - 1) * horizontalSpeed;
            double vz = (Math.random() * 2 - 1) * horizontalSpeed;
            level.addParticle(ParticleTypes.CHERRY_LEAVES, px, py, pz, vx, 0, vz);
        }
    }

    private static boolean isValidTarget(LivingEntity entity, Player localPlayer) {
        if (ElementalVisualConfig.natureMobHideWhenInvisible && entity.hasEffect(MobEffects.INVISIBILITY)) {
            return false;
        }
        if (entity == localPlayer && ElementalVisualConfig.natureMobHideSelfFirstPerson && isFirstPerson()) {
            return false;
        }
        return ElementUtils.getConsistentAttackElement(entity) == ElementType.NATURE;
    }

    private static boolean isFirstPerson() {
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        return cameraType == CameraType.FIRST_PERSON;
    }
}