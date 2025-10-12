package com.hutuneko.magic_chants.api.player.attribute.magic_power;

import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.player.attribute.MagicAttributes;

import java.util.Objects;

public class MPAPI {
    public static boolean calculateMpCost(float scorer, MagicContext ctx){
        int mp = (int) (scorer * 2);
        var player = ctx.player();
        if (player == null)return false;
        int a = (int) (player.getAttributeValue(MagicAttributes.MAGIC_POWER.get()) - mp);
        if (a < 0) return false;
        Objects.requireNonNull(player.getAttribute(MagicAttributes.MAGIC_POWER.get()))
                .setBaseValue(a);
        return true;
    }
}
