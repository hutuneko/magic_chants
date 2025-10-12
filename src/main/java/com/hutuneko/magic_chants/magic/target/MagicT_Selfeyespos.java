package com.hutuneko.magic_chants.magic.target;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

public class MagicT_Selfeyespos extends Magic {
    public MagicT_Selfeyespos() {}
    private double reach = 32.0;

    @Override
    public void magic_content(MagicContext ctx) {
        var p = ctx.player();
        if (p == null) return;
        var r = ctx.data().get(Keys.POWER).orElse(null);
        if (!(r == null))reach = reach * r;
        var eye  = p.getEyePosition(1.0f);
        var look = p.getLookAngle();
        var end  = eye.add(look.scale(reach));

        var hit = p.level().clip(new ClipContext(
                eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, p));
        var tgt = (hit.getType() != HitResult.Type.MISS) ? hit.getLocation() : end;

        ctx.data().put(Keys.POS, tgt);
        System.out.println("[Self] put POS=" + tgt);
    }
}
