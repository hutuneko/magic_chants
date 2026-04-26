package io.github.hutuneko.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.phys.Vec3;

public class MagicTeleport extends Magic {
    private Vec3 pos;

    @Override
    public void mainMagic(MagicContext ctx) {
        var p = ctx.data().get(Keys.POS).orElse(null);
        if (p == null) return;
        pos = p;
        var level = ctx.level();
        if (level.isClientSide()) return;
        var id = ctx.data().get(Keys.TARGET_UUID).orElse(null);
        if (id == null) return;
        var entity = level.getEntity(id);
        if (entity == null) return;
        entity.teleportTo(pos.x, pos.y, pos.z);
    }
}