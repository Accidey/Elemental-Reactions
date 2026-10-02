package com.xulai.elementalcraft.client;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalVisualConfig;
import com.xulai.elementalcraft.event.iss.ISSCore;
import com.xulai.elementalcraft.util.ElementType;
import com.xulai.elementalcraft.util.ElementUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
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
public class FireMobVisuals {

    private FireMobVisuals() {}

    private static final Map<LivingEntity, Integer> ACTIVE_AURAS = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ElementalVisualConfig.fireMobEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Level level = mc.level;
        Player localPlayer = mc.player;

        if (level.getGameTime() % ElementalVisualConfig.fireMobScanInterval == 0) {
            refreshAuras(level, localPlayer);
        }

        spawnParticles(level, localPlayer);
    }

    private static void refreshAuras(Level level, Player localPlayer) {
        double radius = ElementalVisualConfig.fireMobRadius;
        Vec3 center = localPlayer.position();
        AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);

        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && e != localPlayer);

        Map<LivingEntity, Integer> newAuras = new HashMap<>();
        for (LivingEntity entity : candidates) {
            if (!isValidTarget(entity, localPlayer)) continue;
            int tier = FireVisuals.calculateVisualTier(entity, ElementType.FIRE);
            if (tier > 0) {
                newAuras.put(entity, tier);
            }
        }

        ACTIVE_AURAS.clear();
        ACTIVE_AURAS.putAll(newAuras);
    }

    private static boolean isValidTarget(LivingEntity entity, Player localPlayer) {
        if (ElementalVisualConfig.fireMobHideWhenInvisible && entity.hasEffect(MobEffects.INVISIBILITY)) {
            return false;
        }
        if (entity == localPlayer && ElementalVisualConfig.fireMobHideSelfFirstPerson) {
            return false;
        }
        return ElementUtils.getConsistentAttackElement(entity) == ElementType.FIRE;
    }

    private static void spawnParticles(Level level, Player localPlayer) {
        if (ACTIVE_AURAS.isEmpty()) return;

        double radius = ElementalVisualConfig.fireMobRadius;
        double radiusSq = radius * radius;
        ACTIVE_AURAS.entrySet().removeIf(e -> {
            LivingEntity entity = e.getKey();
            return !entity.isAlive() || entity.distanceToSqr(localPlayer) > radiusSq;
        });

        if (ACTIVE_AURAS.isEmpty()) return;

        List<Map.Entry<LivingEntity, Integer>> sorted = new ArrayList<>(ACTIVE_AURAS.entrySet());
        sorted.sort(Comparator.comparingDouble(e -> e.getKey().distanceToSqr(localPlayer)));

        int budget = ElementalVisualConfig.fireMobMaxParticlesPerTick;
        int remaining = budget;
        int totalDemand = 0;
        for (Map.Entry<LivingEntity, Integer> entry : sorted) {
            totalDemand += ElementalVisualConfig.fireMobParticlesPerHelix * entry.getValue();
            if (entry.getValue() >= 4) {
                totalDemand += ElementalVisualConfig.fireMobTopBurstParticles;
            }
        }

        double scale = Math.min(1.0, (double) budget / Math.max(1, totalDemand));

        for (Map.Entry<LivingEntity, Integer> entry : sorted) {
            if (remaining <= 0) break;
            int baseDemand = ElementalVisualConfig.fireMobParticlesPerHelix * entry.getValue();
            int scaled = Math.max(1, (int) Math.floor(baseDemand * scale));
            int used = spawnAuraScaled(level, entry.getKey(), entry.getValue(), scaled, remaining);
            remaining -= used;
        }
    }

    private static int spawnAuraScaled(Level level, LivingEntity entity, int tier, int particleBudget, int hardCap) {
        double height = entity.getBbHeight() + 0.3;
        double radius = Math.max(0.3, entity.getBbWidth() * ElementalVisualConfig.fireMobRadiusFactor);
        double rotationSpeed = ElementalVisualConfig.fireMobRotationSpeed;
        int desiredPerTick = ElementalVisualConfig.fireMobParticlesPerHelix * tier;
        int count = Math.min(particleBudget, Math.min(desiredPerTick, hardCap));
        if (count <= 0) return 0;

        double turns = Math.max(1.0, (rotationSpeed * ElementalVisualConfig.fireMobParticleLife) / (2 * Math.PI));
        double totalPhase = 2 * Math.PI * turns;

        ParticleOptions particle = ISSCore.getFireParticle();
        Vec3 origin = entity.position();
        int spawned = 0;

        for (int i = 0; i < count; i++) {
            double phaseOffset = (2 * Math.PI * i) / count;
            double phase = (entity.tickCount * rotationSpeed + phaseOffset) % totalPhase;
            double y = (phase / totalPhase) * height;
            double angle = phase;
            double px = origin.x + Math.cos(angle) * radius;
            double pz = origin.z + Math.sin(angle) * radius;
            level.addParticle(particle, px, origin.y + y, pz, 0, 0, 0);
            spawned++;
        }

        if (tier >= 4 && ElementalVisualConfig.fireMobTopBurstParticles > 0) {
            int burstBudget = Math.max(0, hardCap - count);
            int burst = Math.min(ElementalVisualConfig.fireMobTopBurstParticles, burstBudget);
            double topY = origin.y + height;
            for (int i = 0; i < burst; i++) {
                double angle = entity.tickCount * 0.4 + (2 * Math.PI * i) / Math.max(1, burst);
                double vx = Math.cos(angle) * 0.05;
                double vz = Math.sin(angle) * 0.05;
                level.addParticle(particle, origin.x, topY, origin.z, vx, 0.02, vz);
                spawned++;
            }
        }

        return spawned;
    }
}