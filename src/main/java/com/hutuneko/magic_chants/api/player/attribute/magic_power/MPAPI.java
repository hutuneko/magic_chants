package com.hutuneko.magic_chants.api.player.attribute.magic_power;

import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.net.S2C_SyncMagicPowerPacket;
import net.minecraftforge.network.PacketDistributor;

public class MPAPI {
    private static int mp;
    public static boolean calculateMpCost(float scorer, MagicContext ctx){
        int mps = (int) (scorer * 2);
        var player = ctx.player();
        System.out.println(mps);
        if (player == null)return false;
        System.out.println(10);
        player.getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(pmp -> mp = (int) pmp.getMP());
        int a = mp - mps;
        if (a < 0) return false;
        System.out.println(11);
        player.getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(pmp -> {
            double current = pmp.getMP();
            pmp.setMP(current - mps); // 5MP 消費
            MagicNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new S2C_SyncMagicPowerPacket(pmp.getMP(), pmp.getMaxMP())
            );
        });

        return true;
    }
}
