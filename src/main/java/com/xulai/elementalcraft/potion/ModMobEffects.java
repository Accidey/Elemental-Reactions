package com.xulai.elementalcraft.potion;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MOB_EFFECT, ElementalCraft.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> WETNESS =
            MOB_EFFECTS.register("wetness", WetnessMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> SPORES =
            MOB_EFFECTS.register("flammable_spores", FlammableSporesEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> STATIC_SHOCK =
            MOB_EFFECTS.register("static_shock", StaticShockEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> PARALYSIS =
            MOB_EFFECTS.register("paralysis", ParalysisEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> FROSTBITE =
            MOB_EFFECTS.register("frostbite", FrostbiteEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> FREEZE =
            MOB_EFFECTS.register("freeze", FreezeEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

    private ModMobEffects() {}
}
