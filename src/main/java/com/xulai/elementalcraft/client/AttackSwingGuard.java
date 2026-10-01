package com.xulai.elementalcraft.client;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = ElementalCraft.MODID, value = Dist.CLIENT)
public final class AttackSwingGuard {

    private static final int ATTACK_CLICK_WINDOW_TICKS = 3;
    private static final long NEVER = Long.MIN_VALUE;

    private static long clientTicks = 0;
    private static long lastAttackClickTick = NEVER;

    private AttackSwingGuard() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        clientTicks++;
    }

    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            lastAttackClickTick = clientTicks;
        }
    }

    public static boolean isAttackSwing(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (entity != mc.player) return true;
        if (mc.options.keyAttack.isDown()) return true;
        return clientTicks - lastAttackClickTick <= ATTACK_CLICK_WINDOW_TICKS;
    }
}
