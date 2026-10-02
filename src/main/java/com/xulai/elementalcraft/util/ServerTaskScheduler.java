package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public final class ServerTaskScheduler {

    private record ScheduledTask(MinecraftServer server, long runAtTick, Runnable task) {
    }

    private static final List<ScheduledTask> TASKS = new CopyOnWriteArrayList<>();

    private ServerTaskScheduler() {
    }

    public static void schedule(MinecraftServer server, int delayTicks, Runnable task) {
        TASKS.add(new ScheduledTask(server, server.getTickCount() + Math.max(1, delayTicks), task));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TASKS.clear();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (TASKS.isEmpty()) return;
        MinecraftServer server = event.getServer();
        List<ScheduledTask> toRemove = new ArrayList<>();
        for (ScheduledTask task : TASKS) {
            if (task.server() != server) {
                if (!task.server().isRunning()) toRemove.add(task);
                continue;
            }
            if (server.getTickCount() >= task.runAtTick()) {
                toRemove.add(task);
                try {
                    task.task().run();
                } catch (Exception e) {
                    ElementalCraft.LOGGER.error("[ElementalCraft] Scheduled server task failed", e);
                }
            }
        }
        if (!toRemove.isEmpty()) {
            TASKS.removeAll(toRemove);
        }
    }
}
