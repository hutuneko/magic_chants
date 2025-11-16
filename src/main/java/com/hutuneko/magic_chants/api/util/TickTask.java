package com.hutuneko.magic_chants.api.util;

import java.util.function.Supplier;

public class TickTask {
    private int remaining;
    private final Runnable action;
    private final Supplier<Boolean> stopper; // true を返したら停止

    public TickTask(int ticks, Runnable action, Supplier<Boolean> stopper) {
        this.remaining = ticks;
        this.action = action;
        this.stopper = stopper;
    }

    public boolean tick() {
        // 停止条件が true なら即終了
        if (stopper != null && stopper.get()) {
            return true;
        }

        if (remaining-- > 0) {
            action.run();
        }
        return remaining <= 0;
    }
}

