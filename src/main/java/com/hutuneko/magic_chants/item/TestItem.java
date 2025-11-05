package com.hutuneko.magic_chants.item;

import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.player.feke.C2S_StartRemoteView;
import com.hutuneko.magic_chants.api.player.feke.S2C_ResetCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class TestItem extends Item {
    public TestItem(Properties properties) {
        super(properties);
    }
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (player.isShiftKeyDown()) {
                MagicNetwork.CHANNEL.sendToServer(new C2S_StartRemoteView());
            } else {
                ServerLevel originalLevel = sp.serverLevel().getLevel(); // 例：オーバーワールドへ戻す
                if (sp.level() != originalLevel) {
                    sp.changeDimension(originalLevel);
                }

                MagicNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),new S2C_ResetCamera());
            }
        }
        // ⚙️ クライアント側：チャット開く
        else if (level.isClientSide) {
            if (!player.isShiftKeyDown() && player instanceof LocalPlayer l) {
                returnCameraToPlayer(l);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    public static void returnCameraToPlayer(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        mc.setCameraEntity(player);
    }

}