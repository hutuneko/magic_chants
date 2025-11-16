package com.hutuneko.magic_chants.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TickTaskManager {
    private static final List<TickTask> tasks = new ArrayList<>();

    public static void addTask(int ticks, Runnable action, Supplier<Boolean> stopper) {
        tasks.add(new TickTask(ticks, action, stopper));
    }


    public static void onTick() {
        tasks.removeIf(TickTask::tick);
    }
}
