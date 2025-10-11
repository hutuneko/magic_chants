package com.hutuneko.magic_chants.api.magic;

import com.mojang.serialization.Codec;
import net.minecraft.world.phys.Vec3;

public final class Keys {
    public static final String NS = "magic_chants";
    public static final DataKey<Vec3> POS =
            DataKey.of(NS, "pos", Vec3.CODEC);
    public static final DataKey<Boolean> VISUAL_ONLY =
            DataKey.of(NS, "visual_only", Codec.BOOL);
    public static final DataKey<Double> POWER =
            DataKey.of(NS, "power", Codec.DOUBLE);
    public static final DataKey<String> CHANT_RAW =
            DataKey.of(NS, "chant_raw",Codec.STRING);
}
