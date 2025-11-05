package com.hutuneko.magic_chants.api.player.feke;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_UpdateProxyPos {
    private final double x, y, z;
    private final float yaw, pitch;

    public C2S_UpdateProxyPos(double x, double y, double z, float yaw, float pitch) {
        this.x = x; this.y = y; this.z = z; this.yaw = yaw; this.pitch = pitch;
    }

    public static void encode(C2S_UpdateProxyPos msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
    }

    public static C2S_UpdateProxyPos decode(FriendlyByteBuf buf) {
        return new C2S_UpdateProxyPos(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(C2S_UpdateProxyPos msg, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        ServerPlayer player = c.getSender();
        RemoteShooterEntity proxy = C2S_StartRemoteView.PROXIES.get(player.getUUID());
        if (proxy != null && proxy.level() == player.serverLevel()) {
            proxy.setPos(msg.x, msg.y, msg.z);
            proxy.setYRot(msg.yaw);
            proxy.setXRot(msg.pitch);
        }
    }

}
