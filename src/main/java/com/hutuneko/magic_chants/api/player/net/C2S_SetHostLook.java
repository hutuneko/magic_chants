package com.hutuneko.magic_chants.api.player.net;

import com.hutuneko.magic_chants.api.util.LookControlUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// 送信用レコード（登録は既存の MagicNetwork に追記）
public record C2S_SetHostLook(float yaw, float pitch) {
    public static void encode(C2S_SetHostLook m, FriendlyByteBuf buf) {
        buf.writeFloat(m.yaw);
        buf.writeFloat(m.pitch);
    }
    public static C2S_SetHostLook decode(FriendlyByteBuf buf) {
        return new C2S_SetHostLook(buf.readFloat(), buf.readFloat());
    }
    public static void handle(C2S_SetHostLook m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            if (!sp.getPersistentData().getBoolean("magic_chants:spiritf")) return;

            ServerLevel sl = (ServerLevel) sp.level();
            UUID uid = sp.getPersistentData().hasUUID("magic_chants:spiritu")
                    ? sp.getPersistentData().getUUID("magic_chants:spiritu") : null;
            if (uid == null) return;

            Entity tgt = sl.getEntity(uid);
            if (!(tgt instanceof LivingEntity le)) return;

            // サーバで強制適用（AIに上書きされないようLOWESTでやっているのと同じ処理）
            LookControlUtil.forceCameraView(le, m.yaw(), m.pitch());

            // 視線パケットを明示配信（確実に同期させる）
            sl.getChunkSource().broadcastAndSend(le,
                    new ClientboundRotateHeadPacket(le, (byte) Mth.floor(le.getYHeadRot() * 256 / 360)));
            sl.getChunkSource().broadcastAndSend(le,
                    new ClientboundMoveEntityPacket.Rot(
                            le.getId(),
                            (byte) Mth.floor(le.getYRot() * 256 / 360),
                            (byte) Mth.floor(le.getXRot() * 256 / 360),
                            le.onGround()
                    )
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
