package io.github.hutuneko.magic_chants.api.magic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class MagicContext {
    private final ServerLevel level;
    @Nullable private final ServerPlayer player;
    private final DataBag data;
    private final MagicCast.Session session;

    private int currentIndex = -1;
    private final List<QueuedInsertion> insertions = new ArrayList<>();
    private int delayTicks = 0;
    private boolean cancelRequested = false;

    // --- 差し込み要求 ---
    private record QueuedInsertion(InsertPosition pos, List<MagicCast.Step> steps, int atIndex) {}

    MagicContext(ServerLevel level, @Nullable ServerPlayer player, DataBag data, MagicCast.Session session) {
        this.level = level; this.player = player; this.data = data; this.session = session;
    }

    /* package */ void updateIndex(int index) { this.currentIndex = index; }

    // === 公開API ===
    public ServerLevel level() { return level; }
    @Nullable public ServerPlayer player() { return player; }
    public DataBag data() { return data; }

    public String getChant(){
        return data.get(Keys.CHANT).orElse(null);
    }
    // === peek系：Sessionから直接導出 ===
    public @Nullable MagicCast.Step current() {
        return (currentIndex >= 0 && currentIndex < session.steps.size())
                ? session.steps.get(currentIndex) : null;
    }

    public @Nullable MagicCast.Step peekNext() {
        return peekRelative(+1);
    }

    public @Nullable MagicCast.Step peekPrevious() {
        return peekRelative(-1);
    }

    /** 次のメイン（非サブ）ステップを探す */
    public @Nullable MagicCast.Step peekNextMain() {
        for (int i = currentIndex + 1; i < session.steps.size(); i++) {
            if (isMainStep(i)) return session.steps.get(i);
        }
        return null;
    }
    public @Nullable MagicCast.Step peekPreviousMain() {
        for (int i = currentIndex + 1; i > session.steps.size(); i--) {
            if (isMainStep(i)) return session.steps.get(i);
        }
        return null;
    }

    /** 現在位置から見た「直近のメイン」＝自分がサブなら親、自分がメインなら次のメイン */
    public @Nullable MagicCast.Step peekMain() {
        // 自分がサブなら、手前を遡って親を探す
        if (currentIndex > 0 && isSubStep(currentIndex)) {
            for (int i = currentIndex - 1; i >= 0; i--) {
                if (isMainStep(i)) return session.steps.get(i);
            }
            return null;
        }
        // 自分がメインなら、次のメインを探す
        return peekNextMain();
    }

    public List<MagicCast.Step> peekRest() {
        if (currentIndex < 0 || currentIndex >= session.steps.size()) return List.of();
        return List.copyOf(session.steps.subList(currentIndex, session.steps.size()));
    }

    private @Nullable MagicCast.Step peekRelative(int offset) {
        int idx = currentIndex + offset;
        return (idx >= 0 && idx < session.steps.size()) ? session.steps.get(idx) : null;
    }

    private boolean isMainStep(int idx) {
        return session.steps.get(idx).isMain();
    }

    private boolean isSubStep(int idx) {
        return session.steps.get(idx).isSub();
    }

    // === 差し込みAPI ===

    /** 今の直後に差し込む（最も一般的） */
    public void enqueueNext(MagicCast.Step step) {
        enqueueNext(List.of(step));
    }

    public void enqueueNext(List<MagicCast.Step> steps) {
        if (steps != null && !steps.isEmpty()) {
            insertions.add(new QueuedInsertion(InsertPosition.IMMEDIATE_NEXT, new ArrayList<>(steps), -1));
        }
    }

    /** 次のメイン魔法の前に割り込む */
    public void insertBeforeNextMain(MagicCast.Step step) {
        insertBeforeNextMain(List.of(step));
    }

    public void insertBeforeNextMain(List<MagicCast.Step> steps) {
        if (steps != null && !steps.isEmpty()) {
            insertions.add(new QueuedInsertion(InsertPosition.BEFORE_NEXT_MAIN, new ArrayList<>(steps), -1));
        }
    }

    /** 残り全部の末尾に追加（最後尾へ） */
    public void appendToRest(MagicCast.Step step) {
        appendToRest(List.of(step));
    }

    public void appendToRest(List<MagicCast.Step> steps) {
        if (steps != null && !steps.isEmpty()) {
            insertions.add(new QueuedInsertion(InsertPosition.APPEND_TO_REST, new ArrayList<>(steps), -1));
        }
    }

    /** 指定インデックスに直接（低レベル・要注意） */
    public void insertAt(int index, MagicCast.Step step) {
        insertAt(index, List.of(step));
    }

    public void insertAt(int index, List<MagicCast.Step> steps) {
        if (steps != null && !steps.isEmpty() && index >= 0) {
            insertions.add(new QueuedInsertion(InsertPosition.AT_INDEX, new ArrayList<>(steps), index));
        }
    }

    // === 遅延・キャンセル ===
    public void delayNext(int ticks) {
        if (ticks > 0) delayTicks = Math.max(delayTicks, ticks);
    }

    public void requestCancel() { cancelRequested = true; }

    // === package-private ドレイン ===

    /** すべての差し込みを解決して、最終的なStepリストを返す */
    List<MagicCast.Step> drainInsertions() {
        if (insertions.isEmpty()) return List.of();

        // 現在のstepsを可変コピーに
        var steps = new ArrayList<>(session.steps);

        for (QueuedInsertion q : insertions) {
            int insertAt = switch (q.pos) {
                case IMMEDIATE_NEXT -> currentIndex + 1;
                case BEFORE_NEXT_MAIN -> findNextMainIndex(steps, currentIndex + 1);
                case APPEND_TO_REST -> steps.size();
                case AT_INDEX -> Math.min(q.atIndex, steps.size());
            };

            // 挿入
            for (int i = 0; i < q.steps.size(); i++) {
                int pos = Math.min(insertAt + i, steps.size());
                steps.add(pos, q.steps.get(i));
            }
        }

        // Sessionに反映
        session.steps = List.copyOf(steps);

        insertions.clear();
        return List.copyOf(steps);
    }

    int drainDelay() {
        int d = delayTicks;
        delayTicks = 0;
        return d;
    }

    boolean consumeCancel() {
        boolean c = cancelRequested;
        cancelRequested = false;
        return c;
    }

    private int findNextMainIndex(List<MagicCast.Step> steps, int from) {
        for (int i = from; i < steps.size(); i++) {
            if (steps.get(i).isMain()) return i;
        }
        return steps.size(); // 見つからなければ末尾
    }
    public enum InsertPosition {
        IMMEDIATE_NEXT,      // 今の直後（index+1）
        BEFORE_NEXT_MAIN,    // 次のメイン魔法の前
        APPEND_TO_REST,      // 残りの末尾に追加
        AT_INDEX             // 指定インデックス（低レベル）
    }
}