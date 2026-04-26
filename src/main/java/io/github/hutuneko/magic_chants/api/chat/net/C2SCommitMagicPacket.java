package io.github.hutuneko.magic_chants.api.chat.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.chat.MagicChatServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2S_CommitMagicPacket(String itemUuid, ItemStack itemStack) implements CustomPacketPayload {

    public static final Type<C2S_CommitMagicPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "commit_magic")
    );

    public static final StreamCodec<FriendlyByteBuf, C2S_CommitMagicPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2S_CommitMagicPacket::itemUuid,
            ByteBufCodecs.fromCodec(ItemStack.CODEC), C2S_CommitMagicPacket::itemStack,
            C2S_CommitMagicPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sp = context.player() instanceof ServerPlayer s ? s : null;
            if (sp != null) {
                MagicChatServer.handleCommit(sp);
            }
        });
    }
}