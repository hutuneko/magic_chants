package com.hutuneko.magic_chants.api.block.net;

import com.hutuneko.magic_chants.api.chat.dictionary.IPlayerAliases;
import com.hutuneko.magic_chants.block.ChantTunerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import com.hutuneko.magic_chants.api.chat.dictionary.PlayerAliasesCapability;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// C2S_ApplyAliasesFromTuner.java
public class C2S_ApplyAliasesFromTuner {
    private final BlockPos pos;
    private final String body;
    private final boolean toBlock;

    public C2S_ApplyAliasesFromTuner(BlockPos pos, String body, boolean toBlock) {
        this.pos = pos;
        this.body = body;
        this.toBlock = toBlock;
    }

    // ★送信時
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(body);
        buf.writeBoolean(toBlock);
    }

    // ★受信時
    public static C2S_ApplyAliasesFromTuner decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String body = buf.readUtf();
        boolean toBlock = buf.readBoolean();
        return new C2S_ApplyAliasesFromTuner(pos, body, toBlock);
    }

    public static void handle(C2S_ApplyAliasesFromTuner msg, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;
            ServerLevel lvl = sp.serverLevel();
            BlockEntity be = lvl.getBlockEntity(msg.pos);
            if (!(be instanceof ChantTunerBE tuner)) return;

            List<IPlayerAliases.AliasRule> parsed = new ArrayList<>();
            for (String line : msg.body.split("\\R")) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] a = line.split("\\|", 4);
                if (a.length < 4) continue;
                parsed.add(new IPlayerAliases.AliasRule(a[0], a[2], a[3], Integer.parseInt(a[1])));
            }

            if (msg.toBlock) {
                tuner.setRules(parsed);
                tuner.setChanged();
                lvl.sendBlockUpdated(msg.pos, tuner.getBlockState(), tuner.getBlockState(), 3);
                System.out.println("[Alias] Saved " + parsed.size() + " rules to block " + msg.pos);
            } else {
                var cap = sp.getCapability(PlayerAliasesCapability.CAP).orElse(null);
                if (cap != null) {
                    cap.setRules(parsed);
                    System.out.println("[Alias] Applied " + parsed.size() + " rules to player " + sp.getGameProfile().getName());
                } else {
                    System.out.println("[Alias] ERROR: Capability not found on player");
                }
            }
        });
        c.setPacketHandled(true);
    }
}

