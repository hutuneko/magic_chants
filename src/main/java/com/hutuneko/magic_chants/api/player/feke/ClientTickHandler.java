package com.hutuneko.magic_chants.api.player.feke;

import com.hutuneko.magic_chants.api.net.MagicNetwork;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTickHandler {
    private static Vec3 lastPos = Vec3.ZERO;
    private static float lastYaw;
    private static float lastPitch;
    private static long lastSendTime = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Vec3 nowPos = player.position();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        boolean moved = nowPos.distanceToSqr(lastPos) > 0.01;
        boolean rotated = Math.abs(yaw - lastYaw) > 1.5f || Math.abs(pitch - lastPitch) > 1.5f;
        boolean timePassed = System.currentTimeMillis() - lastSendTime > 200;

        if (moved || rotated || timePassed) {
            MagicNetwork.CHANNEL.sendToServer(new C2S_UpdateProxyPos(nowPos.x, nowPos.y, nowPos.z, yaw, pitch));
            lastPos = nowPos;
            lastYaw = yaw;
            lastPitch = pitch;
            lastSendTime = System.currentTimeMillis();
        }
    }
}
