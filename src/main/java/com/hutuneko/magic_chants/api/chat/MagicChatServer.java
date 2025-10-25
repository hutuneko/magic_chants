package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.magic.MagicCast;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public final class MagicChatServer {

    private static final Map<UUID, List<MagicCast.Step>> PENDING = new ConcurrentHashMap<>();
    public static final Map<UUID, CurrentMagicContext> CURRENT_SESSIONS = new ConcurrentHashMap<>();
    // 追記
    private static final Map<UUID, List<String>> CHANT_TEXTS = new ConcurrentHashMap<>();

    public record CurrentMagicContext(UUID itemUuid, InteractionHand hand, ItemStack itemStack) {}

    // C2S_CommitMagicPacket の handle で登録
    public static void setCurrent(ServerPlayer player, UUID itemUuid, InteractionHand hand,ItemStack itemStack) {
        CURRENT_SESSIONS.put(player.getUUID(), new CurrentMagicContext(itemUuid, hand,itemStack));
    }

    // 終了時クリア
    public static void clear(ServerPlayer player) {
        CURRENT_SESSIONS.remove(player.getUUID());
    }
    @SubscribeEvent
    public static void onServerChat(ServerChatEvent e) {
        var sp = e.getPlayer();
        String msg = e.getMessage().getString().trim();

        // セッション有無を確認
        boolean inSession = CURRENT_SESSIONS.containsKey(sp.getUUID());

        // ★ セッション中は "#magic.json " なしでも受理
        String raw;
        if (msg.startsWith("#magic ")) {
            raw = msg.substring("#magic ".length()).trim();
        } else if (inSession) {
            raw = msg; // そのまま詠唱文として扱う
        } else {
            return; // 通常チャット
        }
        e.setCanceled(true);

        var level = sp.serverLevel();

        // ===== アイテム辞書（セッションに紐づく UUID があれば読み込む） =====
        var ctx = CURRENT_SESSIONS.get(sp.getUUID());
        UUID itemUuid = (ctx != null) ? ctx.itemUuid() : null;
        var item = (ctx != null) ? ctx.itemStack() : ItemStack.EMPTY;

        if (!item.isEmpty() && itemUuid != null) {
            itemUuid = sp.getPersistentData().getUUID("magic_chants:itemuuid");
        }

        String normalized = raw;
        var steps = MagicLineParser.parse(level,itemUuid, normalized);
        System.out.println(steps);

        if (!steps.isEmpty()) {
            PENDING.computeIfAbsent(sp.getUUID(), k -> new java.util.ArrayList<>()).addAll(steps);
            CHANT_TEXTS.computeIfAbsent(sp.getUUID(), k -> new java.util.ArrayList<>()).add(raw);
            System.out.println("[DBG] parsed steps = " + steps.size());
        }
    }


    // チャット閉じ通知（C2S_CommitMagicPacket）でそのまま実行
    public static void handleCommit(ServerPlayer p) {
        var list = PENDING.remove(p.getUUID());
        if (list == null || list.isEmpty()) return;

        // --- 詠唱文をまとめる ---
        var lines = CHANT_TEXTS.remove(p.getUUID());
        String chantRaw = (lines == null || lines.isEmpty()) ? "" : String.join(" ", lines).trim();

        // --- 近距離チャット送信 ---
        double radius = 32.0; // 聞こえる範囲（ブロック単位）
        var level = p.serverLevel();
        Component msg = Component.literal(chantRaw)
                .withStyle(ChatFormatting.LIGHT_PURPLE);

        for (ServerPlayer sp : level.players()) {
            if (sp.level() == level && sp.distanceToSqr(p) <= radius * radius) {
                sp.sendSystemMessage(msg);
            }
        }

        // --- 詠唱を実行 ---
        MagicCast.startChain(level, p, list, null, 20 * 30, chantRaw);

        // --- 後処理 ---
        clear(p);
    }


    // （任意）キャンセルAPI：ESC時にキャンセルだけしたい等
    public static void cancel(ServerPlayer p) {
        PENDING.remove(p.getUUID());
    }
}
