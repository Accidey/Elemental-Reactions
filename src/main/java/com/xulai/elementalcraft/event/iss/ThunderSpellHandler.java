package com.xulai.elementalcraft.event.iss;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.config.ElementalISSIntegrationConfig;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.enchantment.ModEnchantments;
import com.xulai.elementalcraft.event.ReactionHandler;
import com.xulai.elementalcraft.event.StaticShockHandler;
import com.xulai.elementalcraft.event.SteamReactionHandler;
import com.xulai.elementalcraft.event.WetnessHandler;
import com.xulai.elementalcraft.init.ModDamageTypes;
import com.xulai.elementalcraft.potion.ModMobEffects;
import com.xulai.elementalcraft.sound.ModSounds;
import com.xulai.elementalcraft.util.ElementDamageHelper;
import com.xulai.elementalcraft.util.ElementType;
import com.xulai.elementalcraft.util.ElementUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class ThunderSpellHandler {

    private static final java.util.Set<java.util.UUID> ELECTROCUTING_CASTERS = new java.util.HashSet<>();

    private static final String NBT_ISS_ACTIVE = "EC_ISS_Active";
    private static final String NBT_ISS_DAMAGE = "EC_ISS_Damage";
    private static final String NBT_ISS_ATTACKER = "EC_ISS_Attacker";
    private static final String NBT_ISS_MAINHAND_ENCH = "EC_ISS_MainhandEnch";
    private static final String NBT_ISS_OFFHAND_ENCH = "EC_ISS_OffhandEnch";
    private static final String NBT_ISS_PARALYSIS_CD = "EC_ISS_ParalysisCooldown";

    private static final Map<ResourceKey<Level>, ISSElectrifiedWater> ACTIVE_WATERS = new HashMap<>();

    private record ISSElectrifiedWater(
            BlockPos center, double range, long startTick, int duration,
            float recordedDamage, Set<UUID> damagedEntities, long lastParticleTick
    ) {}

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        var source = event.getSource();
        if (!(source.getDirectEntity() instanceof LightningBolt)) return;

        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (!target.hasEffect(ModMobEffects.WETNESS)) return;

        if (ISSCore.isImmuneToParalysis(target)) return;
        if (target.getPersistentData().getInt("ec_paralysis_cooldown_timer") > 0) return;

        int maxStacks = ElementalThunderFrostReactionsConfig.staticMaxTotalStacks;
        int duration = maxStacks * ElementalThunderFrostReactionsConfig.staticDurationPerStackTicks;

        CompoundTag data = target.getPersistentData();
        int current = data.getInt("ec_static_stacks");
        if (current >= maxStacks) return;

        if (StaticShockHandler.blockStaticIfParalyzed(target)) return;

        data.putInt("ec_static_stacks", maxStacks);
        data.putInt("ec_static_timer", duration);

        target.addEffect(new MobEffectInstance(
                ModMobEffects.STATIC_SHOCK, duration, maxStacks - 1, false, false, true
        ));

        if (target.isInWater()
                && ElementalThunderFrostReactionsConfig.waterElectrificationRangeBase > 0) {
            int pDuration = ElementalThunderFrostReactionsConfig.waterElectrificationParalysisDuration;
            if (!StaticShockHandler.tryTriggerWaterElectrification(target, pDuration)) {
                data.remove("ec_static_stacks");
                data.remove("ec_static_timer");
                target.removeEffect(ModMobEffects.STATIC_SHOCK);
                WetnessHandler.clearWetnessData(target);
                target.addEffect(new MobEffectInstance(
                        ModMobEffects.PARALYSIS, pDuration, 0, false, false, true
                ));
            }
            return;
        }

        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        WetnessHandler.resolveElementReactionConflict(target, attacker);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurtSpellDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!ISSCore.ISS_LOADED) return;
        if (!isLightningSpellDamage(event.getSource())) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        double threshold = ElementalThunderFrostReactionsConfig.thunderStrengthThreshold;
        if (threshold <= 0) return;
        int thunderEnhancement = ElementUtils.getDisplayEnhancement(attacker, ElementType.THUNDER);
        int stacks = (int) (thunderEnhancement / threshold);
        if (stacks <= 0) return;

        event.getEntity().getPersistentData().putInt("EC_ISS_DiffStacks", stacks);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onElectrocuteRefreshParalysis(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;
        if (!ISSCore.ISS_LOADED) return;
        if (!isLightningSpellDamage(event.getSource())) return;
        LivingEntity target = event.getEntity();
        if (!target.hasEffect(ModMobEffects.PARALYSIS)) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if ("SCROLL".equals(attacker.getPersistentData().getString("EC_LastCastSource"))
                && attacker.getPersistentData().getLong(ISSCore.NBT_ISS_REFRESH_CD) > target.level().getGameTime()) return;
        CompoundTag data = target.getPersistentData();
        int stacks = Math.max(1, data.getInt("ec_paralysis_stacks"));
        int duration = stacks * ElementalThunderFrostReactionsConfig.paralysisDurationPerStackTicks;
        if (duration < 20) duration = 20;
        target.removeEffect(ModMobEffects.PARALYSIS);
        target.addEffect(new MobEffectInstance(
                ModMobEffects.PARALYSIS, duration, stacks - 1, false, false, true));
        data.putInt("ec_paralysis_stacks", stacks);
        ELECTROCUTING_CASTERS.add(attacker.getUUID());
        if ("SCROLL".equals(attacker.getPersistentData().getString("EC_LastCastSource"))) {
            attacker.getPersistentData().putLong(ISSCore.NBT_ISS_REFRESH_CD,
                    target.level().getGameTime() + ElementalThunderFrostReactionsConfig.paralysisCooldownTicks);
        }
    }

    @SubscribeEvent
    public static void onElectrocuteEndTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        
        if (!ISSCore.ISS_LOADED) return;
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (event.getLevel().getGameTime() % 20 != 0) return;
        if (ELECTROCUTING_CASTERS.isEmpty()) return;
        Iterable<ServerLevel> allLevels = event.getLevel().getServer().getAllLevels();
        java.util.Set<java.util.UUID> active = new java.util.HashSet<>();
        for (ServerLevel sl : allLevels) {
            for (Entity e : sl.getEntities().getAll()) {
                if (!e.getClass().getName().equals("io.redspace.ironsspellbooks.entity.spells.electrocute.ElectrocuteProjectile")) continue;
                Entity owner = e instanceof net.minecraft.world.entity.projectile.Projectile p ? p.getOwner() : null;
                if (owner != null) active.add(owner.getUUID());
            }
        }
        long now = event.getLevel().getGameTime();
        int cd = ElementalThunderFrostReactionsConfig.paralysisCooldownTicks;
        for (java.util.UUID uid : ELECTROCUTING_CASTERS) {
            if (active.contains(uid)) continue;
            for (ServerLevel sl : allLevels) {
                Entity caster = sl.getEntity(uid);
                if (caster instanceof LivingEntity living) {
                    living.getPersistentData().putLong(NBT_ISS_PARALYSIS_CD, now + cd);
                    break;
                }
            }
        }
        ELECTROCUTING_CASTERS.retainAll(active);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamageHighest(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;
        if (!isLightningSpellDamage(event.getSource())) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        if (attacker instanceof Mob mob && mob.getPersistentData().getBoolean(ISSCore.NBT_MOB_CASTER)) {
            mob.getPersistentData().putBoolean("EC_ISS_SpellHit", true);

            CompoundTag data = event.getEntity().getPersistentData();
            saveAndClearEnchantments(attacker.level(), attacker.getMainHandItem(), data, NBT_ISS_MAINHAND_ENCH);
            saveAndClearEnchantments(attacker.level(), attacker.getOffhandItem(), data, NBT_ISS_OFFHAND_ENCH);
            data.putBoolean(NBT_ISS_ACTIVE, true);
            data.putUUID(NBT_ISS_ATTACKER, attacker.getUUID());
            if (ElementUtils.getDisplayEnhancement(attacker, ElementType.THUNDER) > 0) {
                data.putFloat(NBT_ISS_DAMAGE, event.getNewDamage());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamageLowest(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;
        CompoundTag data = event.getEntity().getPersistentData();
        if (!data.getBoolean(NBT_ISS_ACTIVE)) return;

        data.remove(NBT_ISS_ACTIVE);

        restoreAttackerEnchantments(event.getEntity(), data);

        if (!data.contains(NBT_ISS_DAMAGE)) {
            return;
        }

        float spellDamage = data.getFloat(NBT_ISS_DAMAGE);
        data.remove(NBT_ISS_DAMAGE);

        LivingEntity target = event.getEntity();
        if (target.isDeadOrDying()) return;

        if (target.hasEffect(ModMobEffects.WETNESS)) {
            triggerParalysis(target);
        }

        if (ISSCore.isInOrOnWater(target)) {
            spawnElectrifiedWater(target, spellDamage);
        }

        if (target.hasEffect(ModMobEffects.SPORES)) {
            tryTriggerSporeBlast(target);
        }

        if (SteamReactionHandler.isInCondensingCloud(target)) {
            if (chargeSteamCloud(target)) {
                applyCondensingCloudParalysis(target);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        
        if (!ISSCore.ISS_LOADED) return;

        ResourceKey<Level> dim = event.getLevel().dimension();
        ISSElectrifiedWater water = ACTIVE_WATERS.get(dim);
        if (water == null) return;

        long gameTime = event.getLevel().getGameTime();

        if (gameTime - water.startTick >= water.duration) {
            ACTIVE_WATERS.remove(dim);
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        AABB area = new AABB(
                water.center.getX() - water.range, water.center.getY() - water.range, water.center.getZ() - water.range,
                water.center.getX() + water.range, water.center.getY() + water.range, water.center.getZ() + water.range
        );

        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
            if (water.damagedEntities.contains(entity.getUUID())) continue;
            if (entity instanceof Player p && p.isCreative()) continue;
            if (entity.isDeadOrDying()) continue;
            if (!ISSCore.isInOrOnWater(entity)) continue;
            if (ISSCore.isImmuneToParalysis(entity)) continue;

            water.damagedEntities.add(entity.getUUID());

            if (water.duration > 0 && ElementalThunderFrostReactionsConfig.paralysisMaxStacks > 0) {
                entity.addEffect(new MobEffectInstance(
                        ModMobEffects.PARALYSIS, water.duration, 0, false, false, true));
            }

            if (water.recordedDamage > 0) {
                ElementDamageHelper.applyDamage(entity, water.recordedDamage,
                        ModDamageTypes.source(serverLevel, ModDamageTypes.STATIC_SHOCK));
            }
        }
    }

    static void handleReaction(LivingEntity target, LivingEntity attacker, DamageSource source, int spellStacks) {
        int thunderPower = ElementUtils.getDisplayEnhancement(attacker, ElementType.THUNDER);
        double threshold = ElementalThunderFrostReactionsConfig.thunderStrengthThreshold;
        if (threshold <= 0 || thunderPower < threshold) return;
        if (StaticShockHandler.blockStaticIfParalyzed(target)) return;
        double baseChance = ElementalThunderFrostReactionsConfig.staticBaseChance;
        double scalingStep = ElementalThunderFrostReactionsConfig.staticScalingStep;
        double scalingChance = ElementalThunderFrostReactionsConfig.staticScalingChance;
        int scalingSteps = (scalingStep > 0 && thunderPower >= scalingStep)
                ? (int) ((thunderPower - scalingStep) / scalingStep) : 0;
        double chance = Math.min(1.0, baseChance + scalingSteps * scalingChance);
        if (ISSCore.RANDOM.nextDouble() < chance) {
            int step = (int) Math.max(1, ElementalThunderFrostReactionsConfig.staticScalingStep);
            int eStacks = 1 + (thunderPower - (int) threshold) / step;
            int stacksToAdd = spellStacks > 0 ? Math.min(spellStacks, eStacks) : eStacks;
            int maxStacks = ElementalThunderFrostReactionsConfig.staticMaxTotalStacks;
            CompoundTag data = target.getPersistentData();
            int currentStacks = data.getInt(StaticShockHandler.NBT_STATIC_STACKS);
            int newStacks = Math.min(maxStacks, currentStacks + stacksToAdd);
            int duration = newStacks * ElementalThunderFrostReactionsConfig.staticDurationPerStackTicks;
            data.putInt(StaticShockHandler.NBT_STATIC_STACKS, newStacks);
            data.putInt("ec_static_timer", duration);
            if (target.hasEffect(ModMobEffects.STATIC_SHOCK)) {
                target.removeEffect(ModMobEffects.STATIC_SHOCK);
            }
            target.addEffect(new MobEffectInstance(
                    ModMobEffects.STATIC_SHOCK, duration, newStacks - 1, false, false, true));
            com.xulai.elementalcraft.command.DebugCommand.sendStaticShockSuccess(attacker, target, stacksToAdd,
                    ElementType.THUNDER, thunderPower, baseChance, scalingSteps, scalingChance, 0, 0, 0, chance, false);
            if (ISSCore.isInOrOnWater(target)
                    && ElementalThunderFrostReactionsConfig.waterElectrificationRangeBase > 0
                    && !(attacker instanceof Mob mobCaster && mobCaster.getPersistentData().getBoolean(ISSCore.NBT_MOB_CASTER))) {
                int stacks = data.getInt(StaticShockHandler.NBT_STATIC_STACKS);
                if (stacks > 0 && StaticShockHandler.triggerWaterElectrificationDirect(target, stacks)) {
                    return;
                }
                long cd = StaticShockHandler.getWaterElectrificationCooldown(target.level());
                if (cd > 0) {
                    com.xulai.elementalcraft.command.DebugCommand.sendReactionCooldownBlock(target, "water_electrification", cd);
                }
            }
            data.remove(WetnessHandler.NBT_REACTION_RESOLVED);
            if (target.hasEffect(ModMobEffects.WETNESS)) {
                WetnessHandler.resolveElementReactionConflict(target, attacker);
            }
            if (target.hasEffect(ModMobEffects.SPORES)) {
                StaticShockHandler.tryTriggerSporeBlast(target);
            }
        } else {
            com.xulai.elementalcraft.command.DebugCommand.sendReactionFailed(attacker, "static_shock", "chance",
                    attacker.getDisplayName(), target.getDisplayName(),
                    "",
                    Component.literal(String.format("%.0f", chance * 100)).withStyle(ChatFormatting.YELLOW));
        }
    }

    static void onMobTick(Mob mob, CompoundTag data) {
        long gameTime = mob.level().getGameTime();

        if (mob.getHealth() / mob.getMaxHealth() < ElementalISSIntegrationConfig.mobLowHealthThreshold) {
            ISSCore.tryAggressiveCast(mob, data, gameTime);
            return;
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return;

        if (ISSCore.isImmuneToParalysis(target)) {
            if (gameTime >= data.getLong(ISSCore.NBT_MOB_CAST_CD)) {
                ISSCore.castSpell(mob, target, false);
                data.putLong(ISSCore.NBT_MOB_CAST_CD, gameTime + ElementalISSIntegrationConfig.mobNormalCastCooldown);
            }
            return;
        }

        boolean netherImmune = ElementalFireNatureReactionsConfig.wetnessNetherDimensionImmune
                && mob.level().dimension() == Level.NETHER;

        long pendingCast = data.getLong("EC_ISS_PendingCast");
        if (pendingCast > 0 && gameTime >= pendingCast) {
            if (data.contains("EC_ISS_SpellHit")) {
                data.remove("EC_ISS_PendingCast");
                data.remove("EC_ISS_SpellHit");
                data.remove("EC_ISS_PendingIsSpell");
                return;
            }
            if (data.contains("EC_ISS_PendingIsSpell")) {
                data.remove("EC_ISS_PendingCast");
                data.remove("EC_ISS_PendingIsSpell");
                int missCount = data.getInt("EC_ISS_MissCount") + 1;
                if (missCount >= 2) {
                    data.remove("EC_ISS_MissCount");
                    data.putLong(ISSCore.NBT_MOB_CAST_CD, gameTime + ElementalISSIntegrationConfig.mobNormalCastCooldown);
                } else {
                    data.putInt("EC_ISS_MissCount", missCount);
                    data.putLong(ISSCore.NBT_MOB_CAST_CD, 0);
                }
                return;
            }
            if (!ISSCore.tryThrowWaterBottle(mob, data, target, gameTime)) return;
            ISSCore.castSpell(mob, target, false);
            data.putBoolean("EC_ISS_PendingIsSpell", true);
            data.putLong("EC_ISS_PendingCast", gameTime + 40);
            data.putLong(ISSCore.NBT_MOB_CAST_CD, gameTime + ElementalISSIntegrationConfig.mobNormalCastCooldown);
            data.putInt("EC_ISS_MissCount", 0);
            return;
        }
        if (pendingCast > 0) return;

        if (netherImmune || (ElementalFireNatureReactionsConfig.wetnessMaxLevel > 0
                && WetnessHandler.getWetnessLevel(target) >= ElementalFireNatureReactionsConfig.wetnessMaxLevel)) {
            if (data.getLong(ISSCore.NBT_MOB_CAST_CD) == 0 || gameTime >= data.getLong(ISSCore.NBT_MOB_CAST_CD)) {
                ISSCore.castSpell(mob, target, false);
                data.putBoolean("EC_ISS_PendingIsSpell", true);
                data.putLong("EC_ISS_PendingCast", gameTime + 40);
                data.putLong(ISSCore.NBT_MOB_CAST_CD, gameTime + ElementalISSIntegrationConfig.mobNormalCastCooldown);
            }
            return;
        }

        if (!ISSCore.tryThrowWaterBottle(mob, data, target, gameTime)) return;
        ISSCore.castSpell(mob, target, false);
        data.putBoolean("EC_ISS_PendingIsSpell", true);
        data.putLong("EC_ISS_PendingCast", gameTime + 40);
        data.putLong(ISSCore.NBT_MOB_CAST_CD, gameTime + ElementalISSIntegrationConfig.mobNormalCastCooldown);
        data.putInt("EC_ISS_MissCount", 0);
    }

    static void equipSlots(Mob mob, ItemStack scrollStack, String spellId) {
        mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        mob.setItemSlot(EquipmentSlot.MAINHAND, scrollStack);
        mob.setDropChance(EquipmentSlot.MAINHAND, ElementalISSIntegrationConfig.scrollDropChance);
        scrollStack.enchant(ModEnchantments.THUNDER_STRIKE.get(), 1);

        boolean netherImmune = ElementalFireNatureReactionsConfig.wetnessNetherDimensionImmune
                && mob.level().dimension() == Level.NETHER;
        if (!netherImmune) {
            ItemStack waterBottle = new ItemStack(Items.SPLASH_POTION);
            waterBottle.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
            mob.setItemSlot(EquipmentSlot.OFFHAND, waterBottle);
            mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        }
    }

    private static void triggerParalysis(LivingEntity target) {
        if (ISSCore.isImmuneToParalysis(target)) {
            target.removeEffect(ModMobEffects.WETNESS);
            WetnessHandler.clearWetnessData(target);
            return;
        }

        CompoundTag data = target.getPersistentData();
        long gameTime = target.level().getGameTime();
        if (gameTime < data.getLong(NBT_ISS_PARALYSIS_CD)) {
            target.removeEffect(ModMobEffects.WETNESS);
            WetnessHandler.clearWetnessData(target);
            return;
        }

        int wetnessLevel = target.hasEffect(ModMobEffects.WETNESS)
                ? target.getEffect(ModMobEffects.WETNESS).getAmplifier() + 1 : 1;
        int diffStacks = data.getInt("EC_ISS_DiffStacks");
        data.remove("EC_ISS_DiffStacks");
        int maxParalysis = ElementalThunderFrostReactionsConfig.paralysisMaxStacks;
        if (maxParalysis <= 0) {
            target.removeEffect(ModMobEffects.WETNESS);
            WetnessHandler.clearWetnessData(target);
            return;
        }

        int paralysisStacks = Math.min(Math.max(wetnessLevel, diffStacks), maxParalysis);
        int duration = ElementalThunderFrostReactionsConfig.paralysisDurationPerStackTicks * paralysisStacks;

        target.addEffect(new MobEffectInstance(
                ModMobEffects.PARALYSIS, duration, paralysisStacks - 1, false, false, true));

        target.removeEffect(ModMobEffects.WETNESS);
        WetnessHandler.clearWetnessData(target);

        int cooldownTicks = ElementalThunderFrostReactionsConfig.paralysisCooldownTicks;
        data.putLong(NBT_ISS_PARALYSIS_CD, gameTime + duration + cooldownTicks);

        if (!target.level().isClientSide) {
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    ModSounds.ELECTRIC_ZAP.get(), SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }

    private static void spawnElectrifiedWater(LivingEntity target, float recordedDamage) {
        if (ElementalThunderFrostReactionsConfig.waterElectrificationRangeBase <= 0) return;
        if (recordedDamage <= 0) return;

        ResourceKey<Level> dim = target.level().dimension();
        ISSElectrifiedWater existing = ACTIVE_WATERS.get(dim);
        long gameTime = target.level().getGameTime();
        if (existing != null && gameTime - existing.startTick < existing.duration) return;

        int stacks = ElementalThunderFrostReactionsConfig.staticMaxTotalStacks;
        double range = ElementalThunderFrostReactionsConfig.waterElectrificationRangeBase
                + (stacks - 1) * ElementalThunderFrostReactionsConfig.waterElectrificationRangePerStack;

        int duration = ElementalThunderFrostReactionsConfig.waterElectrificationParalysisDuration;

        ISSElectrifiedWater water = new ISSElectrifiedWater(
                target.blockPosition(), range, gameTime, duration,
                recordedDamage, new HashSet<>(), 0
        );
        water.damagedEntities().add(target.getUUID());

        if (target.level() instanceof ServerLevel sl) {
            AABB area = new AABB(
                    target.getX() - range, target.getY() - range, target.getZ() - range,
                    target.getX() + range, target.getY() + range, target.getZ() + range
            );

            for (LivingEntity entity : sl.getEntitiesOfClass(LivingEntity.class, area)) {
                if (entity == target) continue;
                if (entity instanceof Player p && p.isCreative()) continue;
                if (entity.isDeadOrDying()) continue;
                if (!ISSCore.isInOrOnWater(entity)) continue;
                if (ISSCore.isImmuneToParalysis(entity)) continue;

                water.damagedEntities().add(entity.getUUID());

                if (duration > 0 && ElementalThunderFrostReactionsConfig.paralysisMaxStacks > 0) {
                    entity.addEffect(new MobEffectInstance(
                            ModMobEffects.PARALYSIS, duration, 0, false, false, true));
                }

                if (recordedDamage > 0) {
                    ElementDamageHelper.applyDamage(entity, recordedDamage,
                            ModDamageTypes.source(sl, ModDamageTypes.STATIC_SHOCK));
                }
            }
        }

        ACTIVE_WATERS.put(dim, water);
    }

    private static void tryTriggerSporeBlast(LivingEntity target) {
        MobEffectInstance sporeEffect = target.getEffect(ModMobEffects.SPORES);
        if (sporeEffect == null) return;
        int sporeStacks = sporeEffect.getAmplifier() + 1;

        if (ElementalThunderFrostReactionsConfig.staticSporeBlastBaseChance <= 0) return;

        int staticStacks = ElementalThunderFrostReactionsConfig.staticMaxTotalStacks;
        double chance = Math.min(1.0,
                ElementalThunderFrostReactionsConfig.staticSporeBlastBaseChance
                        + staticStacks * ElementalThunderFrostReactionsConfig.staticSporeBlastPerStaticStack
                        + sporeStacks * ElementalThunderFrostReactionsConfig.staticSporeBlastPerSporeStack);
        chance = ReactionHandler.applySporeBiomeModifier(target, chance);
        if (ISSCore.RANDOM.nextDouble() >= chance) return;

        ReactionHandler.triggerStaticSporeBlast(target,
                ElementalFireNatureReactionsConfig.scorchedTriggerThreshold);
    }

    private static void saveAndClearEnchantments(Level level, ItemStack stack, CompoundTag data, String key) {
        if (stack.isEmpty()) return;
        ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
        if (ench == null || ench.isEmpty()) return;
        var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        ItemEnchantments.CODEC.encodeStart(ops, ench).result().ifPresent(tag -> {
            data.put(key, tag);
            stack.remove(DataComponents.ENCHANTMENTS);
        });
    }

    private static void restoreAttackerEnchantments(LivingEntity target, CompoundTag data) {
        if (!data.contains(NBT_ISS_ATTACKER + "Most")) return;
        UUID attackerId = data.getUUID(NBT_ISS_ATTACKER);
        data.remove(NBT_ISS_ATTACKER + "Most");
        data.remove(NBT_ISS_ATTACKER + "Least");

        Entity attackerEntity = target.level().getPlayerByUUID(attackerId);
        if (attackerEntity == null && target.level() instanceof ServerLevel serverLevel) {
            attackerEntity = serverLevel.getEntity(attackerId);
        }
        if (!(attackerEntity instanceof LivingEntity attacker)) return;

        restoreEnchantments(attacker.level(), attacker.getMainHandItem(), data, NBT_ISS_MAINHAND_ENCH);
        restoreEnchantments(attacker.level(), attacker.getOffhandItem(), data, NBT_ISS_OFFHAND_ENCH);
    }

    private static void restoreEnchantments(Level level, ItemStack stack, CompoundTag data, String key) {
        if (stack.isEmpty()) return;
        if (!data.contains(key)) return;
        var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        ItemEnchantments.CODEC.parse(ops, data.get(key)).result().ifPresent(ench -> {
            stack.set(DataComponents.ENCHANTMENTS, ench);
            data.remove(key);
        });
    }

    private static boolean chargeSteamCloud(LivingEntity target) {
        if (target.level().isClientSide) return false;
        if (ElementalThunderFrostReactionsConfig.staticSteamCloudTriggerStacks <= 0) return false;

        double searchRadius = ElementalFireNatureReactionsConfig.steamCloudRadius * 3.0;
        AABB box = target.getBoundingBox().inflate(searchRadius);
        boolean charged = false;

        for (AreaEffectCloud cloud : target.level().getEntitiesOfClass(AreaEffectCloud.class, box,
                c -> c.getTags().contains(SteamReactionHandler.TAG_STEAM_CLOUD)
                        && !c.getTags().contains(SteamReactionHandler.TAG_HIGH_HEAT)
                        && !c.getTags().contains(SteamReactionHandler.TAG_STATIC_CHARGED))) {
            if (!isEntityInSteamCloud(target, cloud)) continue;
            cloud.addTag(SteamReactionHandler.TAG_STATIC_CHARGED);
            charged = true;
        }

        if (charged) {
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.5f, 1.2f);
        }
        return charged;
    }

    private static void applyCondensingCloudParalysis(LivingEntity target) {
        if (target.level().isClientSide) return;
        if (ElementalThunderFrostReactionsConfig.paralysisMaxStacks <= 0) return;

        String entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
        if (ElementalConfig.matchesBlacklist(ElementalThunderFrostReactionsConfig.cachedParalysisImmunityBlacklist, entityId)) return;

        double searchRadius = ElementalFireNatureReactionsConfig.steamCloudRadius * 3.0;
        AABB box = target.getBoundingBox().inflate(searchRadius);
        int paralysisDuration = 0;

        for (AreaEffectCloud cloud : target.level().getEntitiesOfClass(AreaEffectCloud.class, box,
                c -> c.getTags().contains(SteamReactionHandler.TAG_STEAM_CLOUD)
                        && c.getTags().contains(SteamReactionHandler.TAG_STATIC_CHARGED))) {
            if (!isEntityInSteamCloud(target, cloud)) continue;
            paralysisDuration = cloud.getDuration();
            break;
        }

        if (paralysisDuration <= 0) return;

        target.addEffect(new MobEffectInstance(
                ModMobEffects.PARALYSIS, paralysisDuration, 2, false, false, true
        ));
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                ModSounds.ELECTRIC_ZAP.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    private static boolean isEntityInSteamCloud(LivingEntity entity, AreaEffectCloud cloud) {
        if (cloud.getBoundingBox().inflate(0.1).intersects(entity.getBoundingBox())) {
            return true;
        }
        double heightCeiling = ElementalFireNatureReactionsConfig.steamCloudHeightCeiling;
        double dx = entity.getX() - cloud.getX();
        double dz = entity.getZ() - cloud.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double dy = entity.getY() - cloud.getY();
        return hDist <= cloud.getRadius() && dy >= 0 && dy <= heightCeiling;
    }

    static boolean isLightningSpellDamage(DamageSource source) {
        if (!ISSCore.ISS_LOADED) return false;
        return source.is(ISSCore.ISS_LIGHTNING_MAGIC);
    }
}
