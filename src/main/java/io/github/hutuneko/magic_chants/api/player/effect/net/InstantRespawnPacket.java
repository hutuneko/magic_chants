package io.github.hutuneko.magic_chants.api.player.effect.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.player.effect.RespawnHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InstantRespawnPacket() implements CustomPacketPayload {

    public static final Type<InstantRespawnPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "instant_respawn")
    );

    // データがないのでUnit型を使う（空のパケット）
    public static final StreamCodec<FriendlyByteBuf, InstantRespawnPacket> STREAM_CODEC = StreamCodec.unit(
            new InstantRespawnPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player() instanceof ServerPlayer s ? s : null;
            if (player != null) {
                RespawnHandler.respawnNow(player);
            }
        });
    }
}