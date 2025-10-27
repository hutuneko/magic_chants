package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import com.hutuneko.magic_chants.api.magic.MagicContext;

public class Magic_Replay extends Magic {
    private int times = 1;

    @Override
    public void magic_content(MagicContext ctx) {
        var p = ctx.data().get(Keys.POWER).orElse(null);
        if (p == null)return;

        MagicCast.Step front = ctx.peekFront();
        if (front == null) return; // 直前が無ければ何もしない

        // Step のディープコピー（argsは NBT を copy）
        for (int k = 1; k < times; k++) {
            ctx.enqueueNext(copyOf(front));
        }
        System.out.println("[repeat_next] times=" + times + ", front=" + (ctx.peekNext()!=null));
        // 自分自身は何もしない（効果は“差し込み”のみ）
    }

    private static MagicCast.Step copyOf(MagicCast.Step s) {
        return new MagicCast.Step(s.id(), s.args().copy());
    }
}
