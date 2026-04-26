package io.github.hutuneko.magic_chants.api.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.magic.action.MagicSummon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SEntityGet(String registryName) implements CustomPacketPayload {

    public static final Type<C2SEntityGet> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "entity_get")
    );

    public static final StreamCodec<FriendlyByteBuf, C2SEntityGet> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2SEntityGet::registryName,
            C2SEntityGet::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SEntityGet msg,IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player() instanceof ServerPlayer s ? s : null;
            if (player == null) return;
            MagicSummon.spawnEntity(player, msg.registryName);
        });
    }
}