package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import net.minecraft.server.level.ServerLevel;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class MagicLineParser {
    private MagicLineParser(){}
    private static List<List<MagicCast.Step>> list;
    /** 詠唱文（完全一致）をJSON辞書から引く */
    public static List<List<MagicCast.Step>> parse(ServerLevel level, UUID uuid, String chant) {
        List<String> chants = Arrays.stream(chant.trim().split("\\s+　")).toList();
        for (String chat : chants) {
            list.add(WorldJsonStorage.matchSmartItem(level, uuid, chat));
        }
        return list;
    }
}

