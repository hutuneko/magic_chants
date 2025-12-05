package com.hutuneko.magic_chants.api.player.effect;

import com.hutuneko.magic_chants.Magic_chants;
import com.hutuneko.magic_chants.ModRegistry;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.player.effect.net.InstantRespawnPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ScreenWatcher {

    // ScreenWatcher.java
    private static int deferredRespawnTicks = 0; // クラスフィールドに追加

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (Minecraft.getInstance().player != null && event.getNewScreen() instanceof DeathScreen && Minecraft.getInstance().player.hasEffect(ModRegistry.INSRESPAWN.get())) {

            MagicNetwork.CHANNEL.sendToServer(new InstantRespawnPacket());

            // 処理を即座に実行する代わりに、遅延フラグを立てる
            deferredRespawnTicks = 1;
            event.setCanceled(true);
        }
    }

    // クライアントティックイベントのハンドラを追加
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && deferredRespawnTicks > 0) {
            deferredRespawnTicks--;

            if (deferredRespawnTicks == 0) {
                // 遅延後に強制リスポーン処理を実行
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    try {
                        mc.player.respawn();
                        mc.setScreen(null);
                    } catch (Throwable t) {
                        // 予期せぬエラー（LinkageErrorなど）をキャッチし、ゲームをクラッシュさせずにログに出力
                        mc.gui.getChat().addMessage(net.minecraft.network.chat.Component.literal("§c[Magic Chants] Respawn logic failed defensively. Check log."));
                        Magic_chants.LOGGER.error("Defensive respawn failed", t);
                    }
                }
            }
        }
    }
}
