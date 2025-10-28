package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.api.magic.*;

public class Magic_Replay extends SubWithMagic {

    @Override
    public void magic_content(MagicContext ctx) {
        var p = ctx.data().get(Keys.POWER).orElse(null);
        if (p == null)return;

        MagicCast.Step front = ctx.peekFront();
        if (front == null) return;
        ctx.enqueueNext(copyOf(front));
    }

    private static MagicCast.Step copyOf(MagicCast.Step s) {
        return new MagicCast.Step(s.id(), s.args().copy());
    }

    @Override
    public void sub_magic(MagicContext ctx) {
        var p = ctx.data().get(Keys.POWER).orElse(null);
        if (p == null)return;

        MagicCast.Step front = ctx.peekMain();
        if (front == null) return;
        ctx.enqueueNext(copyOf(front));
    }
}
