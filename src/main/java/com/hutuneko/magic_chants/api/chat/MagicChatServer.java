package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.magic.MagicCast;
import com.hutuneko.magic_chants.api.util.MagicChantsAPI;
import com.hutuneko.magic_chants.api.util.MagicLineParser;
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
    private static final Map<UUID, List<MagicCast.Step>> SUB = new ConcurrentHashMap<>();
    public static final Map<UUID, CurrentMagicContext> CURRENT_SESSIONS = new ConcurrentHashMap<>();
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

        var ctx = CURRENT_SESSIONS.get(sp.getUUID());
        UUID itemUuid = (ctx != null) ? ctx.itemUuid() : null;
        String normalized = raw;
        var steps = MagicLineParser.parse(level,itemUuid, normalized);
        System.out.println(steps);


        if (!steps.isEmpty()) {
            // ① 先頭グループは即時実行候補（PENDING）へ
            PENDING.computeIfAbsent(sp.getUUID(), k -> new ArrayList<>()).addAll(steps.get(0));
            CHANT_TEXTS.computeIfAbsent(sp.getUUID(), k -> new ArrayList<>()).add(raw);
            System.out.println("[DBG] parsed steps = " + steps.size());

            // ② 残りは 1 グループにまとめて SUB の末尾へ追加し、最後に null を1つだけ付与
            if (steps.size() > 1) {
                List<MagicCast.Step> sub = SUB.computeIfAbsent(sp.getUUID(), k -> new ArrayList<>());

                // 残りをフラット化して1グループ化
                List<MagicCast.Step> rest = new ArrayList<>();
                for (int i = 1; i < steps.size(); i++) {
                    if (!steps.get(i).isEmpty()) rest.addAll(steps.get(i));
                }

                if (!rest.isEmpty()) {
                    sub.addAll(rest);
                    // 末尾に null マーカー（重複防止）
                    if (sub.isEmpty() || sub.get(sub.size() - 1) != null) {
                        sub.add(null);
                    }
                }
            }else {
                List<MagicCast.Step> sub = SUB.computeIfAbsent(sp.getUUID(), k -> new ArrayList<>());
                sub.add(null);
            }
        }

    }


    // チャット閉じ通知（C2S_CommitMagicPacket）でそのまま実行
    public static void handleCommit(ServerPlayer p) {
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
        String l = lines.toString();
        List<String> chats = Arrays.stream(
                        l.trim()
                                .replace('\u3000', ' ')  // 全角スペース→半角に正規化
                                .split("\\s+")            // 空白の連続で分割
                )
                .filter(a -> !a.isEmpty())
                .toList();
        var list = PENDING.remove(p.getUUID());

        if (list == null || list.isEmpty()) return;
        var sublist = SUB.remove(p.getUUID());
        var result = MagicChantsAPI.mergeWithUnknownMarkersAndFlags(list,sublist);
        list = result.first;
        System.out.println(list);
        List<Boolean> bList = result.second;


        // --- 詠唱を実行 ---
        MagicCast.startChain(level, p, list, null, 20 * 30, chantRaw,bList);

        // --- 後処理 ---
        clear(p);
    }


    // （任意）キャンセルAPI：ESC時にキャンセルだけしたい等
    public static void cancel(ServerPlayer p) {
        PENDING.remove(p.getUUID());
    }
}
