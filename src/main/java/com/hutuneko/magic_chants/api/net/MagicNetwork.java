package com.hutuneko.magic_chants.api.net;

import com.hutuneko.magic_chants.api.block.net.C2S_RequestItemAliases;
import com.hutuneko.magic_chants.api.block.net.C2S_RewriteAndSaveAliases;
import com.hutuneko.magic_chants.api.block.net.S2C_SyncItemAliases;
import com.hutuneko.magic_chants.api.chat.net.C2S_CommitMagicPacket;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.net.ClientMagicPowerHandler;
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

        // ★ 修正: C2SとS2Cの登録を分離せず、順に呼び出す
        registerC2SPackets(nextId); // サーバー・クライアント両方で実行
        registerS2CPackets(nextId); // サーバー・クライアント両方で実行

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
    private static void registerS2CPackets(int startId) {
        // C2Sの後に続くIDから開始
        int id = startId; // ★ 修正 1: startId + 1 ではなく、startId から開始

        // S2C（クライアントが受信）
        CHANNEL.messageBuilder(S2C_SyncMagicPowerPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_SyncMagicPowerPacket::encode)
                .decoder(S2C_SyncMagicPowerPacket::decode)
                // ★ 修正 2: ClientMagicPowerHandlerの実行をDistExecutorで隔離
                .consumerMainThread((msg, ctx) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientMagicPowerHandler.handle((S2C_SyncMagicPowerPacket) msg, ctx)
                ))
                .add();

        CHANNEL.messageBuilder(S2C_SyncItemAliases.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_SyncItemAliases::encode)
                .decoder(S2C_SyncItemAliases::decode)
                // ★ 同様に隔離
                .consumerMainThread((msg, ctx) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        // S2C_SyncItemAliases.handle のロジックを ClientDistExecutor に委譲
                        // ここに S2C_SyncItemAliases 専用のクライアントハンドラークラスがない場合、
                        // S2C_SyncItemAliases::handle 自体の中身がクライアント専用であることを確認し、
                        // 静的メソッド参照ではなく、以下の DistExecutor ラムダに置き換えます。
                        // 仮に S2C_SyncItemAliases.handle の中身がクライアント専用なら:
                        S2C_SyncItemAliases.handle((S2C_SyncItemAliases) msg, ctx)
                ))
                .add();

        CHANNEL.messageBuilder(S2C_Rot.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2C_Rot::encode)
                .decoder(S2C_Rot::decode)
                // ★ 同様に隔離
                .consumerMainThread((msg, ctx) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        S2C_Rot.handle((S2C_Rot) msg, ctx)
                ))
                .add();

        MagicNetwork.nextId = id; // id の最終値を更新
    }
}