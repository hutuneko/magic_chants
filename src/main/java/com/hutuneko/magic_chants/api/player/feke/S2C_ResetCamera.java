package com.hutuneko.magic_chants.api.player.feke;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_ResetCamera {
    public static void handle(S2C_ResetCamera msg, Supplier<NetworkEvent.Context> ctx) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            mc.setCameraEntity(player);
        }
    }
}

