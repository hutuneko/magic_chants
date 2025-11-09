package com.hutuneko.magic_chants.api.player.attribute.magic_power.net;

import com.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncMagicPowerPacket {
    public final double mp;
    public final double maxMp;

    public S2C_SyncMagicPowerPacket(double mp, double maxMp) {
        this.mp = mp;
        this.maxMp = maxMp;
    }

    public static void encode(S2C_SyncMagicPowerPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.mp);
        buf.writeDouble(msg.maxMp);
    }

    public static S2C_SyncMagicPowerPacket decode(FriendlyByteBuf buf) {
        return new S2C_SyncMagicPowerPacket(buf.readDouble(), buf.readDouble());
    }

    public static void handle(S2C_SyncMagicPowerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        // ★ DistExecutor を使用して、クライアントでのみ ClientMagicPowerHandler.handle を実行
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientMagicPowerHandler.handle(msg, ctx);
        });

        // サーバー側では何も実行されず、クラッシュしない
        ctx.get().setPacketHandled(true);
    }
}
