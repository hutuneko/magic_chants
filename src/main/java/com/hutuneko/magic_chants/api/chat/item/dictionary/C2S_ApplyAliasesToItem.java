package com.hutuneko.magic_chants.api.chat.item.dictionary;

import com.hutuneko.magic_chants.api.chat.item.ChantItemUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * - クライアントから hand と テキスト(body) を受け取り
 * - 手に持っているアイテムに UUID を付与
 * - <world>/data/magic_chants/aliases/<UUID>.json に辞書を保存
 */
public record C2S_ApplyAliasesToItem(InteractionHand hand, String body) {

    public static void encode(C2S_ApplyAliasesToItem msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        buf.writeUtf(msg.body);
    }

    // 受信
    public static C2S_ApplyAliasesToItem decode(FriendlyByteBuf buf) {
        InteractionHand h = buf.readEnum(InteractionHand.class);
        String b = buf.readUtf();
        return new C2S_ApplyAliasesToItem(h, b);
    }

    // サーバ側処理
    public static void handle(C2S_ApplyAliasesToItem msg, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;

            ItemStack stack = sp.getItemInHand(msg.hand);
            if (stack.isEmpty()) return;

            // UUID を必ず付与（既にあれば既存を使用）
            UUID id = ChantItemUtil.ensureUuid(stack);

            // body を行へ分解（空行/コメントは除外）
            List<String> lines = Arrays.stream(msg.body.split("\\R"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                    .toList();

            // <world>/data/magic_chants/aliases/<UUID>.json へ保存
            ItemAliasIO.write(sp.serverLevel(), id, lines);

            // フィードバック
            sp.displayClientMessage(
                    Component.literal("§a[MagicChants] このアイテムの辞書を保存しました: " + id + " (" + lines.size() + " 行)"),
                    false
            );
        });
        c.setPacketHandled(true);
    }
}
