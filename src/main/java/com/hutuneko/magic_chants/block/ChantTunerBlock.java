package com.hutuneko.magic_chants.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

// ChantTunerBlock.java
public class ChantTunerBlock extends Block implements EntityBlock {
    public ChantTunerBlock(Properties p) { super(p); }
    @Override public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState st) { return new ChantTunerBE(pos, st); }
    @Override
    @SuppressWarnings("deprecation") // 1.20.1ではこのままでOK。将来API分割に備えるなら警告抑止。
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            BlockEntity be = level.getBlockEntity(pos);
            System.out.println(0);
            if (be instanceof ChantTunerBE tuner) {
                System.out.println(1);
                NetworkHooks.openScreen(sp, tuner, buf -> buf.writeBlockPos(pos));
            }
        }
        // クライアント側でも「成功」を返すのが GUI 表示のコツ
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}