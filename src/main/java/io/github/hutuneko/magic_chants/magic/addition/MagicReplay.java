package io.magic_chants.magic.addition;

import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicCast;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;

public class MagicReplay extends Magic {

    @Override
    public void mainMagic(MagicContext ctx) {
        MagicCast.Step front = ctx.peekPreviousMain();
        if (front == null) return;
        ctx.enqueueNext(copyOf(front));
    }

    private static MagicCast.Step copyOf(MagicCast.Step s) {
        return MagicCast.Step.main(s.id(),s.args());
    }

    @Override
    public void subMagic(MagicContext ctx) {
        MagicCast.Step main = ctx.peekMain();
        if (main == null) return;
        ctx.enqueueNext(copyOf(main));
    }
}
