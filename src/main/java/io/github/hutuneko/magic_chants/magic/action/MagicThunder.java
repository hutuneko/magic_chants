package io.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.entity.EntityType;

public class MagicThunder extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
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
