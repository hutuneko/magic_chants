// package はあなたの構成に合わせて変更してください。
package com.hutuneko.magic_chants.api.magic;

import com.hutuneko.magic_chants.Magic_chants;
import com.hutuneko.magic_chants.api.chat.ChantScorer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MagicCast {

    private MagicCast() {}
    private static float scorer;
    /* ===== ステップ表現 ===== */

    /** 実行ステップ（id + args）。args は Magic 固有の初期設定。 */
    public record Step(ResourceLocation id, CompoundTag args) {
        public Step(ResourceLocation id) { this(id, new CompoundTag()); }
    }

    /** 待機ポイントを作る：signal / signalAndInject で再開。 */
    public static Step waitStep(String token) {
        CompoundTag tag = new CompoundTag();
        tag.putString("_wait_token", token);
        return new Step(WAIT_ID, tag);
    }

    private static final ResourceLocation WAIT_ID = new ResourceLocation(Magic_chants.MODID, "_wait");

    /* ===== セッション ===== */

    private static final class Session {
        final UUID playerId;
        final ServerLevel level;
        List<Step> steps;
        final DataBag bag;           // 共有データ
        int index;                   // 次に実行するステップのインデックス
        long deadline;               // タイムアウトtick
        long resumeGameTime;
        @Nullable String waitToken;

        Session(ServerLevel lvl, @Nullable ServerPlayer p, List<Step> steps,
                @Nullable DataBag initialBag, int timeoutTicks,String raw) {
            this.playerId = (p != null ? p.getUUID() : new UUID(0, 0));
            this.level = lvl;
            this.steps = List.copyOf(steps);
            this.bag = (initialBag != null) ? initialBag : new DataBag();
            this.bag.put(Keys.CHANT_RAW, raw);
            this.index = 0;
            long now = lvl.getServer().getTickCount();
            this.deadline = (timeoutTicks > 0) ? now + timeoutTicks : Long.MAX_VALUE;
        }
    }

    /* プレイヤー毎のセッション（1人1つ想定。複数許す場合はキーを別にする） */
    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    /* ===== 公開API ===== */

    /**
     * チェーンを先頭から実行。WAIT に当たればセッションに保存して中断。
     */
    public static void startChain(ServerLevel level,
                                  @Nullable ServerPlayer player,
                                  List<Step> steps,
                                  @Nullable DataBag initialBag,
                                  int timeoutTicks,
                                  String string) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(steps, "steps");
        Session s = new Session(level, player, steps, initialBag, timeoutTicks, string);
        SESSIONS.put(s.playerId, s);
        System.out.println("[MagicCast] start steps=" + s.steps.size());
        ensureTicker(level.getServer());
        runUntilWaitOrEnd(s, player);
    }

    /**
     * WAIT をそのまま通過させたいとき（差し込み無しで再開）。
     * signalAndInject(player, token, List.of()) と同じ。
     */
    public static boolean signal(@Nullable ServerPlayer player, String token) {
        return signalAndInject(player, token, List.of());
    }

    /**
     * token を待っている WAIT を見つけ、そこに steps を差し込んでから続行する。
     * @return 差し込み＆再開できたら true
     */
    public static boolean signalAndInject(@Nullable ServerPlayer player, String token, List<Step> steps) {
        if (player == null) return false;
        Session s = SESSIONS.get(player.getUUID());
        if (s == null) return false;
        if (!token.equals(s.waitToken)) return false;

        // WAIT 位置に差し込み（WAIT を削除して、その場所に steps を挿入）
        List<Step> newList = new ArrayList<>(s.steps);
        if (s.index < 0 || s.index >= newList.size()) return false;

        newList.remove(s.index);             // WAIT を外す
        if (steps != null && !steps.isEmpty()) {
            newList.addAll(s.index, steps);  // 同じ位置に差し込む
        }
        s.steps = List.copyOf(newList);
        s.waitToken = null;

        runUntilWaitOrEnd(s, player);
        return true;
    }

    /**
     * セッションを強制キャンセル（任意）。
     */
    public static boolean cancel(@Nullable ServerPlayer player) {
        if (player == null) return false;
        return SESSIONS.remove(player.getUUID()) != null;
    }

    /* ===== 実行ループ ===== */

    private static void runUntilWaitOrEnd(Session s, ServerPlayer player) {
        MagicContext ctx = new MagicContext(s.level, player, s.bag);
        if (s.bag.get(Keys.POWER).isEmpty()) {
            System.out.println(0);
            scorer = (ChantScorer.score(s.bag.get(Keys.CHANT_RAW).orElse(null),player)) / 2;
            ctx.data().put(Keys.POWER, scorer);
            System.out.println("[DBG] POWER=" + s.bag.get(Keys.POWER));
            System.out.printf("[MagicCast/ChantScore] '%s' -> %.2f (Power=%.2f)%n", s.bag.get(Keys.CHANT_RAW), scorer, scorer);
        }
        while (s.index < s.steps.size()) {
            MagicCast.Step step = s.steps.get(s.index);

            // WAIT?
            if (isWait(step)) {
                s.waitToken = step.args().getString("_wait_token");
                return;
            }
            //直後を覗く supplier をセット
            int nextIdx = s.index + 1;
            ctx._setPeekNextSupplier(() -> nextIdx < s.steps.size() ? s.steps.get(nextIdx) : null);

            ctx._setPeekRestSupplier(() ->
                    (nextIdx <= s.steps.size()) ? List.copyOf(s.steps.subList(nextIdx, s.steps.size()))
                            : List.of());
            MagicClassRegistry.call(step.id(), ctx, safeArgs(step.args()),scorer);
            s.index++;

            //Magic 側が enqueue した Step を i+1 に挿入
            var injected = ctx._drainEnqueued();
            if (!injected.isEmpty()) {
                List<Step> newList = new ArrayList<>(s.steps);
                newList.addAll(nextIdx, injected);
                s.steps = List.copyOf(newList);
            }

            //delayNext の要求があれば一時停止
            int delay = ctx._drainRequestedDelay();
            System.out.println("[MagicCast] injected=" + injected.size());
            System.out.println("[MagicCast] delay=" + delay);
            if (delay > 0) {
                s.resumeGameTime = s.level.getServer().getTickCount() + delay;
                System.out.println("[MagicCast] scheduled resume at " + s.resumeGameTime);
                return;
            }
            if (ctx._consumeCancelRequest()) {
                return; // セッション終わり（剣に封入できた等）
            }
        }

        SESSIONS.remove(s.playerId);
    }


    private static boolean isWait(Step step) {
        return WAIT_ID.equals(step.id()) && step.args() != null && step.args().contains("_wait_token");
    }

    private static CompoundTag safeArgs(@Nullable CompoundTag t) {
        return (t != null) ? t : new CompoundTag();
    }

    /* ===== サーバーtick監視（タイムアウト自動掃除） ===== */

    private static volatile boolean TICKER_INSTALLED = false;

    private static void ensureTicker(MinecraftServer server) {
        if (TICKER_INSTALLED) return;
        TICKER_INSTALLED = true;
        MinecraftForge.EVENT_BUS.register(new ServerTicker());
    }

    /** 期限切れセッションの掃除だけ行うシンプルなTicker */
    public static final class ServerTicker {
        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            long now = e.getServer().getTickCount();

            // タイムアウト掃除
            SESSIONS.values().removeIf(s -> now >= s.deadline);
            for (Session s : SESSIONS.values()) {
                if (s.resumeGameTime > 0L && now >= s.resumeGameTime) {
                    s.resumeGameTime = 0L;
                    ServerPlayer sp = s.level.getServer().getPlayerList().getPlayer(s.playerId);
                    System.out.println("[MagicCast] resume for " + s.playerId + " at tick " + now);
                    runUntilWaitOrEnd(s, sp);
                }
            }
        }
    }


    /* =====（任意）チェーンの保存/復元：データ駆動で便利 ===== */

    public static ListTag saveChainToNbt(List<Step> steps) {
        ListTag out = new ListTag();
        for (Step s : steps) {
            CompoundTag row = new CompoundTag();
            row.putString("id", s.id().toString());
            row.put("args", safeArgs(s.args()));
            out.add(row);
        }
        return out;
    }

    public static List<Step> loadChainFromNbt(ListTag list) {
        List<Step> out = new ArrayList<>();
        for (net.minecraft.nbt.Tag tag : list) {
            if (!(tag instanceof CompoundTag ct)) continue;
            ResourceLocation id = new ResourceLocation(ct.getString("id"));
            CompoundTag args = ct.getCompound("args");
            out.add(new Step(id, args));
        }
        return out;
    }

    /** WAIT ステップを NBT で表すときのヘルパ（省略可） */
    public static Step waitStepFromNbt(String token) {
        CompoundTag tag = new CompoundTag();
        tag.putString("_wait_token", token);
        return new Step(WAIT_ID, tag);
    }

    /* 複数 WAIT をあらかじめ置いておき、あとから何段でも差し込めます。 */
}
