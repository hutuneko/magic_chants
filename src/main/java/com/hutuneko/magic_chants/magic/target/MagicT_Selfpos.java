package com.hutuneko.magic_chants.magic.target;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ClipContext;

public class MagicT_Selfpos extends Magic {
    public MagicT_Selfpos() {}
    public MagicT_Selfpos(CompoundTag args) {
        if (args.contains("reach")) this.reach = args.getDouble("reach");
    }
    private double reach = 64.0;

    @Override
    public void magic_content(MagicContext ctx) {
        var p = ctx.player();
        if (p == null) return;

        var eye  = p.getEyePosition(1.0f);
        var look = p.getLookAngle();
        var end  = eye.add(look.scale(reach));

        var hit = p.level().clip(new ClipContext(
                eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, p));
        var tgt = p.position();
        ctx.data().put(Keys.POS, tgt);
    }
}
