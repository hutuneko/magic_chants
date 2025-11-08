package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import com.hutuneko.magic_chants.api.util.MagicChantsAPI;
import com.hutuneko.magic_chants.api.util.MagicLineParser;
import com.ibm.icu.impl.Pair;
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
    private static final Map<UUID, List<WorldJsonStorage.MagicDef>> DEF = new ConcurrentHashMap<>();
    private static final Map<UUID, List<WorldJsonStorage.MagicDef>> DEFSUB = new ConcurrentHashMap<>();
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
        Pair<List<List<MagicCast.Step>>, List<WorldJsonStorage.MagicDef>> p = MagicLineParser.parse(level,itemUuid, normalized);
        var steps = p.first;
        System.out.println(steps);


        if (!steps.isEmpty()) {
            // ① 先頭グループは即時実行候補（PENDING）へ
            PENDING.computeIfAbsent(sp.getUUID(), k -> new ArrayList<>()).addAll(steps.get(0));
            CHANT_TEXTS.computeIfAbsent(sp.getUUID(), k -> new ArrayList<>()).add(raw);
            List<WorldJsonStorage.MagicDef> def = p.second;
            DEF.computeIfAbsent(sp.getUUID(),k -> new ArrayList<>()).add(def.get(0));
            System.out.println("[DBG] parsed steps = " + steps.size());


            // ② 残りは 1 グループにまとめて SUB の末尾へ追加し、最後に null を1つだけ付与
            List<MagicCast.Step> sub = SUB.computeIfAbsent(sp.getUUID(), k -> new ArrayList<>());
            if (steps.size() > 1) {

                // 残りをフラット化して1グループ化
                List<MagicCast.Step> rest = new ArrayList<>();
                for (int i = 1; i < steps.size(); i++) {
                    if (!steps.get(i).isEmpty()) rest.addAll(steps.get(i));
                }
                List<WorldJsonStorage.MagicDef> a = new ArrayList<>();
                for (int i = 1; i < def.size(); i++) {
                    if (!(def.get(i) == null)) a.add(def.get(i));
                }
                DEFSUB.computeIfAbsent(sp.getUUID(),k -> new ArrayList<>()).addAll(a);
                if (!rest.isEmpty()) {
                    sub.addAll(rest);
                    // 末尾に null マーカー（重複防止）
                    if (sub.isEmpty() || sub.get(sub.size() - 1) != null) {
                        sub.add(null);
                    }
                }
            }else {
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
        var sublist = SUB.remove(p.getUUID());
        var result = MagicChantsAPI.mergeAndAlignC(DEF.get(p.getUUID()),DEFSUB.get(p.getUUID()));
        var list = result.getLeft();
        System.out.println(list);
        List<Boolean> bList = result.getMiddle();
        List<String> s = result.getRight();

        // --- 詠唱を実行 ---
        MagicCast.startChain(level, p, list, null, 20 * 30, chantRaw,bList,s);

        // --- 後処理 ---
        clear(p);
    }


    // （任意）キャンセルAPI：ESC時にキャンセルだけしたい等
    public static void cancel(ServerPlayer p) {
        PENDING.remove(p.getUUID());
    }
}
