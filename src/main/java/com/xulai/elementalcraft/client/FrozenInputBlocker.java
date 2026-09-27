package com.xulai.elementalcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.potion.ModMobEffects;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

@EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public class FrozenInputBlocker {

    public static volatile boolean fireCounterLocked;

    private static final Set<String> MOVEMENT_KEYS = Set.of(
            "key.forward", "key.left", "key.back", "key.right", "key.jump");

    private static final Set<String> ALLOWED_KEYS = Set.of(
            "key.chat", "key.command");

    private static boolean isAllowedKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_ESCAPE
            || keyCode == GLFW.GLFW_KEY_F2
            || keyCode == GLFW.GLFW_KEY_F3
            || keyCode == GLFW.GLFW_KEY_F5;
    }

    private static boolean isAllowedKeyName(String name) {
        return ALLOWED_KEYS.contains(name);
    }

    private static boolean isAffected(Minecraft mc) {
        return mc.player != null
            && (mc.player.hasEffect(ModMobEffects.FREEZE)
             || mc.player.hasEffect(ModMobEffects.PARALYSIS));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        boolean isFullLock = isAffected(mc);
        if (!isFullLock && !fireCounterLocked) return;

        for (var key : mc.options.keyMappings) {
            if (isAllowedKey(key.getKey().getValue())) continue;
            if (isAllowedKeyName(key.getName())) continue;
            if (isFullLock) {
                key.setDown(false);
                while (key.consumeClick()) {}
            } else if (fireCounterLocked && MOVEMENT_KEYS.contains(key.getName())) {
                key.setDown(false);
                while (key.consumeClick()) {}
            }
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (!isAffected(mc)) return;

        for (var key : mc.options.keyMappings) {
            var boundKey = key.getKey();
            if (boundKey.getType() == InputConstants.Type.MOUSE && boundKey.getValue() == event.getButton()) {
                key.setDown(false);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (isAffected(mc)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        fireCounterLocked = false;
    }
}
