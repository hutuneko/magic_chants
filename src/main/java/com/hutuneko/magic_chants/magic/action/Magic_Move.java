package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Magic_Move extends Magic {
    private float power = 0.0F;
    @Override
    public void magic_content(MagicContext ctx) {
        var p = ctx.data().get(Keys.POWER).orElse(null);
        if (p == null)return;
        power = p;
        var level = ctx.level();
        if (level.isClientSide()) return;
        var entity = level.getEntity(ctx.data().get(Keys.TARGET_UUID).orElse(null));
        if (entity == null) return;
        var eye  = entity.getEyePosition(1.0f);
        var look = entity.getLookAngle();
        var end  = eye.add(look.scale(power));

        var hit = level.clip(new ClipContext(
                eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity));
        Vec3 tgt = (hit.getType() != HitResult.Type.MISS) ? hit.getLocation() : end;
        entity.teleportTo(tgt.x, tgt.y, tgt.z);
    }
}