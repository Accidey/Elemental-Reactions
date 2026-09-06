package com.xulai.elementalcraft.sound;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, ElementalCraft.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRIC_ZAP =
            SOUND_EVENTS.register("electric_zap",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ElementalCraft.MODID, "electric_zap")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SPORE_GAIN =
            SOUND_EVENTS.register("spore_gain",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ElementalCraft.MODID, "spore_gain")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FREEZING =
            SOUND_EVENTS.register("freezing",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ElementalCraft.MODID, "freezing")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private ModSounds() {}
}
