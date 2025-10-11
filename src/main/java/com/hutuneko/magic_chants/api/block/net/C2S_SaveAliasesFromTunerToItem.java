// C2S_SaveAliasesFromTunerToItem.java （新規）
package com.hutuneko.magic_chants.api.block.net;

import com.hutuneko.magic_chants.api.chat.item.ChantItemUtil;
import com.hutuneko.magic_chants.api.chat.item.dictionary.ItemAliasIO;
import com.hutuneko.magic_chants.block.ChantTunerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public record C2S_SaveAliasesFromTunerToItem(BlockPos pos, String body) {

    public static void encode(C2S_SaveAliasesFromTunerToItem m, FriendlyByteBuf buf){
        buf.writeBlockPos(m.pos);
        buf.writeUtf(m.body);
    }
    public static C2S_SaveAliasesFromTunerToItem decode(FriendlyByteBuf buf){
        return new C2S_SaveAliasesFromTunerToItem(buf.readBlockPos(), buf.readUtf());
    }

    public static void handle(C2S_SaveAliasesFromTunerToItem msg, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;
            ServerLevel lvl = sp.serverLevel();
            var be = lvl.getBlockEntity(msg.pos);
            if (!(be instanceof ChantTunerBE tuner)) return;

            // スロット0のアイテム取得
            IItemHandler handler = tuner.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                    .orElse(null);
            if (handler == null) return;
            ItemStack stack = handler.getStackInSlot(0);
            if (stack.isEmpty()) {
                sp.displayClientMessage(Component.literal("§cアイテムスロットが空です"), false);
                return;
            }

            // 行へ分解（空行・#コメントを除外）
            List<String> lines = Arrays.stream(msg.body.split("\\R"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                    .toList();

            // UUIDを必ず付与 → <world>/data/magic_chants/aliases/<uuid>.json へ書き出し
            UUID id = ChantItemUtil.ensureUuid(stack);
            ItemAliasIO.write(lvl, id, lines);

            sp.displayClientMessage(Component.literal("§aこのアイテムの辞書を保存しました: " + id + "（" + lines.size() + "行）"), false);
            tuner.setChanged();
        });
        c.setPacketHandled(true);
    }
}
