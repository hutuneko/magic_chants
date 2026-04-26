package io.magic_chants.api.chat;

import io.github.hutuneko.magic_chants.api.file.WorldJsonStorage;
import io.github.hutuneko.magic_chants.api.magic.MagicCast;
import io.github.hutuneko.magic_chants.api.util.MagicChantsAPI;
import io.github.hutuneko.magic_chants.api.util.MagicLineParser;
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
            CHANT_TEXTS.computeIfAbsent(sp.getUUID(), k -> new ArrayList<>()).add(raw);
            List<WorldJsonStorage.MagicDef> def = p.second;
            def.forEach(mainDef -> mainDef.setStepsChantSource(MagicCast.ChantSource.MAIN));
            DEF.computeIfAbsent(sp.getUUID(),k -> new ArrayList<>()).add(def.get(0));
            System.out.println("[DBG] parsed steps = " + steps.size());


            // ② 残りは 1 グループにまとめて SUB の末尾へ追加し、最後に null を1つだけ付与
            List<WorldJsonStorage.MagicDef> sub = DEFSUB.computeIfAbsent(sp.getUUID(),k -> new ArrayList<>());
            if (steps.size() > 1) {
                List<WorldJsonStorage.MagicDef> a = new ArrayList<>();
                for (int i = 1; i < def.size(); i++) {
                    if (!(def.get(i).isEmpty())) a.add(def.get(i));
                }
                if (!a.isEmpty()) {
                    sub.addAll(a);
                    // 末尾に null マーカー（重複防止）
                    if (sub.get(sub.size() - 1).isEmpty()) {
                        sub.add(new WorldJsonStorage.MagicDef());
                    }
                }
            }else {
                sub.add(new WorldJsonStorage.MagicDef());
            }
            sub.forEach(subDef -> subDef.setStepsChantSource(MagicCast.ChantSource.SUB));
        }
    }


    // チャット閉じ通知（C2S_CommitMagicPacket）でそのまま実行
    public static void handleCommit(ServerPlayer p) {
        System.out.println("[DEBUG] handleCommit called for player " + p.getName().getString());
        // --- 詠唱文をまとめる ---
        var lines = CHANT_TEXTS.remove(p.getUUID());
        String chantRaw = (lines == null || lines.isEmpty()) ? "" : String.join(" ", lines).trim();
        // --- 近距離チャット送信 ---
        double radius = 32.0; // 聞こえる範囲（ブロック単位）
        var level = p.serverLevel();
        String[] words = chantRaw.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentLineLength = 0;

        for (String word : words) {
            // 単語を追加。行の先頭以外はスペースを付与
            if (currentLineLength > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);

            // 現在の行の長さを更新（スペースを含む）
            currentLineLength = currentLine.length();

            // 指定文字数を超えたら改行し、StringBuilderと文字数カウントをリセット
            if (currentLineLength >= 20) {
                Component msg = Component.literal(currentLine.toString())
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
                for (ServerPlayer sp : level.players()) {
                    if (sp.level() == level && sp.distanceToSqr(p) <= radius * radius) {
                        sp.sendSystemMessage(msg);
                    }
                }

                // リセット処理
                currentLine.setLength(0);
                currentLineLength = 0;
            }
        }
        if (!currentLine.isEmpty()) {
            Component msg = Component.literal(currentLine.toString())
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
            for (ServerPlayer sp : level.players()) {
                if (sp.level() == level && sp.distanceToSqr(p) <= radius * radius) {
                    sp.sendSystemMessage(msg);
                }
            }
        }
        Pair<List<MagicCast.Step>, List<String>> result = MagicChantsAPI.mergeAndAlignC(DEF.remove(p.getUUID()),DEFSUB.remove(p.getUUID()));
        System.out.println(result);
        List<MagicCast.Step> list = result.first;
        System.out.println("[DEBUG] merged steps list: " + list);
        System.out.println("[DEBUG] merged steps list size: " + (list != null ? list.size() : "null"));
        List<String> s = result.second;

        // --- 詠唱を実行 ---
        MagicCast.startChain(level, p, list, null, 20 * 30, chantRaw,s);

        // --- 後処理 ---
        clear(p);
    }
}
