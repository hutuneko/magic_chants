package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.chat.net.C2S_CommitMagicPacket;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public final class MagicChatHook {
    private static boolean magicSessionActive = false;
    private static Screen prev;
    private static UUID currentItemUuid;
    private static InteractionHand currentHand;
    private static ItemStack currentItemStack;

    public static void openMagicChatSession(UUID itemUuid, InteractionHand hand, ItemStack itemStack, Player player) {
        currentItemUuid = itemUuid;
        currentHand = hand;
        currentItemStack = itemStack;
        player.getPersistentData().putUUID("magic_chants:itemuuid", itemUuid);

        var mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(() -> mc.setScreen(new MagicChatScreen(itemUuid, hand, itemStack)));
        }
    }


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc == null) return;
        Screen now = mc.screen;

        // ★ チャットを「閉じた瞬間」を検知して Commit を送る（ESCで閉じた時など）
        if (magicSessionActive && prev instanceof MagicChatScreen && !(now instanceof MagicChatScreen)) {
            magicSessionActive = false;
            if (MagicNetwork.CHANNEL != null) {
                MagicNetwork.CHANNEL.sendToServer(new C2S_CommitMagicPacket(currentItemUuid,currentHand,currentItemStack));
            }
        }
        prev = now;
    }
}
