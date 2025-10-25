package com.hutuneko.magic_chants.api.net;

import com.hutuneko.magic_chants.api.block.net.C2S_ApplyAliasesFromTuner;
import com.hutuneko.magic_chants.api.chat.net.C2S_CommitMagicPacket;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.net.S2C_SyncMagicPowerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
                () -> PROTOCOL,
                PROTOCOL::equals,
                PROTOCOL::equals
        );

        int id = 0;

        // (1) MagicChat 終了時に送られる
        CHANNEL.messageBuilder(C2S_CommitMagicPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_CommitMagicPacket::encode)
                .decoder(C2S_CommitMagicPacket::decode)
                .consumerMainThread(C2S_CommitMagicPacket::handle)
                .add();

        // (2) 詠唱辞書(ChantTuner)の設定送信
        CHANNEL.messageBuilder(C2S_ApplyAliasesFromTuner.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_ApplyAliasesFromTuner::encode)
                .decoder(C2S_ApplyAliasesFromTuner::decode)
                .consumerMainThread(C2S_ApplyAliasesFromTuner::handle)
                .add();
        if (FMLEnvironment.dist.isClient()) {
            CHANNEL.messageBuilder(S2C_SyncMagicPowerPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(S2C_SyncMagicPowerPacket::encode)
                    .decoder(S2C_SyncMagicPowerPacket::decode)
                    .consumerMainThread(S2C_SyncMagicPowerPacket::handle)
                    .add();

        }


        System.out.println("[MagicNetwork] Registered " + id + " packets.");
    }
}
