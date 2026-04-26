package io.github.hutuneko.magic_chants.api.block.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.file.AliasRewriter;
import io.github.hutuneko.magic_chants.api.file.WorldJsonStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SRequestItemAliases(String itemUuid) implements CustomPacketPayload {

    public static final Type<C2SRequestItemAliases> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "request_item_aliases")
    );

    public static final StreamCodec<FriendlyByteBuf, C2SRequestItemAliases> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2SRequestItemAliases::itemUuid,
            C2SRequestItemAliases::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SRequestItemAliases msg,IPayloadContext context) {
        ServerPlayer sp = context.player() instanceof ServerPlayer s ? s : null;
        if (sp == null) return;

        context.enqueueWork(() -> {
            ServerLevel sl = sp.level();
            MagicChants.LOGGER.info("[C2S] req aliases uuid={}", msg.itemUuid);

            String jsonOut;
            Object raw = WorldJsonStorage.load(sl, "magics/" + msg.itemUuid + ".json", Object.class);
            if (raw == null) {
                jsonOut = "{\"magics\":[]}";
            } else {
                jsonOut = AliasRewriter.toAliasLinesFromMagicsA(raw);
            }

            MagicChants.LOGGER.info("[C2S] loaded json length={}", jsonOut.length());

            try {
                PacketDistributor.sendToPlayer(sp, new S2CSyncItemAliases(msg.itemUuid, jsonOut));
                MagicChants.LOGGER.info("[C2S] sent S2C to {}", sp.getGameProfile().name());
            } catch (Throwable t) {
                MagicChants.LOGGER.error("[C2S] send failed", t);
            }
        });
    }
}