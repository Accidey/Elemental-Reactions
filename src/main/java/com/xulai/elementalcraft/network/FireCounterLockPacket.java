package com.xulai.elementalcraft.network;

import com.xulai.elementalcraft.ElementalCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FireCounterLockPacket(boolean locked) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FireCounterLockPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ElementalCraft.MODID, "fire_counter_lock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FireCounterLockPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, FireCounterLockPacket::locked,
            FireCounterLockPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FireCounterLockPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            com.xulai.elementalcraft.client.FrozenInputBlocker.fireCounterLocked = msg.locked;
        });
    }
}
