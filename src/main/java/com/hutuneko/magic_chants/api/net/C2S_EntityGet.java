package com.hutuneko.magic_chants.api.net;

import com.hutuneko.magic_chants.magic.action.Magic_Summon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2S_EntityGet(String registryName) {
    public static void encode(C2S_EntityGet m, FriendlyByteBuf buf) {
        buf.writeUtf(m.registryName());
    }

    public static C2S_EntityGet decode(FriendlyByteBuf buf) {
        return new C2S_EntityGet(buf.readUtf());
    }

    public static void handle(C2S_EntityGet m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // サーバー側で実行される
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Magic_Summon.spawnEntity(player,m.registryName());
        });
        ctx.get().setPacketHandled(true);
    }
}