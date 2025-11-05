package com.hutuneko.magic_chants.api.net;

import com.hutuneko.magic_chants.api.block.net.C2S_RequestItemAliases;
import com.hutuneko.magic_chants.api.block.net.C2S_RewriteAndSaveAliases;
import com.hutuneko.magic_chants.api.block.net.S2C_SyncItemAliases;
import com.hutuneko.magic_chants.api.chat.net.C2S_CommitMagicPacket;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.net.S2C_SyncMagicPowerPacket;
import com.hutuneko.magic_chants.api.player.feke.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class MagicNetwork {
    private MagicNetwork() {}

    public static final String MODID = "magic_chants";
    private static final String PROTOCOL = "1";

    public static SimpleChannel CHANNEL;

    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
        );

        int id = 0;

        // C2S（サーバが受信）
        CHANNEL.messageBuilder(C2S_CommitMagicPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_CommitMagicPacket::encode)
                .decoder(C2S_CommitMagicPacket::decode)
                .consumerMainThread(C2S_CommitMagicPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2S_RewriteAndSaveAliases.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_RewriteAndSaveAliases::encode)
                .decoder(C2S_RewriteAndSaveAliases::decode)
                .consumerMainThread(C2S_RewriteAndSaveAliases::handle)
                .add();

        CHANNEL.messageBuilder(C2S_CameraFire.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_CameraFire::encode)
                .decoder(C2S_CameraFire::decode)
                .consumerMainThread(C2S_CameraFire::handle)
                .add();
        CHANNEL.messageBuilder(C2S_StartRemoteView.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_StartRemoteView::encode)
                .decoder(C2S_StartRemoteView::decode)
                .consumerMainThread(C2S_StartRemoteView::handle)
                .add();
        CHANNEL.messageBuilder(C2S_UpdateProxyPos.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_UpdateProxyPos::encode)
                .decoder(C2S_UpdateProxyPos::decode)
                .consumerMainThread(C2S_UpdateProxyPos::handle)
                .add();

        CHANNEL.messageBuilder(C2S_RequestItemAliases.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_RequestItemAliases::encode)
                .decoder(C2S_RequestItemAliases::decode)
                .consumerMainThread(C2S_RequestItemAliases::handle)
                .add();
        CHANNEL.messageBuilder(S2C_ResetCamera.class,id++,NetworkDirection.LOGIN_TO_CLIENT)
                .consumerMainThread(S2C_ResetCamera::handle).add();

        // ★ S2C（クライアントが受信）→ サーバ側でも “登録” は必要（送信時のエンコードに使う）
        CHANNEL.messageBuilder(S2C_SyncMagicPowerPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_SyncMagicPowerPacket::encode)
                .decoder(S2C_SyncMagicPowerPacket::decode)
                .consumerMainThread(S2C_SyncMagicPowerPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2C_SyncItemAliases.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_SyncItemAliases::encode)
                .decoder(S2C_SyncItemAliases::decode)
                .consumerMainThread(S2C_SyncItemAliases::handle)
                .add();
        CHANNEL.messageBuilder(S2C_SetCameraEntity.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_SetCameraEntity::encode)
                .decoder(S2C_SetCameraEntity::decode)
                .consumerMainThread(S2C_SetCameraEntity::handle)
                .add();

        System.out.println("[MagicNetwork] Registered " + id + " packets.");
    }
}
