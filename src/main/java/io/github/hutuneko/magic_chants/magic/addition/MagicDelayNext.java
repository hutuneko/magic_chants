package io.github.hutuneko.magic_chants.magic.addition;

import io.github.hutuneko.magic_chants.api.magic.*;
import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;

public class MagicDelayNext extends Magic {
    private int ticks = -1; // 1秒
    @Override public void mainMagic(MagicContext ctx) {
        ticks = ctx.data().get(Keys.INT).orElse(ticks);
        if (ticks == -1) return;
        if (ticks > 0) ctx.delayNext(ticks);
    }
}

