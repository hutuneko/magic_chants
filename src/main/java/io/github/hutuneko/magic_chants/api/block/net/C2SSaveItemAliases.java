package io.github.hutuneko.magic_chants.api.block.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.file.AliasRewriter;
import io.github.hutuneko.magic_chants.api.file.WorldJsonStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record C2S_SaveItemAliases(String itemUuid, String json) implements CustomPacketPayload {

    public static final Type<C2S_SaveItemAliases> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "save_item_aliases")
    );

    public static final StreamCodec<FriendlyByteBuf, C2S_SaveItemAliases> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2S_SaveItemAliases::itemUuid,
            ByteBufCodecs.STRING_UTF8, C2S_SaveItemAliases::json,
            C2S_SaveItemAliases::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        ServerPlayer sp = context.player() instanceof ServerPlayer s ? s : null;
        if (sp == null) return;

        context.enqueueWork(() -> {
            try {
                // validate
                var je = com.google.gson.JsonParser.parseString(json);
                String existingJson = (String) WorldJsonStorage.load(sp.level(), "magics/" + itemUuid + ".json", Object.class);
                String pretty = AliasRewriter.rewriteChants(existingJson, String.valueOf(je));
                WorldJsonStorage.save(sp.level(), "magics/" + itemUuid + ".json", pretty);
                MagicChants.LOGGER.info("[C2S_Save] saved {}", itemUuid);

                // 保存後に再送して画面を更新
                PacketDistributor.sendToPlayer(sp, new S2C_SyncItemAliases(itemUuid, pretty));
            } catch (Exception e) {
                MagicChants.LOGGER.error("[C2S_Save] failed", e);
            }
        });
    }
}