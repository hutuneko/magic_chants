package com.hutuneko.magic_chants.item;

import com.hutuneko.magic_chants.api.chat.MagicChatHook;
import com.hutuneko.magic_chants.api.chat.MagicChatServer;
import com.hutuneko.magic_chants.api.chat.item.ChantItemUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MagicWandItem extends Item {
    public MagicWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var uuid = ChantItemUtil.ensureUuid(stack);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            MagicChatServer.setCurrent(sp, uuid, hand,stack);
        }
        if (level.isClientSide) {
            MagicChatHook.openMagicChatSession(uuid,hand,stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
