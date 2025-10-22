package com.hutuneko.magic_chants.block;

import com.hutuneko.magic_chants.ModRegistry;
import com.hutuneko.magic_chants.api.block.gui.ChantTunerMenu;
import com.hutuneko.magic_chants.api.chat.dictionary.IPlayerAliases;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChantTunerBE extends BlockEntity implements MenuProvider {
    // 追記: キャッシュ用フィールド（GUI表示やパケット用に）
    private @Nullable java.util.UUID cachedItemUuid = null;
    private int    cachedUses  = 0;
    private float  cachedPower = 0f;
    private String cachedChant = "";
    private List<IPlayerAliases.AliasRule> rules;

    // ★ 1スロットのインベントリ
    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
            if (level != null && !level.isClientSide) {
                handleItemChanged(slot, getStackInSlot(slot)); // ←★ NBT読む
            }
        }
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) { return !stack.isEmpty(); }
    };

    public ChantTunerBE(BlockPos pos, BlockState state) {
        super(ModRegistry.CHANT_TUNER_BE.get(), pos, state);
    }

    // NBT検知・読み取りロジック
    private void handleItemChanged(int slot, ItemStack stack) {
        // 1スロットしかない前提。複数ならslotで分岐
        if (stack.isEmpty()) {
            // 取り出された：キャッシュをクリア
            cachedItemUuid = null;
            cachedUses = 0;
            cachedPower = 0f;
            cachedChant = "";
            syncToClient();
            return;
        }

        // 置かれた：NBTを読む
        var tag = stack.getTag(); // getOrCreateTag() でもOKだが「無い」判定したいので getTag()
        cachedItemUuid = null;
        cachedUses  = 0;
        cachedPower = 0f;
        cachedChant = "";

        if (tag != null) {
            // UUID（文字列想定）
            final String K_UUID  = "magic_chants:item_uuid";
            final String K_USES  = "magic_chants:uses";
            final String K_POWER = "magic_chants:power";
            final String K_CHANT = "magic_chants:chant_raw";

            if (tag.contains(K_UUID, Tag.TAG_STRING)) {
                try {
                    cachedItemUuid = java.util.UUID.fromString(tag.getString(K_UUID));
                } catch (IllegalArgumentException ignored) {}
            }
            if (tag.contains(K_USES, Tag.TAG_INT)) {
                cachedUses = tag.getInt(K_USES);
            }
            // float は NBT では TAG_FLOAT(5)。double で入れてる場合は TAG_DOUBLE(6) もケア可
            if (tag.contains(K_POWER, Tag.TAG_FLOAT)) {
                cachedPower = tag.getFloat(K_POWER);
            } else if (tag.contains(K_POWER, Tag.TAG_DOUBLE)) {
                cachedPower = (float) tag.getDouble(K_POWER);
            }
            if (tag.contains(K_CHANT, Tag.TAG_STRING)) {
                cachedChant = tag.getString(K_CHANT);
            }
        }

        // 必要ならここで外部ファイル/I/O も：cachedItemUuid をキーに辞書読み込みなど

        // クライアントへ同期（画面の即時更新が必要ならパケット化も検討）
        syncToClient();

        // デバッグ
        System.out.printf("[ChantTunerBE] slot=%d uuid=%s uses=%d power=%.2f chant='%s'%n",
                slot, cachedItemUuid, cachedUses, cachedPower, cachedChant);
    }

    private void syncToClient() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.nullToEmpty("null");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new ChantTunerMenu(id, inv, player);
    }
    public void setRules(List<IPlayerAliases.AliasRule> newRules) {
        this.rules.clear();
        this.rules.addAll(newRules);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
