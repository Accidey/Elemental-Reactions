package com.xulai.elementalcraft.potion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public final class DrownHelper {

    public static final String FREEZE_TIMER_KEY = "EC_FreezeDrownTimer";
    public static final String PARALYSIS_TIMER_KEY = "EC_ParalysisDrownTimer";

    private static final int DROWN_INTERVAL_TICKS = 20;
    private static final float DROWN_DAMAGE = 2.0F;
    private static final double FREEZE_SINK_SPEED = 0.1;
    private static final double PARALYSIS_SINK_SPEED = 0.05;

    public enum SinkMode { FREEZE, PARALYSIS }

    private DrownHelper() {}

    public static void tickWaterSink(LivingEntity entity, SinkMode mode) {
        String timerKey = mode == SinkMode.FREEZE ? FREEZE_TIMER_KEY : PARALYSIS_TIMER_KEY;

        if (!entity.isInWater()) {
            entity.getPersistentData().remove(timerKey);
            if (mode == SinkMode.FREEZE) {
                entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
            }
            return;
        }

        double sinkSpeed = mode == SinkMode.FREEZE ? FREEZE_SINK_SPEED : PARALYSIS_SINK_SPEED;
        entity.move(MoverType.SELF, new Vec3(0, -sinkSpeed, 0));
        if (mode == SinkMode.FREEZE) {
            entity.setDeltaMovement(0, 0, 0);
        } else {
            entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);
        }

        CompoundTag data = entity.getPersistentData();
        int drownTimer = data.getIntOr(timerKey, 0) + 1;
        if (drownTimer >= DROWN_INTERVAL_TICKS) {
            drownTimer = 0;
            entity.hurt(entity.damageSources().drown(), DROWN_DAMAGE);
        }
        data.putInt(timerKey, drownTimer);
    }

    public static void clearTimers(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.remove(FREEZE_TIMER_KEY);
        data.remove(PARALYSIS_TIMER_KEY);
    }
}
