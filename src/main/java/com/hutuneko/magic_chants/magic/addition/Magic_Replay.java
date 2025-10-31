package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.Magic_chants;
import com.hutuneko.magic_chants.api.magic.*;
import net.minecraft.resources.ResourceLocation;

public class Magic_Replay extends SubWithMagic {

    @Override
    public void magic_content(MagicContext ctx) {
        MagicCast.Step front = ctx.peekFront();
        if (front == null) return;
        ctx.enqueueNext(copyOf(front));
    }

    private static MagicCast.Step copyOf(MagicCast.Step s) {
        return new MagicCast.Step(s.id(), s.args().copy());
    }

    @Override
    public void sub_magic(MagicContext ctx) {
        MagicCast.Step main = ctx.peekMain();
        System.out.println(main);
        if (main == null) return;
        ctx.enqueueNext(copyOf(main));
    }
}
