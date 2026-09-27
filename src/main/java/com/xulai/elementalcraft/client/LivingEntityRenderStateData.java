package com.xulai.elementalcraft.client;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.client.model.EntityModel;
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        Class<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, EntityModel<LivingEntityRenderState>>> rendererClass =
                (Class) LivingEntityRenderer.class;
        event.registerEntityModifier(rendererClass, (entity, state) -> state.setRenderData(ENTITY, entity));
    }
}
