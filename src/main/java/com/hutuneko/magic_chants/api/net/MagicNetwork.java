package com.hutuneko.magic_chants.api.net;

import com.hutuneko.magic_chants.api.block.net.C2S_RequestItemAliases;
import com.hutuneko.magic_chants.api.block.net.C2S_RewriteAndSaveAliases;
import com.hutuneko.magic_chants.api.block.net.S2C_SyncItemAliases;
import com.hutuneko.magic_chants.api.chat.net.C2S_CommitMagicPacket;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.net.S2C_SyncMagicPowerPacket;
import com.hutuneko.magic_chants.api.player.net.C2S_SetHostLook;
import com.hutuneko.magic_chants.api.player.net.S2C_Rot;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist; // ★ 新規インポート
import net.minecraftforge.fml.DistExecutor;    // ★ 新規インポート
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class MagicNetwork {
    private MagicNetwork() {}

    public static final String MODID = "magic_chants";
    private static final String PROTOCOL = "1";

    public static SimpleChannel CHANNEL;
    private static int nextId = 0; // id の管理をフィールドに移すか、ラムダ内で final を使う

    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
        );

        // C2S（サーバが受信）パケットの登録
        // サーバー・クライアント両方で登録が必要（クライアントは送信、サーバーは受信のため）
        // id は 0 から開始
        registerC2SPackets(nextId);

        // S2C（クライアントが受信）パケットの登録
        // クライアント側の handle メソッド（LocalPlayer を参照する可能性が高い）のロードを
        // DistExecutor を使ってクライアント実行時のみに限定する。
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> registerS2CPackets(nextId)); // ★ ここで分離

        System.out.println("[MagicNetwork] Registered " + nextId + " packets.");
    }

    // --- C2S パケット登録メソッド (共通) ---
    private static void registerC2SPackets(int startId) {
        int id = startId;

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

        CHANNEL.messageBuilder(C2S_RequestItemAliases.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_RequestItemAliases::encode)
                .decoder(C2S_RequestItemAliases::decode)
                .consumerMainThread(C2S_RequestItemAliases::handle)
                .add();
        CHANNEL.messageBuilder(C2S_SetHostLook.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2S_SetHostLook::encode)
                .decoder(C2S_SetHostLook::decode)
                .consumerMainThread(C2S_SetHostLook::handle)
                .add();

        MagicNetwork.nextId = id; // id の最終値を更新
    }

    // --- S2C パケット登録メソッド (クライアント専用) ---
    // このメソッドは DistExecutor によってクライアント環境でのみ実行される
    private static void registerS2CPackets(int startId) {
        // C2Sの後に続くIDから開始
        int id = startId;

        // S2C（クライアントが受信）
        CHANNEL.messageBuilder(S2C_SyncMagicPowerPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_SyncMagicPowerPacket::encode)
                .decoder(S2C_SyncMagicPowerPacket::decode)
                // クライアントでのみ実行可能なハンドラ
                .consumerMainThread(S2C_SyncMagicPowerPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2C_SyncItemAliases.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_SyncItemAliases::encode)
                .decoder(S2C_SyncItemAliases::decode)
                // クライアントでのみ実行可能なハンドラ
                .consumerMainThread(S2C_SyncItemAliases::handle)
                .add();

        CHANNEL.messageBuilder(S2C_Rot.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_Rot::encode)
                .decoder(S2C_Rot::decode)
                // クライアントでのみ実行可能なハンドラ
                .consumerMainThread(S2C_Rot::handle)
                .add();

        MagicNetwork.nextId = id; // id の最終値を更新
    }
}