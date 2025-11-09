package com.hutuneko.magic_chants.api.player.attribute.magic_power.net;

import com.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

// ★ クラス全体をクライアント専用としてマーク
@OnlyIn(Dist.CLIENT)
public class ClientMagicPowerHandler {

    // S2C_SyncMagicPowerPacket の handle メソッドの中身をコピー
    public static void handle(S2C_SyncMagicPowerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // ★ Minecraft.getInstance().player への安全な参照
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            player.getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(cap -> {
                cap.setMP(msg.mp);
                cap.setMaxMP(msg.maxMp);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}