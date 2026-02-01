package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.Magic_chants;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.magic.SubWithMagic;

import java.util.List;

public class Magic_And extends SubWithMagic {
    @Override
    public void magic_content(MagicContext ctx) {}

    @Override
    public void sub_magic(MagicContext ctx) {
        if (ctx.player() != null) {
            List<Boolean> sub = MagicCast.SUBLIST.get(ctx.player().getUUID());
            if (sub == null) return;

            int scanLimit = sub.size() - 1;

            for (int i = 0; i < scanLimit; i++) {
                if (!sub.get(i)) {
                    // 次が「メイン(false)」なら、その間に割り込ませる
                    boolean nextIsMain = (i + 1 < sub.size()) || !sub.get(i + 1);

                    if (nextIsMain) {
                        MagicCast.Step step = ctx.peekNext();
                        if (step != null) {
                            ctx.enqueueIndex(i + 1 - ctx.getSessionIndex(), copyOf(step));
                            break;
                        }
                    }
                }
            }
            int currentIdx = ctx.getSessionIndex() + 1;
            Magic_chants.LOGGER.error(currentIdx + " currentIdx");
        }
    }
    private static MagicCast.Step copyOf(MagicCast.Step s) {
        // あなたの Step 実装に合わせて
        return new MagicCast.Step(s.id(), s.args().copy());
    }
}
