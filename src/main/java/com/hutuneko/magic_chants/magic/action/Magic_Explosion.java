package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class Magic_Explosion extends Magic {
    private float power = 4.0F;

    public Magic_Explosion() {}

    public Magic_Explosion(CompoundTag args) {
        if (args != null && args.contains("power")) {
            this.power = args.getFloat("power");
        }
    }
    @Override
    public void magic_content(MagicContext ctx) {
        System.out.println(0);
        var level = ctx.level();
        if (level.isClientSide()) return;

        var vec = ctx.data().get(Keys.POS)
                .orElseGet(() -> ctx.player() != null ? ctx.player().position() : null);
        System.out.println(1);
        if (vec == null) return;
        BlockPos pos = new BlockPos((int) vec.x(), (int) vec.y(), (int) vec.z());

        level.explode(
                ctx.player(),                   // 爆破を起こしたエンティティ（不要なら null）
                vec.x(), vec.y(), vec.z(), // 座標
                this.power,                   // 爆破の威力（TNT は 4.0F 相当）
                Level.ExplosionInteraction.TNT // ブロックへの影響（NONE/BLOCK/TNT など）
        );
        ctx.delayNext(1);
    }
}
