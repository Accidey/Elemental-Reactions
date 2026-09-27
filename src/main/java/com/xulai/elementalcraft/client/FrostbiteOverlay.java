package com.xulai.elementalcraft.client;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalThunderFrostReactionsConfig;
import com.xulai.elementalcraft.potion.ModMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public class FrostbiteOverlay {

    private static final Identifier POWDER_SNOW_OUTLINE = Identifier.withDefaultNamespace("textures/misc/powder_snow_outline.png");
    private static float currentDisplayAlpha = 0.0f;

    @SubscribeEvent
    public static void onRenderGuiLayerPost(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS)) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator()) return;

        boolean isFrozen = player.hasEffect(ModMobEffects.FREEZE);
        MobEffectInstance frostbiteEffect = player.getEffect(ModMobEffects.FROSTBITE);
        float targetAlpha;
        if (isFrozen) {
            targetAlpha = 1.0f;
        } else if (frostbiteEffect != null) {
            int stacks = frostbiteEffect.getAmplifier() + 1;
            int maxStacks = ElementalThunderFrostReactionsConfig.frostbiteMaxTotalStacks;
            if (maxStacks <= 0) maxStacks = 1;
            targetAlpha = (float) stacks / (float) maxStacks;
        } else {
            targetAlpha = 0.0f;
        }

        float lerpSpeed = 0.05f;
        if (targetAlpha > currentDisplayAlpha) {
            currentDisplayAlpha = Math.min(targetAlpha, currentDisplayAlpha + lerpSpeed);
        } else if (targetAlpha < currentDisplayAlpha) {
            currentDisplayAlpha = Math.max(targetAlpha, currentDisplayAlpha - lerpSpeed);
        }

        if (currentDisplayAlpha <= 0.01f) return;

        GuiGraphicsExtractor guiGraphics = event.getGuiGraphics();

        int color = ARGB.white(currentDisplayAlpha);
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                POWDER_SNOW_OUTLINE,
                0,
                0,
                0.0F,
                0.0F,
                guiGraphics.guiWidth(),
                guiGraphics.guiHeight(),
                guiGraphics.guiWidth(),
                guiGraphics.guiHeight(),
                color
        );
    }
}
