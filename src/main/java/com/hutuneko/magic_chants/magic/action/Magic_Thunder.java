package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.entity.EntityType;

public class Magic_Thunder extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
        var level = ctx.level();
        if (level.isClientSide()) return;
        var vec = ctx.data().get(Keys.POS)
                .orElseGet(() -> ctx.player() != null ? ctx.player().position() : null);
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(vec.x(), vec.y(), vec.z());
        if (ctx.player() != null) bolt.setCause(ctx.player());
        level.addFreshEntity(bolt);
        ctx.delayNext(1);
    }
}
