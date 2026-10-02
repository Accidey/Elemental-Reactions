package com.xulai.elementalcraft.util;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class MobEffectLookup {

    private MobEffectLookup() {
    }

    public static boolean hasEffect(LivingEntity entity, Holder<MobEffect> holder) {
        MobEffect effect = holder.value();
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (instance.getEffect().value() == effect) return true;
        }
        return false;
    }

    public static MobEffectInstance getEffect(LivingEntity entity, Holder<MobEffect> holder) {
        MobEffect effect = holder.value();
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (instance.getEffect().value() == effect) return instance;
        }
        return null;
    }
}