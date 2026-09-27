package com.xulai.elementalcraft.potion;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.command.DebugCommand;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.event.FrostbiteHandler;
import com.xulai.elementalcraft.init.ModDamageTypes;
import com.xulai.elementalcraft.util.ElementType;
import com.xulai.elementalcraft.util.ElementUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FrostbiteEffect extends MobEffect {

    public static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(ElementalCraft.MODID, "frostbite_speed");
    public static final ResourceLocation ATTACK_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(ElementalCraft.MODID, "frostbite_attack_speed");

    public FrostbiteEffect() {
        super(MobEffectCategory.HARMFUL, 0x66CCFF);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                amplifier -> {
                    double reduction = ElementalThunderFrostReactionsConfig.frostbiteSpeedReductionPerStack;
                    if (reduction <= 0) return 0.0;
                    return Math.max(-reduction * (amplifier + 1), -0.9);
                }
        );
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER_ID,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                amplifier -> {
                    double reduction = ElementalThunderFrostReactionsConfig.frostbiteSpeedReductionPerStack;
                    if (reduction <= 0) return 0.0;
                    return Math.max(-reduction * (amplifier + 1), -0.9);
                }
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return true;

        int damageInterval = ElementalThunderFrostReactionsConfig.frostbiteDamageIntervalTicks;
        if (entity.tickCount % damageInterval == 0) {
            float baseDamage = (float) ElementalThunderFrostReactionsConfig.frostbitePeriodicDamage;
            float damage = baseDamage;
            ElementType element = ElementUtils.getConsistentAttackElement(entity);
            float elementMult = 1.0f;
            if (element == ElementType.FIRE) {
                elementMult = (float) ElementalThunderFrostReactionsConfig.frostbiteDamageFireMultiplier;
            } else if (element == ElementType.NATURE) {
                elementMult = (float) ElementalThunderFrostReactionsConfig.frostbiteDamageNatureMultiplier;
            } else if (element == ElementType.THUNDER) {
                elementMult = (float) ElementalThunderFrostReactionsConfig.frostbiteDamageThunderMultiplier;
            } else if (element == ElementType.FROST) {
                elementMult = (float) ElementalThunderFrostReactionsConfig.frostbiteDamageFrostMultiplier;
            }
            damage *= elementMult;

            CompoundTag data = entity.getPersistentData();
            float lastDmg = data.getFloat(FrostbiteHandler.NBT_FROSTBITE_LAST_PERIODIC_DMG);
            if (data.getInt(FrostbiteHandler.NBT_FROSTBITE_PERIODIC_LOGGED) == 0 || damage != lastDmg) {
                DebugCommand.sendFrostbitePeriodicDamageLog(entity, baseDamage, element, elementMult, damage);
                data.putFloat(FrostbiteHandler.NBT_FROSTBITE_LAST_PERIODIC_DMG, damage);
                data.putInt(FrostbiteHandler.NBT_FROSTBITE_PERIODIC_LOGGED, 1);
            }

            entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.FROSTBITE), damage);

            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        return true;
    }

    public static void onEffectRemoved(LivingEntity entity) {
        entity.setTicksFrozen(0);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
