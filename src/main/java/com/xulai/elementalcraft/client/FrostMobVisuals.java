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
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public class FrostMobVisuals {

    private FrostMobVisuals() {}

    private static final double MOVEMENT_THRESHOLD_SQ = 1.0E-6;
    private static final Map<Integer, Vec3> PREV_POS = new HashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!ElementalVisualConfig.frostMobEnabled) return;

        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        Level level = entity.level();
        if (!level.isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!isValidTarget(entity, mc.player)) return;

        int id = entity.getId();
        Vec3 pos = entity.position();
        Vec3 prev = PREV_POS.get(id);
        boolean moving = false;
        if (prev != null) {
            double dx = pos.x - prev.x;
            double dz = pos.z - prev.z;
            moving = dx * dx + dz * dz >= MOVEMENT_THRESHOLD_SQ;
        }
        PREV_POS.put(id, pos);

        if (!moving) return;

        int tier = FrostVisuals.calculateVisualTier(entity, ElementType.FROST);
        if (tier <= 0) return;

        int count = ElementalVisualConfig.frostMobParticlesPerTier * tier;
        double px = entity.getX();
        double py = entity.getY() + 0.05;
        double pz = entity.getZ();
        for (int i = 0; i < count; i++) {
            double ox = (Math.random() * 2 - 1) * entity.getBbWidth() * 0.3;
            double oz = (Math.random() * 2 - 1) * entity.getBbWidth() * 0.3;
            level.addParticle(ParticleTypes.SNOWFLAKE, px + ox, py, pz + oz, 0, 0, 0);
        }
    }

    private static boolean isValidTarget(LivingEntity entity, Player localPlayer) {
        if (ElementalVisualConfig.frostMobHideWhenInvisible && entity.hasEffect(MobEffects.INVISIBILITY)) {
            return false;
        }
        if (entity == localPlayer && ElementalVisualConfig.frostMobHideSelfFirstPerson && isFirstPerson()) {
            return false;
        }
        return ElementUtils.getConsistentAttackElement(entity) == ElementType.FROST;
    }

    private static boolean isFirstPerson() {
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        return cameraType == CameraType.FIRST_PERSON;
    }
}