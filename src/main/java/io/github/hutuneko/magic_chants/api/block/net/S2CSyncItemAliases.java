package io.github.hutuneko.magic_chants.api.block.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.block.gui.ChantTunerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record S2CSyncItemAliases(String itemUuid, String json) implements CustomPacketPayload {

    public static final Type<S2CSyncItemAliases> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "sync_item_aliases")
    );

    public static final StreamCodec<FriendlyByteBuf, S2CSyncItemAliases> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, S2CSyncItemAliases::itemUuid,
            ByteBufCodecs.STRING_UTF8, S2CSyncItemAliases::json,
            S2CSyncItemAliases::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CSyncItemAliases msg,IPayloadContext context) {
        context.enqueueWork(() -> {
            MagicChants.LOGGER.info("[S2C] recv json length={}", msg.json == null ? 0 : msg.json.length());
            var mc = Minecraft.getInstance();
            if (mc.screen instanceof ChantTunerScreen scr) {
                scr.applyAliasesFromServerJson(msg.itemUuid, msg.json);
                MagicChants.LOGGER.info("[S2C] applied to screen");
            } else {
                MagicChants.LOGGER.info("[S2C] screen not ChantTunerScreen: {}", mc.screen);
            }
        });
    }
}