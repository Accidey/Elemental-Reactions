package com.xulai.elementalcraft.client;

import com.google.common.reflect.TypeToken;
import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public final class LivingEntityRenderStateData {

    public static final ContextKey<LivingEntity> ENTITY = new ContextKey<>(
            Identifier.fromNamespaceAndPath(ElementalCraft.MODID, "rendered_entity"));

    private LivingEntityRenderStateData() {
    }

    @SubscribeEvent
    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {},
                (entity, state) -> state.setRenderData(ENTITY, entity));
    }
}
