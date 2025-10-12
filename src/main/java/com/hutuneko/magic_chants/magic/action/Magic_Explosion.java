package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class Magic_Explosion extends Magic {
    private float power = 4.0F;
    @Override
    public void magic_content(MagicContext ctx) {
        var level = ctx.level();
        if (level.isClientSide()) return;
        var p = ctx.data().get(Keys.POWER).orElse(null);
        if (p == null)return;
        power = p;
        var vec = ctx.data().get(Keys.POS)
                .orElseGet(() -> ctx.player() != null ? ctx.player().position() : null);
        if (vec == null) return;
        BlockPos pos = new BlockPos((int) vec.x(), (int) vec.y(), (int) vec.z());

        level.explode(
                ctx.player(),                   // 爆破を起こしたエンティティ（不要なら null）
                vec.x(), vec.y(), vec.z(), // 座標
                power,                   // 爆破の威力（TNT は 4.0F 相当）
                Level.ExplosionInteraction.TNT // ブロックへの影響（NONE/BLOCK/TNT など）
        );
        ctx.delayNext(1);
    }
}
