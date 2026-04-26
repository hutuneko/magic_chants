package io.magic_chants.magic.target;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.level.ClipContext;

public class MagicSelfpos extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
        var p = ctx.player();
        if (p == null) return;

        var eye  = p.getEyePosition(1.0f);
        var look = p.getLookAngle();
        var end  = eye.add(look.scale(64.0));

        var hit = p.level().clip(new ClipContext(
                eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, p));
        var tgt = p.position();
        ctx.data().put(Keys.POS, tgt);
    }
}
