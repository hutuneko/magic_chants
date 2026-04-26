package io.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MagicMove extends Magic {
    private float power = 0.0F;
    @Override
    public void mainMagic(MagicContext ctx) {
        var p = ctx.data().get(Keys.POWER).orElse(null);
        if (p == null)return;
        power = p;
        var level = ctx.level();
        if (level.isClientSide()) return;
        var id = ctx.data().get(Keys.TARGET_UUID).orElse(null);
        if (id == null) return;
        var entity = level.getEntity(id);
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