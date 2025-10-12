package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/** 直後のステップを (times-1) 回 “自分で” 複製申請する Magic（自分は副作用なし） */
public class Magic_RepeatNext extends Magic {
    private int times = 1;

    public Magic_RepeatNext() {}
    public Magic_RepeatNext(CompoundTag args) {
        if (args != null && args.contains("power")) {
            this.times = Mth.clamp(args.getInt("power"), 1, 64);
        }
    }

    @Override
    public void magic_content(MagicContext ctx) {
        if (times <= 1) return; // 1なら何もしない

        MagicCast.Step next = ctx.peekNext();
        if (next == null) return; // 直後が無ければ何もしない

        // Step のディープコピー（argsは NBT を copy）
        for (int k = 1; k < times; k++) {
            ctx.enqueueNext(copyOf(next));
        }
        System.out.println("[repeat_next] times=" + times + ", next=" + (ctx.peekNext()!=null));
        // 自分自身は何もしない（効果は“差し込み”のみ）
    }

    private static MagicCast.Step copyOf(MagicCast.Step s) {
        // あなたの Step 実装に合わせて
        return new MagicCast.Step(s.id(), s.args().copy());
    }
}
