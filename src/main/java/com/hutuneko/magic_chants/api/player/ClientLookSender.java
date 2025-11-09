package com.hutuneko.magic_chants.api.player;

import com.hutuneko.magic_chants.Magic_chants;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.player.net.C2S_SetHostLook;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Magic_chants.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientLookSender {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 霊体モード中だけ送信
        if (!mc.player.getPersistentData().getBoolean("magic_chants:spiritf")) return;

        // カメラの実角度を取得（カメラ対象が乗り移り先でもOK）
        Entity cam = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
        float yaw   = cam.getYRot();
        float pitch = cam.getXRot();

        MagicNetwork.CHANNEL.sendToServer(new C2S_SetHostLook(yaw, pitch));
    }
}
