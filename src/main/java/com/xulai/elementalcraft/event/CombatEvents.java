package com.xulai.elementalcraft.event;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.command.DebugCommand;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.init.ModDamageTypes;
import com.xulai.elementalcraft.potion.ModMobEffects;
import com.xulai.elementalcraft.util.EffectHelper;
import com.xulai.elementalcraft.util.ElementType;
import com.xulai.elementalcraft.util.ElementUtils;
import com.xulai.elementalcraft.event.ScorchedHandler;
import com.xulai.elementalcraft.event.SteamReactionHandler;
import com.xulai.elementalcraft.event.WetnessHandler;
import com.xulai.elementalcraft.util.DebugMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Random;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class CombatEvents {
    private static final String NBT_LAST_DRY_TICK = "EC_LastSelfDryTick";
    private static final String NBT_SELF_DRYING_PENALTY = "EC_SelfDryingPenalty";

    private static final Random RANDOM = new Random();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamageFireCounterReduction(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity().getPersistentData().getBooleanOr(ScorchedHandler.NBT_FIRE_COUNTER_INVULN, false)) {
            float reduction = (float) ElementalFireNatureReactionsConfig.fireCounterDamageReduction;
            event.setNewDamage(event.getNewDamage() * (1.0f - reduction));
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity target = event.getEntity();

        DamageSource source = event.getSource();
        float currentDamage = event.getNewDamage();

        if (source.is(ModDamageTypes.LAVA_MAGIC)) {
            return;
        }

        if (source.typeHolder().unwrapKey().isPresent()) {
            ResourceKey<DamageType> typeKey = source.typeHolder().unwrapKey().get();
            if ("irons_spellbooks".equals(typeKey.identifier().getNamespace())
                    && typeKey.identifier().getPath().endsWith("_magic")) {
                return;
            }
        }

        Entity directEntity0 = source.getDirectEntity();
        if (directEntity0 != null && directEntity0.getClass().getName()
                .equals("io.redspace.ironsspellbooks.entity.spells.poison_cloud.PoisonCloud")) {
            return;
        }

        Holder<MobEffect> sporeEffect = ModMobEffects.SPORES;
        float originalPhysicalDamage = currentDamage;

        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        ItemStack weaponStack = ItemStack.EMPTY;
        Entity directEntity = source.getDirectEntity();

        if (directEntity instanceof Projectile projectile && directEntity != attacker) {
            weaponStack = ElementUtils.getProjectileWeaponStack(projectile, attacker);
        } else if (ElementUtils.getConsistentAttackElement(attacker) != ElementType.NONE) {
            weaponStack = attacker.getMainHandItem();
        }

        ElementType attackElement = ElementUtils.getAttackElement(weaponStack);
        if (attackElement != ElementType.NONE) {
            if (ElementUtils.getDisplayEnhancement(attacker, attackElement) <= 0) {
                attackElement = ElementType.NONE;
            }
        }
        if (!weaponStack.isEmpty() && directEntity == attacker
                && (weaponStack.getItem() instanceof net.minecraft.world.item.BowItem
                 || weaponStack.getItem() instanceof net.minecraft.world.item.CrossbowItem)) {
            return;
        }
        if (attackElement == ElementType.NONE) {
            return;
        }

        boolean selfDryingSpawnedSteam = false;
        float sporeVulnMult = 1.0f;
        if (attackElement == ElementType.FIRE && sporeEffect != null && target.hasEffect(sporeEffect)) {
            net.minecraft.world.effect.MobEffectInstance spore = target.getEffect(sporeEffect);
            int stacks = (spore != null) ? (spore.getAmplifier() + 1) : 0;
            if (stacks > 0) {
                float vulnPerStack = (float) ElementalFireNatureReactionsConfig.sporeFireVulnPerStack;
                sporeVulnMult = 1.0f + (stacks * vulnPerStack);
            }
        }

        if (attackElement == ElementType.FIRE) {
            CompoundTag attackerData = attacker.getPersistentData();
            int attackerWetness = WetnessHandler.getWetnessLevel(attacker);
            if (attackerWetness > 0) {
                long currentTick = attacker.level().getGameTime();
                long lastDryTick = attackerData.getLongOr(NBT_LAST_DRY_TICK, 0L);
                if (currentTick != lastDryTick) {
                    int firePower = ElementUtils.getDisplayEnhancement(attacker, ElementType.FIRE);
                    int threshold = Math.max(1, ElementalFireNatureReactionsConfig.wetnessDryingThreshold);
                    int layersToRemove = firePower / threshold;
                    if (layersToRemove > 0) {
                        int newLevel = Math.max(0, attackerWetness - layersToRemove);
                        int actuallyRemoved = attackerWetness - newLevel;
                        WetnessHandler.updateWetnessLevel(attacker, newLevel);
                        attackerData.putLong(NBT_LAST_DRY_TICK, currentTick);
                        Holder<MobEffect> wetnessEffect = ModMobEffects.WETNESS;
                        if (newLevel == 0 && wetnessEffect != null && attacker.hasEffect(wetnessEffect)) {
                            attacker.removeEffect(wetnessEffect);
                        }
                        int maxBurstLevel = ElementalFireNatureReactionsConfig.steamHighHeatMaxLevel;
                        EffectHelper.playSteamBurst((ServerLevel) attacker.level(), attacker, 0.5f, Math.min(layersToRemove, maxBurstLevel), true);
                        int spawnedSteamLevel = 0;
                        if (ElementalFireNatureReactionsConfig.steamLowHeatMaxLevel > 0) {
                            if (firePower < ElementalFireNatureReactionsConfig.steamLowHeatTriggerThreshold) {
                                DebugCommand.sendReactionFailed(attacker, "steam", "power_low",
                                        attacker.getDisplayName(),
                                        firePower,
                                        ElementalFireNatureReactionsConfig.steamLowHeatTriggerThreshold);
                            } else if (SteamReactionHandler.isOnSteamCooldown(attacker)) {
                                long remaining = DebugCommand.getRemainingCooldown(attacker, SteamReactionHandler.NBT_STEAM_ATTACKER_COOLDOWN);
                                DebugCommand.sendReactionCooldownBlock(attacker, "steam", remaining);
                            } else {
                                int steamLevel = Math.max(1, Math.min(actuallyRemoved, ElementalFireNatureReactionsConfig.steamLowHeatMaxLevel));
                                SteamReactionHandler.spawnSteamCloud(target, false, steamLevel);
                                SteamReactionHandler.applySteamCooldown(attacker, SteamReactionHandler.computeCloudDuration(false, steamLevel));
                                spawnedSteamLevel = steamLevel;
                                selfDryingSpawnedSteam = true;
                                DebugCommand.sendSteamCloudCombinedLog(
                                        target, attacker, false, steamLevel,
                                        0f, 1f,
                                        (float) ElementalFireNatureReactionsConfig.steamCondensationRadius,
                                        SteamReactionHandler.computeCloudDuration(false, steamLevel),
                                        ElementalFireNatureReactionsConfig.steamCloudHeightCeiling,
                                        ElementalFireNatureReactionsConfig.steamClearAggro,
                                        ElementType.NONE, 1f, false, 0f);
                            }
                        }
                        attackerData.putInt(NBT_SELF_DRYING_PENALTY, 1);

                        DebugCommand.DryLogContext dryCtx = new DebugCommand.DryLogContext();
                        dryCtx.entity = attacker;
                        dryCtx.oldLevel = attackerWetness;
                        dryCtx.newLevel = newLevel;
                        dryCtx.removedLayers = actuallyRemoved;
                        dryCtx.firePower = firePower;
                        dryCtx.steamLevel = spawnedSteamLevel;
                        DebugCommand.sendDryLog(dryCtx);
                    }
                } else {
                    attackerData.putInt(NBT_SELF_DRYING_PENALTY, 1);
                }
            }
        }

        float physicalDamage = currentDamage;
        int enhancementPoints = ElementUtils.getDisplayEnhancement(attacker, attackElement);
        int resistancePoints = ElementUtils.getDisplayResistance(target, attackElement);

        float frozenMeltMult = 1.0f;
        boolean frozenMelted = false;
        if (attackElement == ElementType.FIRE && ElementalThunderFrostReactionsConfig.frostbiteFireSteamThreshold > 0 && FrostbiteHandler.isFrozen(target)) {
            int required = ElementalThunderFrostReactionsConfig.frostbiteFireSteamThreshold;
            frozenMeltMult = (float) ElementalThunderFrostReactionsConfig.fireFrostMeltDamageMult;
            if (enhancementPoints >= required) {
                frozenMelted = true;
            }
        }

        int strengthPerHalfDamage = ElementalConfig.getStrengthPerHalfDamage();
        int resistPerHalfReduction = ElementalConfig.getResistPerHalfReduction();

        float baseEnhancementDamage = enhancementPoints / (float) strengthPerHalfDamage * 0.5f;
        float baseResistReduction = resistancePoints / (float) resistPerHalfReduction * 0.5f;

        if (baseEnhancementDamage <= 0.0f) {
            return;
        }

        int wetnessLevel = WetnessHandler.getWetnessLevel(target);
        if (wetnessLevel <= 0) {
            String prefix = "EC_WetnessSnapshot_";
            for (String tag : target.entityTags()) {
                if (tag.startsWith(prefix)) {
                    try {
                        wetnessLevel = Integer.parseInt(tag.substring(prefix.length()));
                        target.removeTag(tag);
                    } catch (NumberFormatException ignored) {}
                    break;
                }
            }
        }

        float wetnessBaseMult = 1.0f;
        if (wetnessLevel > 0 && attackElement == ElementType.FIRE) {
            float reductionPerLevel = (float) ElementalFireNatureReactionsConfig.wetnessFireReduction;
            float finalReduction = wetnessLevel * reductionPerLevel;
            wetnessBaseMult = 1.0f - finalReduction;
        }

        CompoundTag attackerData = attacker.getPersistentData();
        float selfDryingPenaltyMult = 1.0f;
        if (attackerData.getIntOr(NBT_SELF_DRYING_PENALTY, 0) != 0 && attackElement == ElementType.FIRE) {
            selfDryingPenaltyMult = 1.0f - (float) ElementalFireNatureReactionsConfig.wetnessSelfDryingDamagePenalty;
            attackerData.putInt(NBT_SELF_DRYING_PENALTY, 0);
        }

        float combinedWetnessMult = wetnessBaseMult * selfDryingPenaltyMult;

        ElementType targetDominant = ElementUtils.getConsistentAttackElement(target);
        float restraintMult = ElementalConfig.getRestraintMultiplier(attackElement, targetDominant);

        float globalDamageMult = (float) ElementalConfig.elementalDamageMultiplier;
        float globalResistMult = (float) ElementalConfig.elementalResistanceMultiplier;

        float scorchVulnMult = 1.0f;
        ElementType scorchedElement = null;
        if (ScorchedHandler.isScorched(target)
                && attackElement != ElementType.NONE) {
            switch (attackElement) {
                case FROST -> { scorchVulnMult = (float) ElementalFireNatureReactionsConfig.scorchedFrostDmgMultiplier; scorchedElement = ElementType.FROST; }
                case NATURE -> { scorchVulnMult = (float) ElementalFireNatureReactionsConfig.scorchedNatureDmgMultiplier; scorchedElement = ElementType.NATURE; }
                case FIRE -> { scorchVulnMult = (float) ElementalFireNatureReactionsConfig.scorchedFireDmgMultiplier; scorchedElement = ElementType.FIRE; }
                case THUNDER -> { scorchVulnMult = (float) ElementalFireNatureReactionsConfig.scorchedThunderDmgMultiplier; scorchedElement = ElementType.THUNDER; }
                case NONE -> {}
            }
        }

        float attackPart = baseEnhancementDamage * globalDamageMult * combinedWetnessMult * restraintMult * sporeVulnMult * scorchVulnMult * frozenMeltMult;

        float finalElementalDmg;
        boolean isFloored = false;
        double minPercent = ElementalConfig.restraintMinDamagePercent;
        float benchmark = (float) ElementalConfig.getMaxStatCap();

        if (restraintMult > 1.0f && resistancePoints >= benchmark) {
            float resistRatio = Math.min(resistancePoints / benchmark, 1.0f);
            double maxReductionAllowed = 1.0 - minPercent;
            double actualReduction = resistRatio * maxReductionAllowed;
            finalElementalDmg = attackPart * (float) (1.0 - actualReduction);
            isFloored = true;
        } else {
            float defensePart = baseResistReduction * globalResistMult;
            finalElementalDmg = Math.max(0.0f, attackPart - defensePart);
        }

        float remainingAbsorption = target.getAbsorptionAmount();
        if (remainingAbsorption > 0.0f && finalElementalDmg > 0.0f) {
            float absorbed = Math.min(remainingAbsorption, finalElementalDmg);
            target.setAbsorptionAmount(remainingAbsorption - absorbed);
            finalElementalDmg -= absorbed;
        }

        float totalDamage = physicalDamage + finalElementalDmg;
        event.setNewDamage(totalDamage);

        DebugCommand.CombatLogContext combatCtx = new DebugCommand.CombatLogContext();
        combatCtx.attacker = attacker;
        combatCtx.target = target;
        combatCtx.directEntity = directEntity;
        combatCtx.originalPhysicalDamage = originalPhysicalDamage;
        combatCtx.physicalDamage = physicalDamage;
        combatCtx.attackElement = attackElement;
        combatCtx.attackerEnhancement = enhancementPoints;
        combatCtx.targetResistance = resistancePoints;
        combatCtx.baseEnhancementDamage = baseEnhancementDamage;
        combatCtx.globalDamageMult = globalDamageMult;
        combatCtx.restraintMult = restraintMult;
        combatCtx.sporeVulnMult = sporeVulnMult;
        combatCtx.scorchVulnMult = scorchVulnMult;
        combatCtx.scorchedElement = scorchedElement;
        combatCtx.wetnessBaseMult = wetnessBaseMult;
        combatCtx.selfDryingPenaltyMult = selfDryingPenaltyMult;
        combatCtx.combinedWetnessMult = combinedWetnessMult;
        combatCtx.baseResistReduction = baseResistReduction;
        combatCtx.globalResistMult = globalResistMult;
        combatCtx.finalElemDmg = finalElementalDmg;
        combatCtx.totalDamage = totalDamage;
        combatCtx.isFloored = isFloored;
        combatCtx.minPercent = minPercent;
        combatCtx.wetnessLevel = wetnessLevel;
        combatCtx.frozenMeltMult = frozenMeltMult;
        combatCtx.frozenMelted = frozenMelted;
        DebugCommand.sendCombatLog(combatCtx);

        if (attackElement == ElementType.FIRE) {
            if (frozenMelted) {
                if (!selfDryingSpawnedSteam) {
                    applyFireFreezeMelt(target, attacker, enhancementPoints);
                }
            } else {
                tryTriggerScorched(attacker, target, enhancementPoints, selfDryingSpawnedSteam);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        DamageSource source = event.getSource();
        Entity attackerEntity = source.getEntity();
        if (attackerEntity instanceof LivingEntity attacker) {
            Holder<MobEffect> paralysisEffect = ModMobEffects.PARALYSIS;
            if (paralysisEffect != null && attacker.hasEffect(paralysisEffect)) {
                event.setCanceled(true);
            }
            if (FrostbiteHandler.isFrozen(attacker) && !event.getSource().is(ModDamageTypes.FROSTBITE_THERMAL_SHOCK)) {
                event.setCanceled(true);
            }
        }
    }

    private static void tryTriggerScorched(LivingEntity attacker, LivingEntity target, int firePower, boolean selfDryingSpawnedSteam) {
        if (selfDryingSpawnedSteam) return;
        if (ElementalFireNatureReactionsConfig.scorchedTriggerThreshold <= 0) return;
        if (firePower < ElementalFireNatureReactionsConfig.scorchedTriggerThreshold) {
            DebugCommand.sendReactionFailed(target, "scorched", "power_low",
                    attacker.getDisplayName(),
                    target.getDisplayName(),
                    Component.literal(String.valueOf(firePower)).withStyle(ChatFormatting.RED),
                    Component.literal(String.valueOf(ElementalFireNatureReactionsConfig.scorchedTriggerThreshold)).withStyle(ChatFormatting.GOLD));
            return;
        }
        Holder<MobEffect> wetnessEffect = ModMobEffects.WETNESS;
        if (wetnessEffect != null && (target.hasEffect(wetnessEffect) || attacker.hasEffect(wetnessEffect))) {
            DebugCommand.sendReactionFailed(target, "scorched", "wet",
                    attacker.getDisplayName(),
                    target.getDisplayName());
            return;
        }
        if (ScorchedHandler.isScorched(target)) {
            DebugCommand.sendReactionFailed(target, "scorched", "already",
                    attacker.getDisplayName(),
                    target.getDisplayName());
            return;
        }
        boolean hasPoison = target.hasEffect(net.minecraft.world.effect.MobEffects.POISON);
        Holder<MobEffect> sporeEffect2 = ModMobEffects.SPORES;
        boolean hasSpores = sporeEffect2 != null && target.hasEffect(sporeEffect2);
        if (!hasSpores) {
            CompoundTag attackerData = attacker.getPersistentData();
            long gameTime = target.level().getGameTime();
            if (attackerData.contains(ScorchedHandler.NBT_ATTACKER_SCORCHED_COOLDOWN)) {
                long cd = attackerData.getLongOr(ScorchedHandler.NBT_ATTACKER_SCORCHED_COOLDOWN, 0L);
                if (gameTime < cd) {
                    DebugCommand.sendReactionCooldownBlock(attacker, "scorched_attack", cd - gameTime);
                    return;
                }
            }
        }

        double baseChance = ElementalFireNatureReactionsConfig.scorchedBaseChance;
        int pointsPerStep = ElementalFireNatureReactionsConfig.scorchedChancePerPoint;
        int threshold = ElementalFireNatureReactionsConfig.scorchedTriggerThreshold;
        int scalingSteps = Math.max(0, (int)Math.floor((firePower - threshold) / (double) pointsPerStep));
        double scalingChance = 0.05;
        double growth = scalingSteps * scalingChance;
        double totalChance = Math.min(1.0, Math.max(0.0, baseChance + growth));

        double biomeBonus = 0;
        String biomeTag = "";
        if (target.level().canSeeSky(target.blockPosition())) {
            var biome = target.level().getBiome(target.blockPosition()).value();
            double temp = biome.getBaseTemperature();
            if (temp >= 2.0) {
                biomeBonus = ElementalFireNatureReactionsConfig.scorchedHotBiomeChanceBonus;
                biomeTag = Component.translatable("debug.elementalcraft.reaction.biome.hot").getString();
            } else if (temp <= 0.3) {
                biomeBonus = -ElementalFireNatureReactionsConfig.scorchedColdBiomeChancePenalty;
                biomeTag = Component.translatable("debug.elementalcraft.reaction.biome.cold").getString();
            }
            totalChance += biomeBonus;
            totalChance = Math.min(1.0, Math.max(0.0, totalChance));
        }

        boolean triggered;
        if (hasPoison || hasSpores) {
            totalChance = 1.0;
            triggered = true;
        } else {
            triggered = RANDOM.nextDouble() < totalChance;
        }

        if (triggered) {
            int duration = ElementalFireNatureReactionsConfig.scorchedDuration;
            ScorchedHandler.ScorchedApplyResult result = ScorchedHandler.applyScorched(target, attacker, firePower, duration, firePower, 1.0f, false);
            if (result != ScorchedHandler.ScorchedApplyResult.FAILED) {
                ScorchedHandler.igniteCreeperIfScorched(target);

                target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
                String durationInfo;
                double durationSec = duration / 20.0;
                double adjustedSec = result.adjustedDuration / 20.0;
                if (result.targetElement != ElementType.NONE && result.multiplier != 1.0f) {
                    durationInfo = Component.translatable("debug.elementalcraft.reaction.scorched.duration_element_enhanced",
                            String.format("%.1f", durationSec), String.format("%.1f", adjustedSec),
                            result.targetElement.getDisplayName(), String.format("%.1f", result.multiplier)).getString();
                } else {
                    durationInfo = Component.translatable("debug.elementalcraft.reaction.scorched.duration_seconds",
                            String.format("%.1f", adjustedSec)).getString();
                }
                if (hasPoison) {
                    double enhancedSec = (int)(duration * ElementalFireNatureReactionsConfig.poisonScorchDurationMultiplier) / 20.0;
                    durationInfo = Component.translatable("debug.elementalcraft.reaction.scorched.duration_poison_enhanced",
                            String.format("%.1f", durationSec), String.format("%.1f", enhancedSec)).getString()
                            + "(" + Component.translatable("effect.minecraft.poison").getString() + ")";
                }
                float baseDamage = ScorchedHandler.calculateScorchedDamage(firePower, target);
                DebugCommand.sendScorchedSuccess(target, attacker, firePower, baseChance, scalingSteps, scalingChance, biomeBonus, biomeTag, (int)(totalChance * 100), durationInfo, String.format("%.1f", baseDamage));
            }
        } else if (totalChance > 0.01) {
            DebugCommand.sendScorchedChanceFailed(attacker, target, firePower, baseChance, scalingSteps, scalingChance, biomeBonus, biomeTag, (int)(totalChance * 100));
        }
    }

    public static void applyFireFreezeMelt(LivingEntity target, LivingEntity attacker, int firePower) {
        if (target.level().isClientSide()) return;
        MobEffectInstance freezeEffect = target.getEffect(ModMobEffects.FREEZE);
        int frozenStacks = (freezeEffect != null) ? freezeEffect.getAmplifier() + 1 : 1;
        int ratio = ElementalThunderFrostReactionsConfig.fireFrostMeltWetnessRatio;
        int newWetness = (ratio > 0) ? Math.min(frozenStacks * ratio, ElementalFireNatureReactionsConfig.wetnessMaxLevel) : 0;

        target.removeEffect(ModMobEffects.FREEZE);
        CompoundTag data = target.getPersistentData();
        data.remove(FrostbiteHandler.NBT_FREEZE_STACKS);
        data.remove(FrostbiteHandler.NBT_FROZEN_FROSTBITE_STACKS);
        data.putLong(FrostbiteHandler.NBT_FREEZE_COOLDOWN,
                target.level().getGameTime() + ElementalThunderFrostReactionsConfig.freezeCooldownTicks);

        FrostbiteHandler.clearFrostbite(target);
        ScorchedHandler.clearScorched(target);
        SteamReactionHandler.discardFrostedCloudsNear(target);

        if (newWetness > 0) {
            WetnessHandler.updateWetnessLevel(target, newWetness);
            data.putFloat(WetnessHandler.NBT_DECAY_PROGRESS, 0);
            SteamReactionHandler.spawnSteamCloud(target, true, newWetness);
            WetnessHandler.clearWetnessData(target);
        }

        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 1, target.getZ(), 15, 0.5, 0.5, 0.5, 0.05);
        }
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.0f);
        DebugCommand.sendFireFreezeMeltLog(target, attacker, frozenStacks, newWetness, firePower, ElementalThunderFrostReactionsConfig.frostbiteFireSteamThreshold);
        data.putBoolean("EC_FireFrostMeltResolved", true);
    }
}
