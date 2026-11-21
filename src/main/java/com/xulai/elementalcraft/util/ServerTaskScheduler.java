package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public final class ServerTaskScheduler {

    private record ScheduledTask(MinecraftServer server, long runAtTick, Runnable task) {
    }

    private static final Queue<ScheduledTask> TASKS = new ConcurrentLinkedQueue<>();

    private ServerTaskScheduler() {
    }

    public static void schedule(MinecraftServer server, int delayTicks, Runnable task) {
        TASKS.add(new ScheduledTask(server, server.getTickCount() + Math.max(1, delayTicks), task));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (TASKS.isEmpty()) return;
        MinecraftServer server = event.getServer();
        int pending = TASKS.size();
        for (int i = 0; i < pending; i++) {
            ScheduledTask task = TASKS.poll();
            if (task == null) return;
            if (task.server() != server) {
                if (task.server().isRunning()) {
                    TASKS.add(task);
                }
                continue;
            }
            if (server.getTickCount() >= task.runAtTick()) {
                runTask(task);
            } else {
                TASKS.add(task);
            }
        }
    }

    private static void runTask(ScheduledTask task) {
        try {
            task.task().run();
        } catch (Exception e) {
            ElementalCraft.LOGGER.error("[ElementalCraft] Scheduled task failed", e);
        }
    }
}
