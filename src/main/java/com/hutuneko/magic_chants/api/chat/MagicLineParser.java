package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.UUID;

public final class MagicLineParser {
    private MagicLineParser(){}

    /** 詠唱文（完全一致）をJSON辞書から引く */
    public static List<MagicCast.Step> parse(ServerLevel level, UUID uuid, String chant) {
        return WorldJsonStorage.matchSmartItem(level,uuid,chant);
    }
}

