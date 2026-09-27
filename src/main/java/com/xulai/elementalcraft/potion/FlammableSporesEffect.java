package com.xulai.elementalcraft.potion;

import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.event.FrostbiteHandler;
import com.xulai.elementalcraft.init.ModDamageTypes;
import com.xulai.elementalcraft.util.DebugMode;
import com.xulai.elementalcraft.command.DebugCommand;
import com.xulai.elementalcraft.util.EffectHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class FlammableSporesEffect extends MobEffect {

    public FlammableSporesEffect() {
        super(MobEffectCategory.HARMFUL, 0x2E8B57);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            if (ElementalThunderFrostReactionsConfig.frostbiteSporeDecaySpeed > 1
                    && (FrostbiteHandler.hasFrostbite(entity) || FrostbiteHandler.isTempFrostbite(entity))) {
                return true;
            }

            int damageInterval = ElementalFireNatureReactionsConfig.sporeDamageInterval;
            if (damageInterval <= 0) {
                damageInterval = 100;
            }
            if (entity.tickCount % damageInterval == 0) {
                double damagePerStack = ElementalFireNatureReactionsConfig.sporePoisonDamage;
                if (damagePerStack > 0) {
                    float totalDamage = (float) damagePerStack;
                    if (DebugMode.hasAnyDebugEnabled() && !entity.getPersistentData().getBoolean("EC_SporeDamageLogged")) {
                        DebugCommand.sendSporeDamageLog(entity, (float) damagePerStack, amplifier + 1, totalDamage);
                        entity.getPersistentData().putBoolean("EC_SporeDamageLogged", true);
                    }
                    entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.SPORES), totalDamage);
                }
            }

            if (entity.tickCount % 20 == 0) {
                double durabilityPercent = ElementalFireNatureReactionsConfig.sporeDurabilityPercent;
                if (durabilityPercent > 0) {
                    int stacks = amplifier + 1;
                    if (stacks >= 5) {
                        for (EquipmentSlot slot : new EquipmentSlot[]{
                                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                                EquipmentSlot.LEGS, EquipmentSlot.FEET,
                                EquipmentSlot.MAINHAND}) {
                            ItemStack stack = entity.getItemBySlot(slot);
                            if (!stack.isEmpty() && stack.isDamageableItem()) {
                                int durabilityDamage = Math.max(1, (int) Math.round(stack.getMaxDamage() * durabilityPercent / 100.0));
                                stack.hurtAndBreak(durabilityDamage, entity, slot);
                            }
                        }
                    } else {
                        List<EquipmentSlot> slots = new ArrayList<>();
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            ItemStack stack = entity.getItemBySlot(slot);
                            if (!stack.isEmpty() && stack.isDamageableItem()) {
                                slots.add(slot);
                            }
                        }
                        for (int i = 0; i < stacks && !slots.isEmpty(); i++) {
                            EquipmentSlot targetSlot = slots.remove(entity.getRandom().nextInt(slots.size()));
                            ItemStack stack = entity.getItemBySlot(targetSlot);
                            int durabilityDamage = Math.max(1, (int) Math.round(stack.getMaxDamage() * durabilityPercent / 100.0));
                            stack.hurtAndBreak(durabilityDamage, entity, targetSlot);
                        }
                    }
                }
            }

            if (entity.tickCount % 2 == 0) {
                EffectHelper.playSporeAmbient(entity);
            }

        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }
}
