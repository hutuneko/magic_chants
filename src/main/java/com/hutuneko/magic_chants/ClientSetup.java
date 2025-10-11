package com.hutuneko.magic_chants;

import com.hutuneko.magic_chants.api.block.gui.ChantTunerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Magic_chants.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // メニューとスクリーンを紐付け
        MenuScreens.register(ModRegistry.CHANT_TUNER_MENU.get(), ChantTunerScreen::new);
        System.out.println("[ClientSetup] ChantTunerScreen registered");
    }
}
