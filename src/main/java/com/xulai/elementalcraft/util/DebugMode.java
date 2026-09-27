package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class DebugMode {

    private static final Map<UUID, Boolean> DEBUG_PLAYERS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        remove(event.getEntity());
    }

    public static void setEnabled(Player player, boolean enabled) {
        if (enabled) {
            DEBUG_PLAYERS.put(player.getUUID(), true);
        } else {
            DEBUG_PLAYERS.remove(player.getUUID());
        }
    }

    public static void remove(Player player) {
        if (player != null) {
            DEBUG_PLAYERS.remove(player.getUUID());
        }
    }

    public static boolean isEnabled(Player player) {
        return player != null && DEBUG_PLAYERS.containsKey(player.getUUID());
    }

    public static void toggle(Player player) {
        setEnabled(player, !isEnabled(player));
    }

    public static boolean hasAnyDebugEnabled() {
        return !DEBUG_PLAYERS.isEmpty();
    }
}