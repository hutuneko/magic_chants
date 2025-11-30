package com.hutuneko.magic_chants.api.player.effect;

import com.hutuneko.magic_chants.ModRegistry;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.player.effect.net.InstantRespawnPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ScreenWatcher {

    private static Screen lastScreen = null;

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        Screen newScreen = event.getNewScreen();

        // 死亡画面（DeathScreen）になった瞬間
        if (Minecraft.getInstance().player != null && newScreen instanceof DeathScreen && Minecraft.getInstance().player.hasEffect(ModRegistry.INFRESPAWN.get())) {

            MagicNetwork.CHANNEL.sendToServer(new InstantRespawnPacket());

            // 修正箇所：画面の変更を次回のティックに予約する
            // これにより、パケット送信と画面操作のタイミングの問題を回避します。
            Minecraft.getInstance().execute(() -> {
                // 画面を消す（Respawn ボタンを押さない）
                Minecraft.getInstance().player.respawn();
                Minecraft.getInstance().setScreen(null);
            });

            event.setCanceled(true); // 念のため、このイベントで画面を開くのをキャンセルする

        }
//&& !(lastScreen instanceof DeathScreen)
        lastScreen = newScreen;
    }
}
