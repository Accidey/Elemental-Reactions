package com.xulai.elementalcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.potion.ModMobEffects;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public class FrozenIceLayer {

    @SuppressWarnings("removal")
    private static final Identifier ICE_TEXTURE = Identifier.withDefaultNamespace("textures/block/packed_ice.png");

    private static final Map<UUID, Boolean> freezeCache = new HashMap<>();

    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().value() == ModMobEffects.FREEZE.get()) {
            freezeCache.put(event.getEntity().getUUID(), true);
        }
    }

    @SubscribeEvent
    public static void onMobEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect().value() == ModMobEffects.FREEZE.get()) {
            freezeCache.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onMobEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().value() == ModMobEffects.FREEZE.get()) {
            freezeCache.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?, ?> event) {
        LivingEntity entity = event.getRenderState().getRenderData(LivingEntityRenderStateData.ENTITY);
        if (entity == null) return;

        boolean hasFreezeEffect = freezeCache.containsKey(entity.getUUID());

        if (!hasFreezeEffect) return;

        float alpha = calcAlpha(entity);
        if (alpha <= 0.01f) return;

        int packedLight = event.getRenderState().lightCoords;
        renderIceBox(event.getPoseStack(), event.getSubmitNodeCollector(), packedLight, entity, alpha);
    }

    private static float calcAlpha(LivingEntity entity) {
        if (!freezeCache.containsKey(entity.getUUID())) return 0;
        MobEffectInstance effect = entity.getEffect(ModMobEffects.FREEZE);
        if (effect != null) {
            int dur = effect.getDuration();
            if (dur < 20) return 1.0f * (dur / 20.0f);
        }
        return 1.0f;
    }

    private static void renderIceBox(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                                     LivingEntity entity, float alpha) {
        float hw = entity.getBbWidth() / 2.0f + 0.15f;
        float hd = hw;
        float top = entity.getBbHeight() + 0.15f;
        float bot = -0.15f;

        RenderType renderType = RenderTypes.entityTranslucent(ICE_TEXTURE);

        collector.submitCustomGeometry(poseStack, renderType, (PoseStack.Pose pose, VertexConsumer consumer) -> {

            consumer.addVertex(pose, -hw, bot, hd).setColor(1, 1, 1, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose, -hw, top, hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose,  hw, top, hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose,  hw, bot, hd).setColor(1, 1, 1, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);

consumer.addVertex(pose,  hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, -1);
            consumer.addVertex(pose,  hw, top, -hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, -1);
            consumer.addVertex(pose, -hw, top, -hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, -1);
            consumer.addVertex(pose, -hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, -1);

consumer.addVertex(pose, -hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, -1, 0, 0);
            consumer.addVertex(pose, -hw, top, -hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, -1, 0, 0);
            consumer.addVertex(pose, -hw, top,  hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, -1, 0, 0);
            consumer.addVertex(pose, -hw, bot,  hd).setColor(1, 1, 1, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, -1, 0, 0);

consumer.addVertex(pose, hw, bot,  hd).setColor(1, 1, 1, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 1, 0, 0);
            consumer.addVertex(pose, hw, top,  hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 1, 0, 0);
            consumer.addVertex(pose, hw, top, -hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 1, 0, 0);
            consumer.addVertex(pose, hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 1, 0, 0);

consumer.addVertex(pose, -hw, top,  hd).setColor(1, 1, 1, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, -hw, top, -hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose,  hw, top, -hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose,  hw, top,  hd).setColor(1, 1, 1, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 1, 0);

consumer.addVertex(pose, -hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, -1, 0);
            consumer.addVertex(pose,  hw, bot, -hd).setColor(1, 1, 1, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, -1, 0);
            consumer.addVertex(pose,  hw, bot,  hd).setColor(1, 1, 1, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, -1, 0);
            consumer.addVertex(pose, -hw, bot,  hd).setColor(1, 1, 1, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, -1, 0);
        });
    }
}
