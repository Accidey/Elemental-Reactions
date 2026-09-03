package com.xulai.elementalcraft.event.cataclysm;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ElementalFireNatureReactionsConfig;
import com.xulai.elementalcraft.event.FrostbiteHandler;
import com.xulai.elementalcraft.event.ScorchedHandler;
import com.xulai.elementalcraft.event.StaticShockHandler;
import com.xulai.elementalcraft.event.WetnessHandler;
import com.xulai.elementalcraft.potion.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ElementalCraft.MODID)
public class CataclysmWetnessBridge {

    public static final boolean CATA_LOADED;

    static {
        boolean loaded = false;
        try {
            loaded = ModList.get() != null && ModList.get().isLoaded("cataclysm");
        } catch (Exception e) {
            loaded = false;
        }
        CATA_LOADED = loaded;
    }

    static final String NBT_CATA_WET_COUNTER = "EC_CataclysmWetnessLevel";

    private static final float LIGHTNING_BOOST_PER_LEVEL = 0.2F;
    private static final int LIGHTNING_BOOST_MAX_LEVEL = 5;

    private static MobEffect cachedCataclysmWetness;
    private static boolean cacheAttempted;

    private CataclysmWetnessBridge() {
    }

    private static MobEffect getCataclysmWetness() {
        if (!CATA_LOADED || cacheAttempted) return cachedCataclysmWetness;
        cacheAttempted = true;
        cachedCataclysmWetness = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cataclysm", "wetness"));
        return cachedCataclysmWetness;
    }

    private static boolean active() {
        return CATA_LOADED && ElementalFireNatureReactionsConfig.wetnessMaxLevel > 0;
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!active()) return;
        MobEffectInstance instance = event.getEffectInstance();
        MobEffect catWet = getCataclysmWetness();
        if (catWet == null || instance == null || instance.getEffect() != catWet) return;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        event.setResult(Event.Result.DENY);
        applyAsExternal(target, instance.getAmplifier(), instance.getDuration());
    }

    private static void applyAsExternal(LivingEntity target, int amplifier, int durationTicks) {
        CompoundTag data = target.getPersistentData();
        int counter = data.getInt(NBT_CATA_WET_COUNTER);
        int cap = Math.min(5, ElementalFireNatureReactionsConfig.wetnessMaxLevel);
        int fromEvent = Math.min(amplifier + 1, cap);
        int stacked = Math.min(counter + 1, cap);
        int newLevel = Math.max(fromEvent, stacked);
        newLevel = Math.max(newLevel, WetnessHandler.getWetnessLevel(target));
        newLevel = Math.max(1, Math.min(newLevel, ElementalFireNatureReactionsConfig.wetnessMaxLevel));
        data.putInt(NBT_CATA_WET_COUNTER, newLevel);
        tryApplyWetness(target, newLevel, durationTicks);
    }

    private static boolean tryApplyWetness(LivingEntity entity, int level, int durationTicks) {
        if (ElementalFireNatureReactionsConfig.wetnessMaxLevel <= 0) return false;
        if (entity.level().isClientSide) return false;
        if (ScorchedHandler.isScorched(entity)) return false;
        if (isImmune(entity)) return false;
        if (entity.hasEffect(ModMobEffects.PARALYSIS.get()) || FrostbiteHandler.isFrozen(entity)) return false;

        int clampedLevel = Math.min(level, ElementalFireNatureReactionsConfig.wetnessMaxLevel);
        if (clampedLevel <= 0) return false;

        CompoundTag data = entity.getPersistentData();

        if (ModMobEffects.SPORES.isPresent() && entity.hasEffect(ModMobEffects.SPORES.get())) {
            boolean hasStatic = data.getInt(StaticShockHandler.NBT_STATIC_STACKS) > 0;
            boolean hasFrostbite = FrostbiteHandler.isFrozen(entity) || FrostbiteHandler.isTempFrostbite(entity);
            if (!hasStatic && !hasFrostbite) {
                WetnessHandler.convertWetnessToSpores(entity);
                return false;
            }
        }

        int baseTime = Math.max(1, ElementalFireNatureReactionsConfig.wetnessDecayBaseTime);
        int threshold = clampedLevel * baseTime;
        float progress = Math.max(0, threshold - Math.max(20, durationTicks) / 20.0F);
        data.putFloat(WetnessHandler.NBT_DECAY_PROGRESS, progress);
        data.putInt(WetnessHandler.NBT_RAIN_TIMER, 0);
        WetnessHandler.updateWetnessLevel(entity, clampedLevel);

        int effectDuration = isPaused(entity) ? 24000 : Math.max(5, durationTicks);
        entity.addEffect(new MobEffectInstance(
                ModMobEffects.WETNESS.get(), effectDuration, clampedLevel - 1, true, false, true));
        return true;
    }

    private static boolean isPaused(LivingEntity entity) {
        if (entity.isInWater()) return true;
        BlockPos pos = entity.blockPosition();
        Level level = entity.level();
        if (level.isRainingAt(pos)) return true;
        if (level.isRaining() && level.canSeeSky(pos)) {
            var biome = level.getBiome(pos).value();
            if (biome != null && biome.getPrecipitationAt(pos) == Biome.Precipitation.SNOW) return true;
        }
        return false;
    }

    private static boolean isImmune(LivingEntity entity) {
        if (ElementalFireNatureReactionsConfig.wetnessWaterAnimalImmune && entity instanceof WaterAnimal) {
            return true;
        }
        if (ElementalFireNatureReactionsConfig.wetnessNetherDimensionImmune
                && entity.level().dimension() == Level.NETHER) {
            return true;
        }
        if (!ElementalFireNatureReactionsConfig.cachedWetnessBlacklist.isEmpty()) {
            var key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            if (key != null && ElementalConfig.matchesBlacklist(
                    ElementalFireNatureReactionsConfig.cachedWetnessBlacklist, key.toString())) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!active()) return;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (event.isCanceled()) return;
        if (!event.getSource().is(DamageTypeTags.IS_LIGHTNING)) return;

        MobEffect catWet = getCataclysmWetness();
        if (catWet != null && target.hasEffect(catWet)) return;

        int level = WetnessHandler.getWetnessLevel(target);
        if (level <= 0) return;
        int effective = Math.min(level, LIGHTNING_BOOST_MAX_LEVEL);
        if (effective <= 0 || LIGHTNING_BOOST_PER_LEVEL <= 0) return;
        event.setAmount(Math.min(Float.MAX_VALUE,
                event.getAmount() * (1.0F + effective * LIGHTNING_BOOST_PER_LEVEL)));
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!active()) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (entity.tickCount % 20 != 0) return;

        CompoundTag data = entity.getPersistentData();
        MobEffect catWet = getCataclysmWetness();
        if (catWet != null) {
            MobEffectInstance residual = entity.getEffect(catWet);
            if (residual != null) {
                entity.removeEffect(catWet);
                applyAsExternal(entity, residual.getAmplifier(), residual.getDuration());
            }
        }
        if (WetnessHandler.getWetnessLevel(entity) <= 0) {
            data.remove(NBT_CATA_WET_COUNTER);
        }
    }
}
