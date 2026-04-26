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
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record C2S_RewriteAndSaveAliases(String itemUuid, String rulesText) implements CustomPacketPayload {

    public static final Type<C2S_RewriteAndSaveAliases> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "rewrite_and_save_aliases")
    );

    public static final StreamCodec<FriendlyByteBuf, C2S_RewriteAndSaveAliases> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2S_RewriteAndSaveAliases::itemUuid,
            ByteBufCodecs.STRING_UTF8, C2S_RewriteAndSaveAliases::rulesText,
            C2S_RewriteAndSaveAliases::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sp = context.player() instanceof ServerPlayer s ? s : null;
            if (sp == null) return;
            ServerLevel sl = sp.level();

            // 1) サーバでロード
            Object raw = WorldJsonStorage.load(sl, "magics/" + itemUuid + ".json", Object.class);
            String js = (raw == null) ? "{\"magics\":[]}" : new com.google.gson.Gson().toJson(raw);

            // 2) ルール適用
            String out = AliasRewriter.rewriteChants(js, rulesText);

            // 3) 検証 & 保存
            com.google.gson.JsonParser.parseString(out); // throws if invalid
            String pretty = new com.google.gson.GsonBuilder().setPrettyPrinting().create()
                    .toJson(com.google.gson.JsonParser.parseString(out));
            WorldJsonStorage.save(sl, "magics/" + itemUuid + ".json", pretty);
            Object o = WorldJsonStorage.load(sl, "magics/" + itemUuid + ".json", Object.class);
            String j = AliasRewriter.toAliasLinesFromMagicsA(o);

            // 4) 最新状態をS2Cで返して画面更新
            PacketDistributor.sendToPlayer(sp, new S2C_SyncItemAliases(itemUuid, j));
        });
    }
}