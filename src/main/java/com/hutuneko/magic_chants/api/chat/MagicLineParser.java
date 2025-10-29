package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public final class MagicLineParser {
    private MagicLineParser(){}
    private static final List<List<MagicCast.Step>> list = new ArrayList<>();
    /** 詠唱文（完全一致）をJSON辞書から引く */
    public static List<List<MagicCast.Step>> parse(ServerLevel level, UUID uuid, String chant) {
        List<String> chants = Arrays.stream(chant.trim().split("\\s+　")).toList();
        for (String chat : chants) {
            list.add(WorldJsonStorage.matchSmartItem(level, uuid, chat));
        }
        return list;
    }
}

