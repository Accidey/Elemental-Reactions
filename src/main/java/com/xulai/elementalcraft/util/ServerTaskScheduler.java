package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public final class ServerTaskScheduler {

    private record ScheduledTask(MinecraftServer server, long runAtTick, Runnable task) {
    }

    private static final List<ScheduledTask> TASKS = new ArrayList<>();

    private ServerTaskScheduler() {
    }

    public static void schedule(MinecraftServer server, int delayTicks, Runnable task) {
        TASKS.add(new ScheduledTask(server, server.getTickCount() + Math.max(1, delayTicks), task));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (TASKS.isEmpty()) return;
        MinecraftServer server = event.getServer();
        Iterator<ScheduledTask> it = TASKS.iterator();
        while (it.hasNext()) {
            ScheduledTask task = it.next();
            if (task.server() != server) continue;
            if (server.getTickCount() >= task.runAtTick()) {
                it.remove();
                task.task().run();
            }
        }
    }
}
