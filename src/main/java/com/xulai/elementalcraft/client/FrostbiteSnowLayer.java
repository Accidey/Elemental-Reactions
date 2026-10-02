package com.xulai.elementalcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.potion.ModMobEffects;
import com.xulai.elementalcraft.util.MobEffectLookup;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public class FrostbiteSnowLayer {

    private static final Identifier SNOW_TEXTURE = Identifier.withDefaultNamespace("textures/block/powder_snow.png");

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?, ?> event) {
        LivingEntity entity = event.getRenderState().getRenderData(LivingEntityRenderStateData.ENTITY);
        if (entity == null) return;

        if (MobEffectLookup.hasEffect(entity, ModMobEffects.FREEZE)) return;

        MobEffectInstance frostbite = MobEffectLookup.getEffect(entity, ModMobEffects.FROSTBITE);
        if (frostbite == null) return;
        int stacks = frostbite.getAmplifier() + 1;
        if (stacks <= 0) return;
        int maxStacks = ElementalThunderFrostReactionsConfig.frostbiteMaxTotalStacks;
        if (maxStacks <= 0) maxStacks = 5;

        float coverage = Math.min(1.0f, (float) stacks / maxStacks);
        float hw = entity.getBbWidth() / 2.0f + 0.2f;
        float hd = hw;
        float top = entity.getBbHeight() * coverage;
        float bot = -0.1f;
        float alpha = 1.0f;
        float uvTop = coverage;

        int light = event.getRenderState().lightCoords;
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        PoseStack poseStack = event.getPoseStack();

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(SNOW_TEXTURE),
                (PoseStack.Pose pose, VertexConsumer consumer) -> {
            consumer.addVertex(pose, -hw, bot, hd).setColor(1, 1, 1, alpha).setUv(0, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose, -hw, top, hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose,  hw, top, hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose,  hw, bot, hd).setColor(1, 1, 1, alpha).setUv(1, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);

            consumer.addVertex(pose,  hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(0, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
            consumer.addVertex(pose,  hw, top, -hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
            consumer.addVertex(pose, -hw, top, -hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
            consumer.addVertex(pose, -hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(1, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);

            consumer.addVertex(pose, -hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(0, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
            consumer.addVertex(pose, -hw, top, -hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
            consumer.addVertex(pose, -hw, top,  hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
            consumer.addVertex(pose, -hw, bot,  hd).setColor(1, 1, 1, alpha).setUv(1, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);

            consumer.addVertex(pose, hw, bot,  hd).setColor(1, 1, 1, alpha).setUv(0, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
            consumer.addVertex(pose, hw, top,  hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
            consumer.addVertex(pose, hw, top, -hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
            consumer.addVertex(pose, hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(1, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);

            consumer.addVertex(pose, -hw, top,  hd).setColor(1, 1, 1, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, -hw, top, -hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose,  hw, top, -hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose,  hw, top,  hd).setColor(1, 1, 1, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);

            consumer.addVertex(pose, -hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
            consumer.addVertex(pose,  hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
            consumer.addVertex(pose,  hw, bot,  hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
            consumer.addVertex(pose, -hw, bot,  hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
        });
    }
}
