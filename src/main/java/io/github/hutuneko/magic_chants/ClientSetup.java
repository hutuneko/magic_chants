package io.github.hutuneko.magic_chants;

import io.github.hutuneko.magic_chants.api.block.gui.ChantTunerScreen;
import io.github.hutuneko.magic_chants.entity.InvisibleLandMineRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;


@EventBusSubscriber
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // メニューとスクリーンを紐付け
//        MenuScreens.create(ModRegistry.CHANT_TUNER_MENU.get(), ChantTunerScreen::new);
    }
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModRegistry.LAND_MINE.get(),
                InvisibleLandMineRenderer::new
        );
    }
}
