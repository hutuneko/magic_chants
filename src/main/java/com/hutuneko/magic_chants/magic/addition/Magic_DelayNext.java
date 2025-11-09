package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.api.magic.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class Magic_DelayNext extends Magic {
    private int ticks = -1; // 1秒
    @Override public void magic_content(MagicContext ctx) {
        ticks = ctx.data().get(Keys.INT).orElse(ticks);
        if (ticks == -1) return;
        if (ticks > 0) ctx.delayNext(ticks);
    }
}

