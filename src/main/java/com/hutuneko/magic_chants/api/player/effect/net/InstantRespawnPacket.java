package com.hutuneko.magic_chants.api.player.effect.net;

import com.hutuneko.magic_chants.api.player.effect.RespawnHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InstantRespawnPacket {
    public InstantRespawnPacket() {}

    public static void encode(InstantRespawnPacket msg, FriendlyByteBuf buf) {}
    public static InstantRespawnPacket decode(FriendlyByteBuf buf) { return new InstantRespawnPacket(); }

    public static void handle(InstantRespawnPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                RespawnHandler.respawnNow(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
