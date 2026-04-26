package io.github.hutuneko.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.ModRegistry;
import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicCast;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import io.github.hutuneko.magic_chants.api.util.MagicChantsAPI;
import io.github.hutuneko.magic_chants.item.MagicItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/** 直後以降のチェーンを“剣に封入”して、以後の発動を剣攻撃トリガに委ねる */
public class MagicBindSword extends Magic {
    private int uses = 1;
    public static final String NBT_KEY_CHAIN = "magic_chants:bound_chain";
    public static final String NBT_KEY_SUB = "magic_chants:bound_sub";
    public static final String NBT_KEY_USES  = "magic_chants:uses";

    @Override
    public void mainMagic(MagicContext ctx) {
        ServerPlayer player = ctx.player();
        ServerLevel level = ctx.level();
        if (player == null || level.isClientSide()) return;

        // 直後から終わりまでの Step を覗く
        List<MagicCast.Step> rest = ctx.peekRest();
        if (rest.isEmpty()) return;

        ListTag chain = new ListTag();
        ListTag sublist = new ListTag();
        String chantRaw = ctx.data().get(Keys.CHANT_RAW).orElse("");
        uses = ctx.data().get(Keys.INT).orElse(uses);

        ItemStack sword = new ItemStack(ModRegistry.MAGIC_SWORD.get());
        CompoundTag tag = MagicChantsAPI.getOrCreateTag(sword);
        tag.put(NBT_KEY_CHAIN, chain);
        tag.putInt(NBT_KEY_USES, uses);
        tag.putInt("CustomUses", this.uses); // 最大耐久
        if (!chantRaw.isEmpty()) {
            tag.putString("magic_chants:chant_raw", chantRaw);
        }
        tag.put(NBT_KEY_CHAIN, chain);
        tag.put(NBT_KEY_SUB, sublist);
        tag.putInt(NBT_KEY_USES, uses);
        tag.putInt("CustomUses", this.uses); // 最大耐久を上書き
        sword.setDamageValue(0);
        sword.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        // プレイヤーに付与（手持ちが埋まっていれば落とす）
        if (!player.getInventory().add(sword)) {
            player.spawnAtLocation(level,sword, 0.5f);
        }

        // ここで実行チェーンを終了（以後は剣がトリガ）
        ctx.requestCancel();
    }
}
