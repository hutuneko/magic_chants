package com.hutuneko.magic_chants.block;

import com.hutuneko.magic_chants.ModRegistry;
import com.hutuneko.magic_chants.api.block.gui.ChantTunerMenu;
import com.hutuneko.magic_chants.api.chat.dictionary.IPlayerAliases;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChantTunerBE extends BlockEntity implements MenuProvider {
    private final List<IPlayerAliases.AliasRule> rules = new ArrayList<>();

    // ★ 1スロットのインベントリ
    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return !stack.isEmpty(); }
    };
    private final LazyOptional<net.minecraftforge.items.IItemHandler> itemCap = LazyOptional.of(() -> items);

    public ChantTunerBE(BlockPos pos, BlockState st) { super(ModRegistry.CHANT_TUNER_BE.get(), pos, st); }

    @Override public @NotNull Component getDisplayName() {
        return Component.literal("Chant Tuner");
    }

    @Nullable
    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new ChantTunerMenu(id, inv, this);
    }

    public List<IPlayerAliases.AliasRule> getRules() { return rules; }

    /** パケットから渡されたルールを反映 */
    public void setRules(List<IPlayerAliases.AliasRule> newRules) {
        this.rules.clear();
        this.rules.addAll(newRules);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // === 保存/読込（ルール＋アイテムインベントリ） ===
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        // ルール
        ListTag list = new ListTag();
        for (var r : rules) {
            CompoundTag c = new CompoundTag();
            c.putString("type", r.type());
            c.putString("from", r.from());
            c.putString("to", r.to());
            c.putInt("priority", r.priority());
            list.add(c);
        }
        tag.put("rules", list);
        // インベントリ
        tag.put("inv", items.serializeNBT());
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        // ルール
        rules.clear();
        ListTag list = tag.getList("rules", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag c = (CompoundTag) t;
            rules.add(new IPlayerAliases.AliasRule(
                    c.getString("type"),
                    c.getString("from"),
                    c.getString("to"),
                    c.getInt("priority")
            ));
        }
        // インベントリ
        if (tag.contains("inv", Tag.TAG_COMPOUND)) {
            items.deserializeNBT(tag.getCompound("inv"));
        }
    }

    // === Cap: IItemHandler ===
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }
}
