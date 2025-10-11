package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.api.magic.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class Magic_DelayNext extends Magic {
    private int ticks = 20; // 1秒
    public Magic_DelayNext() {}
    public Magic_DelayNext(CompoundTag args) {
        if (args != null && args.contains("ticks")) this.ticks = Mth.clamp(args.getInt("ticks"), 0, 20 * 60 * 5);
    }
    @Override public void magic_content(MagicContext ctx) {
        if (ticks > 0) ctx.delayNext(ticks);
    }
}

