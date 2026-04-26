package io.github.hutuneko.magic_chants.item;

import io.github.hutuneko.magic_chants.api.magic.MagicCast;
import io.github.hutuneko.magic_chants.magic.action.MagicBindSword;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MagicSwordItem extends Item {
    public MagicSwordItem(Properties props) {
        super(props.sword(ToolMaterial.IRON,2,1));
    }

    @Override
    public void postHurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NonNull LivingEntity attacker) {
        var cdata = stack.get(DataComponents.CUSTOM_DATA);
        if (cdata == null) return;
        CompoundTag tag = cdata.copyTag();
        if (tag.contains(MagicBindSword.NBT_KEY_CHAIN)) {
            if (attacker.level() instanceof ServerLevel && attacker instanceof ServerPlayer sp) {
                // チェーン復元（既存）
                var chain = tag.getList(MagicBindSword.NBT_KEY_CHAIN).orElse(null);
                if (chain == null) return;
                List<MagicCast.Step> steps = readChain(chain);
                // ★ NBT から詠唱テキスト取得（無ければ空文字）
                String chantRaw = tag.getStringOr("magic_chants:chant_raw","");

                MagicCast.startChain((ServerLevel) attacker.level(), sp, steps, null, 200, chantRaw,null);
                stack.hurtAndBreak(1,sp,sp.getUsedItemHand());
            }
        }
    }


    private static List<MagicCast.Step> readChain(ListTag list) {
        List<MagicCast.Step> out = new ArrayList<>();
        for (Tag t : list) {
            CompoundTag c = (CompoundTag) t;
            Identifier id = Identifier.parse(String.valueOf(c.getString("id")));
            CompoundTag args = c.getCompound("args").orElse(null);
            MagicCast.ChantSource source = MagicCast.ChantSource.valueOf(c.getString("source").orElse(""));
            out.add(new MagicCast.Step(id, args,source));
        }
        return out;
    }
    @Override
    public int getMaxDamage(ItemStack stack) {
        var cdata = stack.get(DataComponents.CUSTOM_DATA);
        if (cdata == null) return super.getMaxDamage(stack);
        CompoundTag tag = cdata.copyTag();
        if (tag.contains("CustomUses")) {
            return tag.getIntOr("CustomUses",super.getMaxDamage(stack));
        }
        return super.getMaxDamage(stack);
    }
}
