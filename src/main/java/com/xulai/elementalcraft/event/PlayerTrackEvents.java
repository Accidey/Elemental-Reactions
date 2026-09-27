package com.xulai.elementalcraft.event;

import com.xulai.elementalcraft.logic.MobAttributeLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "elementalcraft")
public class PlayerTrackEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 40 != 0) return;
        ServerLevel level = player.serverLevel();

        level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(20, 5, 20),
                mob -> mob.isAlive() && !mob.getPersistentData().getBoolean("ElementalCraft_AttributesSet")
        ).forEach(MobAttributeLogic::processMob);
    }
}
