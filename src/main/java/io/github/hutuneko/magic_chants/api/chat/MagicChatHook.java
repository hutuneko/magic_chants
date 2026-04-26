package io.github.hutuneko.magic_chants.api.chat;

import io.github.hutuneko.magic_chants.api.chat.net.C2SCommitMagicPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.UUID;

@EventBusSubscriber(value = Dist.CLIENT)
public final class MagicChatHook {
    private static boolean magicSessionActive = false;
    private static Screen prev;
    private static UUID currentItemUuid;
    private static ItemStack currentItemStack;

    public static void openMagicChatSession(UUID itemUuid, InteractionHand hand, ItemStack itemStack, Player player) {
        currentItemUuid = itemUuid;
        currentItemStack = itemStack;
        player.getPersistentData().putString("magic_chants:itemuuid", itemUuid.toString());

        var mc = Minecraft.getInstance();
        mc.execute(() -> mc.setScreen(new MagicChatScreen(itemUuid, itemStack)));
    }


    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre e) {
        var mc = Minecraft.getInstance();
        Screen now = mc.screen;

        // ★ チャットを「閉じた瞬間」を検知して Commit を送る（ESCで閉じた時など）
        if (magicSessionActive && prev instanceof MagicChatScreen && !(now instanceof MagicChatScreen)) {
            magicSessionActive = false;
            ClientPacketDistributor.sendToServer(new C2SCommitMagicPacket(currentItemUuid.toString(),currentItemStack));
        }
        prev = now;
    }
}
