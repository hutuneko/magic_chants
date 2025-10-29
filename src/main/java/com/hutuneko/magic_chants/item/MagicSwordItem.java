package com.hutuneko.magic_chants.item;

import com.hutuneko.magic_chants.api.magic.*;
import com.hutuneko.magic_chants.magic.action.Magic_BindSword;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MagicSwordItem extends SwordItem {
    public MagicSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties props) {
        super(tier, attackDamageModifier, attackSpeedModifier, props);
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof ServerPlayer sp) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(Magic_BindSword.NBT_KEY_CHAIN)) {
                // チェーン復元（既存）
                List<MagicCast.Step> steps = readChain(tag.getList(Magic_BindSword.NBT_KEY_CHAIN, Tag.TAG_COMPOUND));
                List<Boolean> sub = readsub(tag.getList(Magic_BindSword.NBT_KEY_SUB,Tag.TAG_COMPOUND));
                // ★ NBT から詠唱テキスト取得（無ければ空文字）
                String chantRaw = tag.contains("magic_chants:chant_raw", Tag.TAG_STRING)
                        ? tag.getString("magic_chants:chant_raw")
                        : "";

                // ★ 実行：chantRaw を MagicCast に渡す（Power は MagicCast 側で導出）
                MagicCast.startChain((ServerLevel) attacker.level(), sp, steps, null, 200, chantRaw,sub);

                // 既存：1消費
                stack.hurtAndBreak(1, sp, p -> p.broadcastBreakEvent(sp.getUsedItemHand()));
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }


    private static List<MagicCast.Step> readChain(ListTag list) {
        List<MagicCast.Step> out = new ArrayList<>();
        for (Tag t : list) {
            CompoundTag c = (CompoundTag) t;
            ResourceLocation id = new ResourceLocation(c.getString("id"));
            CompoundTag args = c.getCompound("args");
            out.add(new MagicCast.Step(id, args));
        }
        return out;
    }
    private static List<Boolean> readsub(ListTag list) {
        List<Boolean> out = new ArrayList<>();
        for (Tag t : list) {
            CompoundTag c = (CompoundTag) t;
            Boolean sub = c.getCompound("sub").contains("sub");
            out.add(sub);
        }
        return out;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CustomUses")) {
            return tag.getInt("CustomUses");
        }
        return super.getMaxDamage(stack); // fallback
    }
}
