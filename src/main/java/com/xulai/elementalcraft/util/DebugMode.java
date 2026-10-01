package com.xulai.elementalcraft.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "elementalcraft")
public class DebugMode {

    private static final Map<UUID, Boolean> DEBUG_PLAYERS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        DEBUG_PLAYERS.remove(event.getEntity().getUUID());
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

    public static boolean isRelatedTo(LivingEntity contextEntity, Player player) {
        if (contextEntity == null || player == null) return false;
        if (contextEntity.getUUID().equals(player.getUUID())) return true;
        if (contextEntity instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            if (target != null && target.getUUID().equals(player.getUUID())) return true;
        }
        LivingEntity lastAttacker = player.getLastHurtByMob();
        return lastAttacker != null && lastAttacker.getUUID().equals(contextEntity.getUUID());
    }

    public static boolean hasViewer(LivingEntity contextEntity) {
        if (DEBUG_PLAYERS.isEmpty() || contextEntity == null) return false;
        if (!(contextEntity.level() instanceof ServerLevel level)) return false;
        MinecraftServer server = level.getServer();
        if (server == null) return false;
        for (UUID id : DEBUG_PLAYERS.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null && isRelatedTo(contextEntity, player)) return true;
        }
        return false;
    }

    public static List<ServerPlayer> viewersOf(LivingEntity contextEntity) {
        List<ServerPlayer> viewers = new ArrayList<>();
        if (DEBUG_PLAYERS.isEmpty() || contextEntity == null) return viewers;
        if (!(contextEntity.level() instanceof ServerLevel level)) return viewers;
        MinecraftServer server = level.getServer();
        if (server == null) return viewers;
        for (UUID id : DEBUG_PLAYERS.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null && isRelatedTo(contextEntity, player)) viewers.add(player);
        }
        return viewers;
    }
}