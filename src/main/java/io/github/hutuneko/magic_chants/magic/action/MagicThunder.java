package io.github.hutuneko.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class MagicThunder extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
        var level = ctx.level();
        if (level.isClientSide()) return;
        var vec = ctx.data().get(Keys.POS)
                .orElse(ctx.player() != null ? ctx.player().position() : Vec3.ZERO);
        var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
        if (bolt == null) return;
        bolt.move(MoverType.SELF,vec);
        if (ctx.player() != null) bolt.setCause(ctx.player());
        level.addFreshEntity(bolt);
        ctx.delayNext(1);
    }
}
