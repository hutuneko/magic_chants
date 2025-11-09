package com.hutuneko.magic_chants.api.player.net;

import com.hutuneko.magic_chants.api.net.MagicNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public record S2C_Rot(UUID uuid, float yrot, float xrot) {

    public static void encode(S2C_Rot m, FriendlyByteBuf buf){
        buf.writeUUID(m.uuid());
        buf.writeFloat(m.yrot());
        buf.writeFloatLE(m.yrot());
    }

    public static S2C_Rot decode(FriendlyByteBuf buf){
        UUID id = buf.readUUID();
        float y = buf.readFloat();
        float x = buf.readFloatLE();
        return new S2C_Rot(id, y,x);
    }

    public static void handle(S2C_Rot m, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        ServerPlayer p = c.getSender();
        MagicNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p),new C2S_SetHostLook(m.yrot(),m.xrot()));
    }
}