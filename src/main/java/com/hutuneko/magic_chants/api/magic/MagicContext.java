// 置き換え：MagicContext.java
package com.hutuneko.magic_chants.api.magic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class MagicContext {
    private int rank;
    private final ServerLevel level;
    @Nullable private final ServerPlayer player;
    private final DataBag data;
    private Supplier<List<MagicCast.Step>> peekRestSupplier;
    private boolean cancelRequested = false;
    // ★ 追加：直後のStepを覗くためのSupplier
    private Supplier<MagicCast.Step> peekNextSupplier;
    private Supplier<MagicCast.Step> peekFrontSupplier;
    // ★ 追加：直後に差し込みたいStepのキュー
    private final ArrayDeque<MagicCast.Step> enqueueNext = new ArrayDeque<>();

    public MagicContext(ServerLevel level, @Nullable ServerPlayer player, DataBag data) {
        this.level = level; this.player = player; this.data = data;
    }

    public ServerLevel level() { return level; }
    @Nullable public ServerPlayer player() { return player; }
    public DataBag data() { return data; }

    /* package */ void _setPeekNextSupplier(Supplier<MagicCast.Step> s) {
        this.peekNextSupplier = s;
    }
    /* package */ void _setPeekFrontSupplier(Supplier<MagicCast.Step> s) {
        this.peekFrontSupplier = s;
    }

    /** 今の直後に来るStepを覗く（無ければnull） */
    public @Nullable MagicCast.Step peekNext() {
        return this.peekNextSupplier != null ? this.peekNextSupplier.get() : null;
    }

    public @Nullable MagicCast.Step peekFront() {
        return this.peekFrontSupplier != null ? this.peekFrontSupplier.get() : null;
    }

    /** 今の直後に差し込みたいStepを申請（複数OK・順序維持） */
    public void enqueueNext(MagicCast.Step step) {
        if (step != null) this.enqueueNext.add(step);
    }
    /* package */ List<MagicCast.Step> _drainEnqueued() {
        var out = new ArrayList<MagicCast.Step>(enqueueNext.size());
        while (!enqueueNext.isEmpty()) out.add(enqueueNext.removeFirst());
        return out;
    }
    private int requestedDelayTicks = 0;

    /** 次へ進む前に待つtick数をリクエスト（複数回呼ばれたら最大値を採用） */
    public void delayNext(int ticks) {
        if (ticks > 0) {
            requestedDelayTicks = Math.max(requestedDelayTicks, ticks);
        }
    }

    /* package */ int _drainRequestedDelay() {
        int t = requestedDelayTicks;
        requestedDelayTicks = 0;
        return t;
    }
    public List<MagicCast.Step> peekRest() {
        return (peekRestSupplier != null) ? peekRestSupplier.get() : List.of();
    }

    /** このチェーン実行をここで終了したい（アイテムへ封入した等） */
    public void requestCancel() { this.cancelRequested = true; }

    /* package */ void _setPeekRestSupplier(Supplier<List<MagicCast.Step>> s) {
        this.peekRestSupplier = s;
    }

    /* package */ boolean _consumeCancelRequest() {
        boolean v = this.cancelRequested;
        this.cancelRequested = false;
        return v;
    }
}
