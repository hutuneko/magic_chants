package com.hutuneko.magic_chants.item;

import com.hutuneko.magic_chants.api.chat.MagicChatHook;
import com.hutuneko.magic_chants.api.chat.MagicChatServer;
import com.hutuneko.magic_chants.api.chat.item.ChantItemUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MagicWandItem extends Item {
    // NBTキー（Utilと同じキーを使うこと）
    private static final String NBT_UUID = "magic_chants:item_uuid";

    public MagicWandItem(Properties properties) {
        super(properties);
    }

    // ① クラフトやレシピ生成で手に入った瞬間に付与
    @Override
    public void onCraftedBy(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
        super.onCraftedBy(stack, level, player);
        ChantItemUtil.ensureUuid(stack); // 無ければランダム生成
    }

    // ② NBTロード時（インベントリやワールド読み込み直後）に欠落を補完
    @Override
    public void verifyTagAfterLoad(@NotNull CompoundTag tag) {
        super.verifyTagAfterLoad(tag);
        if (!tag.contains(NBT_UUID, net.minecraft.nbt.Tag.TAG_STRING)) {
            tag.putString(NBT_UUID, java.util.UUID.randomUUID().toString());
        }
    }

    // ③ 戦利品やコマンド付与等の“その場生成”を最終的にカバー
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level,
                              @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide) {
            ChantItemUtil.ensureUuid(stack);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 念のためここでも（既に付いていれば何もしない）
        ChantItemUtil.ensureUuid(stack);

        UUID uuid = ChantItemUtil.getUuid(stack);
        if (uuid==null) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            MagicChatServer.setCurrent(sp, uuid, hand, stack);
        }
        if (level.isClientSide) {
            MagicChatHook.openMagicChatSession(uuid, hand, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}

