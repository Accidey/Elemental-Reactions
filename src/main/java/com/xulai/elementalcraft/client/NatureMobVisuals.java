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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public class NatureMobVisuals {

    private NatureMobVisuals() {}

    private static final Map<LivingEntity, Integer> ACTIVE_AURAS = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ElementalVisualConfig.natureMobEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Level level = mc.level;
        Player localPlayer = mc.player;

        if (level.getGameTime() % ElementalVisualConfig.natureMobScanInterval == 0) {
            refreshAuras(level, localPlayer);
        }

        spawnParticles(level, localPlayer);
    }

    private static void refreshAuras(Level level, Player localPlayer) {
        double radius = ElementalVisualConfig.natureMobRadius;
        Vec3 center = localPlayer.position();
        AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);

        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive());

        Map<LivingEntity, Integer> newAuras = new HashMap<>();
        for (LivingEntity entity : candidates) {
            if (!isValidTarget(entity, localPlayer)) continue;
            int tier = NatureVisuals.calculateVisualTier(entity, ElementType.NATURE);
            if (tier > 0) {
                newAuras.put(entity, tier);
            }
        }

        ACTIVE_AURAS.clear();
        ACTIVE_AURAS.putAll(newAuras);
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

    private static void spawnParticles(Level level, Player localPlayer) {
        if (ACTIVE_AURAS.isEmpty()) return;

        double radius = ElementalVisualConfig.natureMobRadius;
        double radiusSq = radius * radius;
        ACTIVE_AURAS.entrySet().removeIf(e -> {
            LivingEntity entity = e.getKey();
            return !entity.isAlive() || entity.distanceToSqr(localPlayer) > radiusSq;
        });

        if (ACTIVE_AURAS.isEmpty()) return;

        List<Map.Entry<LivingEntity, Integer>> sorted = new ArrayList<>(ACTIVE_AURAS.entrySet());
        sorted.sort(Comparator.comparingDouble(e -> e.getKey().distanceToSqr(localPlayer)));

        int budget = ElementalVisualConfig.natureMobMaxParticlesPerTick;
        int remaining = budget;
        int totalDemand = 0;
        for (Map.Entry<LivingEntity, Integer> entry : sorted) {
            totalDemand += ElementalVisualConfig.natureMobParticlesPerTier * entry.getValue();
        }

        double scale = Math.min(1.0, (double) budget / Math.max(1, totalDemand));

        for (Map.Entry<LivingEntity, Integer> entry : sorted) {
            if (remaining <= 0) break;
            int baseDemand = ElementalVisualConfig.natureMobParticlesPerTier * entry.getValue();
            int scaled = Math.max(1, (int) Math.floor(baseDemand * scale));
            int used = spawnAuraScaled(level, entry.getKey(), entry.getValue(), scaled, remaining);
            remaining -= used;
        }
    }

    private static int spawnAuraScaled(Level level, LivingEntity entity, int tier, int particleBudget, int hardCap) {
        double spawnHeightOffset = ElementalVisualConfig.natureMobSpawnHeightOffset;
        double radius = Math.max(0.3, entity.getBbWidth() * ElementalVisualConfig.natureMobRadiusFactor);
        int desiredPerTick = ElementalVisualConfig.natureMobParticlesPerTier * tier;
        int count = Math.min(particleBudget, Math.min(desiredPerTick, hardCap));
        if (count <= 0) return 0;

        double horizontalSpeed = ElementalVisualConfig.natureMobHorizontalSpeed;
        double originX = entity.getX();
        double originY = entity.getY() + entity.getBbHeight() + spawnHeightOffset;
        double originZ = entity.getZ();
        int spawned = 0;

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.sqrt(Math.random()) * radius;
            double px = originX + Math.cos(angle) * distance;
            double pz = originZ + Math.sin(angle) * distance;
            double vx = (Math.random() * 2 - 1) * horizontalSpeed;
            double vz = (Math.random() * 2 - 1) * horizontalSpeed;
            level.addParticle(ParticleTypes.CHERRY_LEAVES, px, originY, pz, vx, 0, vz);
            spawned++;
        }

        return spawned;
    }
}