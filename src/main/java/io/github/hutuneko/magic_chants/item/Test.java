package io.github.hutuneko.magic_chants.item;

import io.github.hutuneko.magic_chants.api.player.ForgeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Test extends Item {
    public Test(Properties properties) {
        super(properties);
    }
//    @Override
//    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
//        ItemStack stack = player.getItemInHand(hand);
//
//        if (player instanceof ServerPlayer sp) {
//            if (player.isShiftKeyDown()) {
//                ForgeEvent.spiritification(level,sp);
//            } else {
//                ForgeEvent.spiritFormRelease(level,sp);
//            }
//        }
//        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
//    }
}
