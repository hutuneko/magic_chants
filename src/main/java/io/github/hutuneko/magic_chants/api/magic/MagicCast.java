package io.magic_chants.api.magic;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.chat.ChantScorer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MagicCast {
    public record Step(
            ResourceLocation id,
            CompoundTag args,
            ChantSource source
    ) {
        // --- ファクトリメソッド（明示的にsource指定）---
        public static Step main(ResourceLocation id) {
            return new Step(id, new CompoundTag(), ChantSource.MAIN);
        }

        public static Step main(ResourceLocation id, CompoundTag args) {
            return new Step(id, args, ChantSource.MAIN);
        }

        public static Step sub(ResourceLocation id) {
            return new Step(id, new CompoundTag(), ChantSource.SUB);
        }
        public static Step sub(ResourceLocation id, CompoundTag args) {
            return new Step(id, args, ChantSource.SUB);
        }

        // --- 判定メソッド ---
        public boolean isMain() { return source == ChantSource.MAIN; }
        public boolean isSub()  { return source == ChantSource.SUB; }

        // --- 派生 ---
        public Step withArgs(CompoundTag newArgs) {
            return new Step(id, newArgs, source);
        }

        public Step withSource(ChantSource newSource) {
            return new Step(id, args, newSource);
        }
    }
    public enum ChantSource { MAIN, SUB }
    /* ===== すべての状態はここに ===== */
    public static final class Session {
        final UUID playerId;
        final ServerLevel level;
        List<Step> steps;
        final DataBag bag;
        int index = 0;
        long deadline;
        long resumeAt = 0;           // 旧 resumeAt
        @Nullable String waitToken;

        final List<String> chantWords;
        float power = 0;

        Session(ServerLevel lvl, @Nullable ServerPlayer p, List<Step> steps,
                @Nullable DataBag initialBag, int timeoutTicks, String raw,
                @Nullable List<String> st) {
            this.playerId = p != null ? p.getUUID() : new UUID(0, 0);
            this.level = lvl;
            this.steps = List.copyOf(steps);
            this.bag = initialBag != null ? initialBag : new DataBag();
            this.bag.put(Keys.CHANT_RAW, raw);
            this.deadline = timeoutTicks > 0
                    ? lvl.getServer().getTickCount() + timeoutTicks
                    : Long.MAX_VALUE;

            this.chantWords = resolveWords(raw, st);
        }

        private static List<String> resolveWords(String raw, @Nullable List<String> st) {
            if (st != null && !st.isEmpty()) return new ArrayList<>(st);
            return Arrays.stream(raw.trim().replace('\u3000', ' ').split("\\s+"))
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        // --- 状態問い合わせ ---
        boolean isAlive(long now)      { return now < deadline; }
        boolean isResumable(long now)  { return resumeAt > 0 && now >= resumeAt; }
        boolean hasMoreSteps()         { return index < steps.size(); }
        @Nullable Step currentStep()   { return hasMoreSteps() ? steps.get(index) : null; }
        void advance()                 { index++; }

        String currentChantWord() {
            if (index < chantWords.size()) return chantWords.get(index);
            return chantWords.isEmpty() ? "" : chantWords.get(chantWords.size() - 1);
        }

        /** steps と subFlags を同時に挿入・整列 */
        void insertSteps(int at, List<Step> injected) {
            var newSteps = new ArrayList<>(steps);

            for (int i = 0; i < injected.size(); i++) {
                int pos = Math.min(at + i, newSteps.size());
                newSteps.add(pos, injected.get(i));
            }

            this.steps = List.copyOf(newSteps);
        }
    }

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    /* ===== 公開API（ファサード） ===== */
    public static void startChain(ServerLevel level, @Nullable ServerPlayer player,
                                  List<Step> steps, @Nullable DataBag bag,
                                  int timeoutTicks, String raw,
                                  @Nullable List<String> st) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(steps, "steps");

        Session s = new Session(level, player, steps, bag, timeoutTicks, raw, st);
        SESSIONS.put(s.playerId, s);
        ensureTicker();

        new ChantRunner(s, player).run();   // ← 実行は別クラスへ委譲
    }

    public static boolean signal(@Nullable ServerPlayer player, String token) {
        return signalAndInject(player, token, List.of());
    }

    public static boolean signalAndInject(@Nullable ServerPlayer player, String token, List<Step> steps) {
        if (player == null) return false;
        Session s = SESSIONS.get(player.getUUID());
        if (s == null || !token.equals(s.waitToken)) return false;

        s.waitToken = null;
        if (steps != null && !steps.isEmpty()) {
            s.steps = removeWaitAndInject(s.steps, s.index, steps);
        }
        new ChantRunner(s, player).run();
        return true;
    }

    public static boolean cancel(@Nullable ServerPlayer player) {
        return player != null && SESSIONS.remove(player.getUUID()) != null;
    }

    /* ===== 実行ロジック（責務分離） ===== */
    private static final class ChantRunner {
        private final Session s;
        private final @Nullable ServerPlayer player;
        private final MagicContext ctx;

        ChantRunner(Session s, @Nullable ServerPlayer player) {
            this.s = s;
            this.player = player;
            this.ctx = new MagicContext(s.level, player, s.bag, s);
        }

        void run() {
            ensurePowerComputed();

            while (s.hasMoreSteps()) {
                Step step = s.currentStep();

                // 1. WAIT判定
                if (isWait(step)) {
                    s.waitToken = step.args().getString("_wait_token");
                    return; // 中断
                }

                // 2. 実行前準備
                ctx.updateIndex(s.index);
                ctx.data().put(Keys.CHANT, s.currentChantWord());

                // 3. ステップ実行
                boolean isSub = s.steps.get(s.index).isSub();
                MagicClassRegistry.call(step.id(), ctx, safeArgs(step.args()), s.power, isSub);

                s.advance();

                // 4. 後処理（挿入 → 遅延 → キャンセル）
                applyInjections();
                if (applyDelay()) return;
                if (applyCancel()) return;
            }

            // 完了
            SESSIONS.remove(s.playerId);
        }

        private void ensurePowerComputed() {
            if (s.bag.get(Keys.POWER).isEmpty()) {
                s.power = ChantScorer.score(s.bag.get(Keys.CHANT_RAW).orElse(null), player) / 2.0f;
                ctx.data().put(Keys.POWER, s.power);
            }
        }

        private void applyInjections() {
            List<Step> injected = ctx.drainInsertions();
            if (!injected.isEmpty()) {
                s.insertSteps(s.index, injected);
            }
        }

        private boolean applyDelay() {
            int delay = ctx.drainDelay();
            if (delay > 0) {
                s.resumeAt = s.level.getServer().getTickCount() + delay;
                return true; // 中断
            }
            return false;
        }

        private boolean applyCancel() {
            return ctx.consumeCancel();
        }
    }

    private static List<Step> removeWaitAndInject(List<Step> steps, int waitIndex, List<Step> inject) {
        var list = new ArrayList<>(steps);
        if (waitIndex >= 0 && waitIndex < list.size()) list.remove(waitIndex);
        list.addAll(waitIndex, inject);
        return List.copyOf(list);
    }

    private static boolean isWait(Step step) {
        return WAIT_ID.equals(step.id())
                && step.args() != null
                && step.args().contains("_wait_token");
    }

    private static final ResourceLocation WAIT_ID = MagicChants.rl("_wait");
    private static CompoundTag safeArgs(@Nullable CompoundTag t) { return t != null ? t : new CompoundTag(); }

    /* ===== サーバーtick監視（タイムアウト自動掃除） ===== */

    private static volatile boolean TICKER_INSTALLED = false;

    private static void ensureTicker() {
        if (TICKER_INSTALLED) return;
        TICKER_INSTALLED = true;
        MinecraftForge.EVENT_BUS.register(new ServerTicker());
    }

    public static final class ServerTicker {
        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            long now = e.getServer().getTickCount();

            // タイムアウト掃除
            SESSIONS.values().removeIf(s -> now >= s.deadline);

            // 再開処理：Iteratorで安全に削除・再開
            for (Session s : SESSIONS.values()) {
                if (!s.isResumable(now)) continue;

                s.resumeAt = 0L;
                ServerPlayer sp = s.level.getServer().getPlayerList().getPlayer(s.playerId);
                if (sp == null) continue; // プレイヤー不在ならスキップ

                new ChantRunner(s, sp).run();   // ← ここで再開
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
}
