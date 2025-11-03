package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.phys.Vec3;

public class Magic_Teleport extends Magic {
    private Vec3 pos;

    @Override
    public void magic_content(MagicContext ctx) {
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