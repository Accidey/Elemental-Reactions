package com.xulai.elementalcraft.potion;

import com.xulai.elementalcraft.util.EffectHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class ParalysisEffect extends MobEffect {
    private static final String NBT_AI_DISABLED = "EC_AIDisabled";
    private static final String NBT_ORIGINAL_NO_AI = "EC_ParalysisOriginalNoAI";

    public ParalysisEffect() {
        super(MobEffectCategory.HARMFUL, 0x808080);
    }

    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide()) {
            disableAI(pLivingEntity);

            EffectHelper.playParalysisAmbient(pLivingEntity, pAmplifier);

            DrownHelper.tickWaterSink(pLivingEntity, DrownHelper.SinkMode.PARALYSIS);
        }
        return true;
    }

    public static void onEffectRemoved(LivingEntity entity) {
        restoreAI(entity);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

    private static void disableAI(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) return;
        CompoundTag data = entity.getPersistentData();
        if (data.getBooleanOr(NBT_AI_DISABLED, false)) return;
        if (!data.contains(NBT_ORIGINAL_NO_AI)) {
            data.putBoolean(NBT_ORIGINAL_NO_AI, mob.isNoAi());
        }
        mob.setNoAi(true);
        data.putBoolean(NBT_AI_DISABLED, true);
    }

    private static void restoreAI(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) return;
        CompoundTag data = entity.getPersistentData();
        if (!data.getBooleanOr(NBT_AI_DISABLED, false)) return;
        data.remove(NBT_AI_DISABLED);
        if (entity.hasEffect(ModMobEffects.FREEZE)) return;
        boolean wasNoAi = data.getBooleanOr(NBT_ORIGINAL_NO_AI, false);
        mob.setNoAi(wasNoAi);
        data.remove(NBT_ORIGINAL_NO_AI);
    }

}
