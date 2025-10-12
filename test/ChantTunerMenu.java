package com.hutuneko.magic_chants.api.block.gui;

import com.hutuneko.magic_chants.ModRegistry;
import com.hutuneko.magic_chants.api.chat.dictionary.IPlayerAliases;
import com.hutuneko.magic_chants.block.ChantTunerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChantTunerMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final List<IPlayerAliases.AliasRule> snapshot;
    private final IItemHandler handler;

    public ChantTunerMenu(int id, Inventory inv, BlockPos pos, List<IPlayerAliases.AliasRule> serverRules,
                          IItemHandler handler) {
        super(ModRegistry.CHANT_TUNER_MENU.get(), id);
        this.pos = pos;
        this.snapshot = serverRules;
        this.handler = handler;
    }

    // ★ クライアント（MenuType factory）
    public ChantTunerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(ModRegistry.CHANT_TUNER_MENU.get(), id);

        // 1) 位置を受け取る
        this.pos = buf.readBlockPos();

        // 2) クライアント側のBEとハンドラを解決（無ければダミー）
        var level = inv.player.level();
        IItemHandler h = new ItemStackHandler(1); // ダミー1スロット
        List<IPlayerAliases.AliasRule> snap = List.of();

        if (level != null && level.getBlockEntity(this.pos) instanceof ChantTunerBE be) {
            h = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(h);
            snap = List.copyOf(be.getRules());
        }

        this.handler = h;
        this.snapshot = snap;

        // 3) ★ 必ずスロットを追加（ここが抜けていると今回の例外になる）
        this.addSlot(new SlotItemHandler(handler, 0, 12, 20));

        // 4) プレイヤーインベントリを追加
        final int xBase = 8, yBase = 84;
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, xBase + col * 18, yBase + row * 18));
        for (int hot = 0; hot < 9; ++hot)
            this.addSlot(new Slot(inv, hot, xBase + hot * 18, yBase + 58));
    }

    @Override public boolean stillValid(Player p) {
        return p.level().getBlockEntity(pos) instanceof ChantTunerBE;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack in = slot.getItem();
            ret = in.copy();
            if (index == 0) { // BE→プレイヤー
                if (!this.moveItemStackTo(in, 1, 37, true)) return ItemStack.EMPTY;
            } else {          // プレイヤー→BE
                if (!this.moveItemStackTo(in, 0, 1, false)) return ItemStack.EMPTY;
            }
            if (in.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return ret;
    }
}
