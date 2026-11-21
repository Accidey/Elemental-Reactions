package com.xulai.elementalcraft.event;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class PotionStackSyncHandler {

    private static final int STACK_LIMIT = 64;
    private static final int SYNC_INTERVAL = 100;

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncInventory(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % SYNC_INTERVAL != 0) return;
        syncInventory(player);
    }

    private static void syncInventory(Player player) {
        boolean enabled = ElementalConfig.potionStack64Enabled;
        applyTo(player.getInventory().getNonEquipmentItems(), enabled);
    }

    private static void applyTo(Iterable<ItemStack> stacks, boolean enabled) {
        for (ItemStack stack : stacks) {
            if (stack.isEmpty() || !isPotion(stack)) continue;
            Integer current = stack.get(DataComponents.MAX_STACK_SIZE);
            if (enabled) {
                if (current == null || current != STACK_LIMIT) {
                    stack.set(DataComponents.MAX_STACK_SIZE, STACK_LIMIT);
                }
            } else if (stack.getCount() <= 1 && (current == null || current != 1)) {
                stack.set(DataComponents.MAX_STACK_SIZE, 1);
            }
        }
    }

    private static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }
}
