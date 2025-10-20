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
    public MagicWandItem(Properties properties) {
        super(properties);
    }

    // --- クラフト時 ---
    @Override
    public void onCraftedBy(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
        super.onCraftedBy(stack, level, player);
        ChantItemUtil.ensureUuid(stack); // in-placeでも安全（初期生成時）
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    // --- ロード後の補完 ---
    @Override
    public void verifyTagAfterLoad(@NotNull CompoundTag tag) {
        super.verifyTagAfterLoad(tag);
        if (!tag.hasUUID(ChantItemUtil.KEY_UUID)) {
            tag.putUUID(ChantItemUtil.KEY_UUID, UUID.randomUUID());
        }
    }

    // --- Tick更新時（loot・command・pickupなど） ---
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level,
                              @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity instanceof Player player) {
            ChantItemUtil.ensureUuid(stack);
        }
    }

    // --- 右クリック動作 ---
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        UUID uuid;

        // ⚙️ サーバー側：安全な置き換えでUUID付与＋同期
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            uuid = ChantItemUtil.ensureUuidReplace(sp, hand);
            if (player.isShiftKeyDown()) {
                MagicChatServer.setCurrent(sp, uuid, hand, stack);
            } else {
                System.out.println("Wand UUID (server): " + uuid);
            }
        }
        // ⚙️ クライアント側：チャット開く
        else if (level.isClientSide) {
            uuid = ChantItemUtil.getUuid(stack);
            if (player.isShiftKeyDown() && uuid != null) {
                MagicChatHook.openMagicChatSession(uuid, hand, stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
