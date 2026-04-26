package io.github.hutuneko.magic_chants.magic.addition;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicCast;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;

/** 直後のステップを (times-1) 回 “自分で” 複製申請する Magic（自分は副作用なし） */
public class MagicRepeatNext extends Magic {

    @Override
    public void mainMagic(MagicContext ctx) {
        var times = ctx.data().get(Keys.POWER).orElse(null);
        if (times == null) return;
        if (times <= 1) return;

        MagicCast.Step next = ctx.peekNext();
        if (next == null) return; // 直後が無ければ何もしない

        // Step のディープコピー（argsは NBT を copy）
        for (int k = 1; k < times; k++) {
            ctx.enqueueNext(next.copy());
        }
        System.out.println("[repeat_next] times=" + times + ", next=" + (ctx.peekNext()!=null));
        // 自分自身は何もしない（効果は“差し込み”のみ）
    }
}
