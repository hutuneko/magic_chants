package io.github.hutuneko.magic_chants.api.player.attribute.magic_power;

import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.net.S2CSyncMagicPowerPacket;
import net.minecraft.server.level.ServerPlayer;

public class MPAPI {
    public static boolean calculateMpCost(float scorer, MagicContext ctx) {
        int mps = (int) (scorer * 2);
        var player = ctx.player();
        if (player == null) return false;

        MagicPower pmp = player.getData(MagicPowerProvider.MAGIC_POWER);
        double mp = pmp.getMP();
        int a = (int) mp - mps;
        if (a < 0) return false;

        pmp.setMP(mp - mps);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new S2CSyncMagicPowerPacket(pmp.getMP(), pmp.getMaxMP()));
        }

        return true;
    }
}