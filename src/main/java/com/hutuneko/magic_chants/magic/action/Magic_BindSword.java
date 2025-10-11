package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.item.MagicItems;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;

import java.util.List;

/** 直後以降のチェーンを“剣に封入”して、以後の発動を剣攻撃トリガに委ねる */
public class Magic_BindSword extends Magic {
    private int uses = 1;
    public static final String NBT_KEY_CHAIN = "magic_chants:bound_chain";
    public static final String NBT_KEY_USES  = "magic_chants:uses";

    public Magic_BindSword() {}
    public Magic_BindSword(CompoundTag args) {
        if (args != null) {
            if (args.contains("uses")) this.uses = Mth.clamp(args.getInt("uses"), 1, 999);
            else if (args.contains("power")) this.uses = Mth.clamp(args.getInt("power"), 1, 999); // 互換
        }
    }

    @Override
    public void magic_content(MagicContext ctx) {
        ServerPlayer player = ctx.player();
        Level level = ctx.level();
        if (player == null || level.isClientSide()) return;

        // 直後から終わりまでの Step を覗く
        List<MagicCast.Step> rest = ctx.peekRest();
        if (rest.isEmpty()) return; // 封入する中身が無い

        // チェーンを NBT にシリアライズ
        ListTag chain = new ListTag();
        for (MagicCast.Step s : rest) {
            CompoundTag one = new CompoundTag();
            one.putString("id", s.id().toString());
            one.put("args", s.args().copy());
            chain.add(one);
        }
        String chantRaw = ctx.data().get(Keys.CHANT_RAW).orElse("");

        ItemStack sword = new ItemStack(MagicItems.MAGIC_SWORD.get());
        sword.getOrCreateTag().put(NBT_KEY_CHAIN, chain);
        sword.getOrCreateTag().putInt(NBT_KEY_USES, uses);
        sword.getOrCreateTag().putInt("CustomUses", this.uses); // 最大耐久
        if (!chantRaw.isEmpty()) {
            sword.getOrCreateTag().putString("magic_chants:chant_raw", chantRaw);
        }
        sword.getOrCreateTag().put(NBT_KEY_CHAIN, chain);
        sword.getOrCreateTag().putInt(NBT_KEY_USES, uses);
        sword.getOrCreateTag().putInt("CustomUses", this.uses); // 最大耐久を上書き
        sword.setDamageValue(0);
        // プレイヤーに付与（手持ちが埋まっていれば落とす）
        if (!player.getInventory().add(sword)) {
            player.spawnAtLocation(sword, 0.5f);
        }

        // ここで実行チェーンを終了（以後は剣がトリガ）
        ctx.requestCancel();
    }
}
