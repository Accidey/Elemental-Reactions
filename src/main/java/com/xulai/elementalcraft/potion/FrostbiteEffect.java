package com.xulai.elementalcraft.potion;

import com.xulai.elementalcraft.command.DebugCommand;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.event.FrostbiteHandler;
import com.xulai.elementalcraft.init.ModDamageTypes;
import com.xulai.elementalcraft.util.ElementType;
import com.xulai.elementalcraft.util.ElementUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class FrostbiteEffect extends MobEffect {

    public static final UUID SPEED_MODIFIER_UUID = UUID.fromString("7107DE5E-7CE8-403C-8064-03E786A055C8");
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("7107DE5E-7CE8-403C-8064-03E786A055C9");

    public FrostbiteEffect() {
        super(MobEffectCategory.HARMFUL, 0x66CCFF);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        applySlowness(attributeMap, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID,
                ElementalThunderFrostReactionsConfig.frostbiteSpeedReductionPerStack, amplifier);
        applySlowness(attributeMap, Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_UUID,
                ElementalThunderFrostReactionsConfig.frostbiteAttackSpeedReductionPerStack, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance speedAttr = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_MODIFIER_UUID);
        AttributeInstance attackAttr = attributeMap.getInstance(Attributes.ATTACK_SPEED);
        if (attackAttr != null) attackAttr.removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        entity.setTicksFrozen(0);
    }

    private static void applySlowness(AttributeMap attributeMap, Attribute attribute, UUID uuid, double reductionPerStack, int amplifier) {
        AttributeInstance instance = attributeMap.getInstance(attribute);
        if (instance == null) return;
        instance.removeModifier(uuid);
        if (reductionPerStack <= 0) return;
        double value = Math.max(-reductionPerStack * (amplifier + 1), -0.9);
        instance.addPermanentModifier(new AttributeModifier(uuid, attribute.getDescriptionId() + " " + amplifier, value, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;

        int damageInterval = ElementalThunderFrostReactionsConfig.frostbiteDamageIntervalTicks;
        if (damageInterval < 1) damageInterval = 1;

        CompoundTag data = entity.getPersistentData();
        int damageTimer = data.getInt(FrostbiteHandler.NBT_FROSTBITE_DAMAGE_TIMER) + 1;

        if (damageTimer >= damageInterval) {
            damageTimer = 0;
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

        data.putInt(FrostbiteHandler.NBT_FROSTBITE_DAMAGE_TIMER, damageTimer);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
