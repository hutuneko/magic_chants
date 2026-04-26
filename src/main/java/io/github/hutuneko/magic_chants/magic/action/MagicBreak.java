package io.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class MagicBreak extends Magic {
    private Vec3 pos;

    @Override
    public void mainMagic(MagicContext ctx) {
        var p = ctx.data().get(Keys.POS).orElse(null);
        if (p == null) return;
        pos = p;
        var level = ctx.level();
        if (level.isClientSide()) return;
        BlockPos blockPos = BlockPos.containing(pos);
        level.destroyBlock(blockPos,true);
    }
}