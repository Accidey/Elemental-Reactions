package com.xulai.elementalcraft.event;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.minecraft.world.item.Items;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class PotionStackHandler {

    @SubscribeEvent
    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        if (!ElementalConfig.POTION_STACK_64.get()) return;
        event.modify(Items.POTION, builder -> builder.set(DataComponents.MAX_STACK_SIZE, 64));
        event.modify(Items.SPLASH_POTION, builder -> builder.set(DataComponents.MAX_STACK_SIZE, 64));
        event.modify(Items.LINGERING_POTION, builder -> builder.set(DataComponents.MAX_STACK_SIZE, 64));
        ElementalCraft.LOGGER.info("[ElementalCraft] 原版药水堆叠上限已改为 64");
    }
}
