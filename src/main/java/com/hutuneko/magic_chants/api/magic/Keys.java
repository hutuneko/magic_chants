package com.hutuneko.magic_chants.api.magic;

import com.hutuneko.magic_chants.api.magic.target.Target;
import com.mojang.serialization.Codec;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class Keys {
    public static final Codec<UUID> UUID_CODEC =
            Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final String NS = "magic_chants";
    public static final DataKey<Vec3> POS =
            DataKey.of(NS, "pos", Vec3.CODEC);
    public static final DataKey<Boolean> VISUAL_ONLY =
            DataKey.of(NS, "visual_only", Codec.BOOL);
    public static final DataKey<Float> POWER =
            DataKey.of(NS, "power", Codec.FLOAT);
    public static final DataKey<String> CHANT_RAW =
            DataKey.of(NS, "chant_raw",Codec.STRING);
    public static final DataKey<UUID> TARGET_UUID =
            DataKey.of(NS, "target_uuid", UUID_CODEC);
    public static final DataKey<UUID> PLAYER_UUID =
            DataKey.of(NS, "player_uuid", UUID_CODEC);
    public static final DataKey<String> STRING =
            DataKey.of(NS, "string", Codec.STRING);
    public static final DataKey<String> CHANT =
            DataKey.of(NS, "chant", Codec.STRING);
    public static final DataKey<Integer> INT =
            DataKey.of(NS, "int", Codec.INT);
    public static final DataKey<String> TARGET =
            DataKey.of(NS,"target",Codec.STRING);
}
