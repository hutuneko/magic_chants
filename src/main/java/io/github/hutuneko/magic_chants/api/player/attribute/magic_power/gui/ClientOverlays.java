package io.github.hutuneko.magic_chants.api.player.attribute.magic_power.gui;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPower;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = MagicChants.MODID, value = Dist.CLIENT)
public class ClientOverlays {
    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.FOOD_LEVEL,
                Identifier.fromNamespaceAndPath(MagicChants.MODID, "attr_display"),
                (guiGraphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    Player player = mc.player;
                    if (player == null) return;

                    MagicPower cap = player.getData(MagicPowerProvider.MAGIC_POWER);
                    double mp = cap.getMP();
                    String text = "MP: " + (int) Math.round(mp);

                    int x = mc.getWindow().getGuiScaledWidth() / 2 + 91;
                    int y = mc.getWindow().getGuiScaledHeight() - 50;
                    int w = mc.font.width(text);
                    int drawX = x - w;
                    int drawY = y - 10;

                    guiGraphics.drawScrollingString(guiGraphics.textRenderer(), mc.font, Component.literal(text), drawX, drawX,drawY);
                }
        );
    }
}