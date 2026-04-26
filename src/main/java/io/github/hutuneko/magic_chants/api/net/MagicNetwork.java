package io.github.hutuneko.magic_chants.api.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.block.net.C2SRequestItemAliases;
import io.github.hutuneko.magic_chants.api.block.net.C2SRewriteAndSaveAliases;
import io.github.hutuneko.magic_chants.api.block.net.S2CSyncItemAliases;
import io.github.hutuneko.magic_chants.api.chat.net.C2SCommitMagicPacket;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.net.S2CSyncMagicPowerPacket;
import io.github.hutuneko.magic_chants.api.player.effect.net.InstantRespawnPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MagicChants.MODID)
public final class MagicNetwork {
    private MagicNetwork() {}

    private static final String PROTOCOL = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL);

        // ===== C2S（クライアント→サーバー）=====
        registrar.playToServer(
                C2SCommitMagicPacket.TYPE,
                C2SCommitMagicPacket.STREAM_CODEC,
                (_, context) -> C2SCommitMagicPacket.handle(context)
        );

        registrar.playToServer(
                C2SRewriteAndSaveAliases.TYPE,
                C2SRewriteAndSaveAliases.STREAM_CODEC,
                C2SRewriteAndSaveAliases::handle
        );

        registrar.playToServer(
                C2SRequestItemAliases.TYPE,
                C2SRequestItemAliases.STREAM_CODEC,
                C2SRequestItemAliases::handle
        );

        registrar.playToServer(
                InstantRespawnPacket.TYPE,
                InstantRespawnPacket.STREAM_CODEC,
                (_, context) -> InstantRespawnPacket.handle(context)
        );

        registrar.playToServer(
                C2SEntityGet.TYPE,
                C2SEntityGet.STREAM_CODEC,
                C2SEntityGet::handle
        );

        // ===== S2C（サーバー→クライアント）=====
        registrar.playToClient(
                S2CSyncMagicPowerPacket.TYPE,
                S2CSyncMagicPowerPacket.STREAM_CODEC,
                S2CSyncMagicPowerPacket::handle
        );

        registrar.playToClient(
                S2CSyncItemAliases.TYPE,
                S2CSyncItemAliases.STREAM_CODEC,
                S2CSyncItemAliases::handle
        );

        registrar.playToClient(
                S2CEntityGet.TYPE,
                S2CEntityGet.STREAM_CODEC,
                S2CEntityGet::handle
        );

        MagicChants.LOGGER.info("[MagicNetwork] Registered all packets.");
    }
}