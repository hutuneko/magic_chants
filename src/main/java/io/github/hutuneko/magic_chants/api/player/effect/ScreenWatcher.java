package io.github.hutuneko.magic_chants.api.player.effect;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.ModRegistry;
import io.github.hutuneko.magic_chants.api.player.effect.net.InstantRespawnPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public class ScreenWatcher {

    // ScreenWatcher.java
    private static int deferredRespawnTicks = 0; // クラスフィールドに追加

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (Minecraft.getInstance().player != null && event.getNewScreen() instanceof DeathScreen && Minecraft.getInstance().player.hasEffect(ModRegistry.INSRESPAWN)) {

            ClientPacketDistributor.sendToServer(new InstantRespawnPacket());

            // 処理を即座に実行する代わりに、遅延フラグを立てる
            deferredRespawnTicks = 1;
            event.setCanceled(true);
        }
    }

    // クライアントティックイベントのハンドラを追加
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if (deferredRespawnTicks > 0) {
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
                        mc.gui.getChat().addClientSystemMessage(Component.literal("§c[Magic Chants] Respawn logic failed defensively. Check log."));
                        MagicChants.LOGGER.error("Defensive respawn failed", t);
                    }
                }
            }
        }
    }
}
