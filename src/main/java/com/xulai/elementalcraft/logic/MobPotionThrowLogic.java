package com.xulai.elementalcraft.logic;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.event.WetnessHandler;
import com.xulai.elementalcraft.util.ElementType;
import com.xulai.elementalcraft.util.ElementUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class MobPotionThrowLogic {

    private static final double THROW_RANGE = 12.0;
    private static final int MAX_MISSES = 3;
    private static final int ATTEMPT_INTERVAL = 40;
    private static final int ROUND_THROWS = 3;

    private static final String NBT_BOTTLE_ROLLED = "EC_BottleRolled";
    private static final String NBT_BOTTLE_CD = "EC_BottleCd";
    private static final String NBT_BOTTLE_MISS = "EC_BottleMiss";
    private static final String NBT_BOTTLE_VERDICT = "EC_BottleVerdict";
    private static final String NBT_BOTTLE_ROUND = "EC_BottleRound";
    private static final String NBT_BOTTLE_WAIT = "EC_BottleWait";

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!ElementalConfig.mobPotionThrowEnabled) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        CompoundTag data = mob.getPersistentData();
        if (!data.getBooleanOr("ElementalCraft_AttributesSet", false)) return;

        if (!data.getBooleanOr(NBT_BOTTLE_ROLLED, false)) {
            tryEquip(mob, data);
            data.putBoolean(NBT_BOTTLE_ROLLED, true);
        }

        tryThrow(mob, data);
    }

    private static void tryEquip(Mob mob, CompoundTag data) {
        if (!mob.getOffhandItem().isEmpty()) return;
        ElementType element = ElementUtils.getConsistentAttackElement(mob);
        if (element == ElementType.NONE) return;
        boolean isFire = element == ElementType.FIRE;
        if (!isFire && ElementalFireNatureReactionsConfig.wetnessNetherDimensionImmune
                && mob.level().dimension() == Level.NETHER) return;
        if (ThreadLocalRandom.current().nextDouble() >= ElementalConfig.mobBottleEquipChance) return;

        ItemStack bottle = new ItemStack(Items.SPLASH_POTION);
        bottle.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
                new PotionContents(isFire ? Potions.POISON : Potions.WATER));
        mob.setItemSlot(EquipmentSlot.OFFHAND, bottle);
        mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    private static void tryThrow(Mob mob, CompoundTag data) {

        ItemStack offhand = mob.getOffhandItem();
        if (!offhand.is(Items.SPLASH_POTION)) return;
        ElementType element = ElementUtils.getConsistentAttackElement(mob);
        boolean isFire = element == ElementType.FIRE;
        PotionContents contents = offhand.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (!isFire && (contents == null || !contents.is(Potions.WATER))) return;
        if (isFire && (contents == null || !contents.is(Potions.POISON))) return;

        long gameTime = mob.level().getGameTime();
        if (gameTime < data.getLongOr(NBT_BOTTLE_CD, 0L)) return;

        long verdict = data.getLongOr(NBT_BOTTLE_VERDICT, 0L);
        if (verdict > 0) {
            if (gameTime < verdict) return;
            LivingEntity target = mob.getTarget();
            boolean hit = target != null && target.isAlive()
                    && (isFire ? target.hasEffect(MobEffects.POISON) : WetnessHandler.getWetnessLevel(target) > 0);
            if (hit) {
                data.putInt(NBT_BOTTLE_MISS, 0);
                data.remove(NBT_BOTTLE_VERDICT);
            } else {
                int miss = data.getIntOr(NBT_BOTTLE_MISS, 0) + 1;
                if (miss >= MAX_MISSES) {
                    data.putLong(NBT_BOTTLE_CD, gameTime + ElementalConfig.mobBottleThrowCooldown);
                    data.putInt(NBT_BOTTLE_MISS, 0);
                    data.remove(NBT_BOTTLE_VERDICT);
                    return;
                }
                data.putInt(NBT_BOTTLE_MISS, miss);
                data.remove(NBT_BOTTLE_VERDICT);
            }
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return;

        if (mob.distanceToSqr(target) > THROW_RANGE * THROW_RANGE) return;
        if (!mob.getSensing().hasLineOfSight(target)) return;

        if (isFire) {
            int wetnessMax = ElementalFireNatureReactionsConfig.wetnessMaxLevel;
            int wetness = WetnessHandler.getWetnessLevel(target);
            if (wetnessMax > 0 && wetness >= wetnessMax) {
                data.putInt(NBT_BOTTLE_ROUND, 0);
                data.putBoolean(NBT_BOTTLE_WAIT, true);
                return;
            }
            if (data.getBooleanOr(NBT_BOTTLE_WAIT, false)) {
                if (wetness > 0) return;
                data.remove(NBT_BOTTLE_WAIT);
            }
            if (data.getIntOr(NBT_BOTTLE_ROUND, 0) >= ROUND_THROWS) {
                data.putLong(NBT_BOTTLE_CD, gameTime + ElementalConfig.mobBottleThrowCooldown);
                data.putInt(NBT_BOTTLE_ROUND, 0);
                return;
            }
            throwSplashBottle(mob, target, true);
        } else {
            if (!WetnessHandler.canGainWetness(target)) return;
            int wetnessLevel = WetnessHandler.getWetnessLevel(target);
            if (wetnessLevel >= ElementalFireNatureReactionsConfig.wetnessMaxLevel) {
                data.putInt(NBT_BOTTLE_ROUND, 0);
                data.putBoolean(NBT_BOTTLE_WAIT, true);
                return;
            }
            if (data.getBooleanOr(NBT_BOTTLE_WAIT, false)) {
                if (wetnessLevel > 0) return;
                data.remove(NBT_BOTTLE_WAIT);
            }
            if (data.getIntOr(NBT_BOTTLE_ROUND, 0) >= ROUND_THROWS) {
                data.putLong(NBT_BOTTLE_CD, gameTime + ElementalConfig.mobBottleThrowCooldown);
                data.putInt(NBT_BOTTLE_ROUND, 0);
                return;
            }
            throwSplashBottle(mob, target, false);
        }
        data.putLong(NBT_BOTTLE_VERDICT, gameTime + ATTEMPT_INTERVAL);
        data.putInt(NBT_BOTTLE_ROUND, data.getIntOr(NBT_BOTTLE_ROUND, 0) + 1);
    }
    private static void throwSplashBottle(Mob mob, LivingEntity target, boolean poison) {
        ItemStack bottle = new ItemStack(Items.SPLASH_POTION);
        bottle.set(DataComponents.POTION_CONTENTS, new PotionContents(poison ? Potions.POISON : Potions.WATER));

        ThrownSplashPotion potion = new ThrownSplashPotion(mob.level(), mob, bottle);
        potion.setXRot(potion.getXRot() - -20.0F);

        Vec3 vel = target.getDeltaMovement();
        double d0 = target.getX() + vel.x - mob.getX();
        double d1 = target.getEyeY() - 1.1 - mob.getEyeY();
        double d2 = target.getZ() + vel.z - mob.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        potion.shoot(d0, d1 + d3 * 0.2, d2, 0.75F, 8.0F);

        mob.level().addFreshEntity(potion);
        mob.level().playSound(null, mob, SoundEvents.WITCH_THROW, mob.getSoundSource(), 1.0F, 0.8F);
    }
}
