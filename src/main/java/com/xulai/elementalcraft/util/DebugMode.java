package com.xulai.elementalcraft.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DebugMode {

    private static final Map<UUID, Boolean> DEBUG_PLAYERS = new ConcurrentHashMap<>();

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

    public static boolean hasRelevantDebugger(LivingEntity contextEntity) {
        if (contextEntity == null) return false;
        for (UUID id : DEBUG_PLAYERS.keySet()) {
            if (isRelated(contextEntity, id)) return true;
        }
        return false;
    }

    private static boolean isRelated(LivingEntity contextEntity, UUID playerId) {
        if (contextEntity.getUUID().equals(playerId)) return true;
        if (contextEntity instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            if (target != null && target.getUUID().equals(playerId)) return true;
        }
        if (contextEntity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Player player = serverLevel.getPlayerByUUID(playerId);
            if (player != null) {
                LivingEntity lastAttacker = player.getLastHurtByMob();
                if (lastAttacker != null && lastAttacker.getUUID().equals(contextEntity.getUUID())) return true;
            }
        }
        return false;
    }
}
