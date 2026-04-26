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

public record C2S_EntityGet(String registryName) implements CustomPacketPayload {

    public static final Type<C2S_EntityGet> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "entity_get")
    );

    public static final StreamCodec<FriendlyByteBuf, C2S_EntityGet> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2S_EntityGet::registryName,
            C2S_EntityGet::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player() instanceof ServerPlayer s ? s : null;
            if (player == null) return;
            MagicSummon.spawnEntity(player, registryName);
        });
    }
}