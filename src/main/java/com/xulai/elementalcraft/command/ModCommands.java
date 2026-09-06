package com.xulai.elementalcraft.command;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid = "elementalcraft")
public class ModCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        DebugCommand.register(event.getDispatcher());

        BiomeBiasCommand.register(event.getDispatcher());

        BlacklistCommandHelper.registerAll(event.getDispatcher());
    }
}