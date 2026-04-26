package io.github.hutuneko.magic_chants.magic.addition;

import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicCast;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;

public class MagicAnd extends Magic {

    @Override
    public void subMagic(MagicContext ctx) {
        if (ctx.player() != null) {
            MagicCast.Step step = ctx.peekNext();

            if (step != null) {
                ctx.insertBeforeNextMain(copyOf(step));
            }
        }
    }
    private static MagicCast.Step copyOf(MagicCast.Step s) {
        // あなたの Step 実装に合わせて
        return new MagicCast.Step(s.id(), s.args().copy(),s.source());
    }
}
