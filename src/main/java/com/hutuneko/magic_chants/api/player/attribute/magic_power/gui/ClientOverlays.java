package com.hutuneko.magic_chants.api.player.attribute.magic_power.gui;

import com.hutuneko.magic_chants.Magic_chants;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// ClientOverlays.java
@Mod.EventBusSubscriber(modid = Magic_chants.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientOverlays {
    private static double mp;
    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent e) {
        e.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "attr_display", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;

            player.getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(cap -> mp = cap.getMP());
            // 表示内容を整形（整数/小数は好みで）
            String text = "MP: " + (int) Math.round(mp);

            // フードバーの位置に合わせて座標算出（右下寄り）
            int x = screenWidth / 2 + 91;   // フードバー基準
            int y = screenHeight - 50;      // フードバーの少し上

            // 文字幅を考慮して右寄せしたい場合
            int w = mc.font.width(text);
            int drawX = x - w;   // 右端合わせ
            int drawY = y - 10;  // ちょっと上に

            // 描画（影付き）
            graphics.drawString(mc.font, text, drawX, drawY, 0xFFAA66, true);
        });
    }
}
