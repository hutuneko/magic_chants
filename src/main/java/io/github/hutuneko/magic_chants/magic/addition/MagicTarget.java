package io.github.hutuneko.magic_chants.magic.addition;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

public class MagicTarget extends Magic {

    @Override
    public void mainMagic(MagicContext ctx) {
        var level = ctx.level();
        if (level.isClientSide()) return;
        var pos = ctx.data().get(Keys.POS)
                .orElse(null);
        if (pos == null) return;
        var p = ctx.player();
        if (p == null) return;
        LivingEntity entity = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(10))
                .stream()
                .filter(e -> !(e instanceof Player))
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(pos)))
                .orElse(null);

        if (entity == null)return;
        ctx.data().put(Keys.TARGET_UUID,entity.getUUID());
        System.out.println("["+entity.getUUID()+"]");
    }
}
