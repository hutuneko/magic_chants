package com.hutuneko.magic_chants.api.util;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public final class MagicLineParser {
    private MagicLineParser(){}
    /** 詠唱文（完全一致）をJSON辞書から引く */
    public static List<List<MagicCast.Step>> parse(ServerLevel level, UUID uuid, String chant) {
        List<List<MagicCast.Step>> list = new ArrayList<>();
        List<String> chants = Arrays.stream(
                        chant.trim()
                                .replace('\u3000', ' ')  // 全角スペース→半角に正規化
                                .split("\\s+")            // 空白の連続で分割
                )
                .filter(s -> !s.isEmpty())
                .toList();

        System.out.println(chants);
        for (String chat : chants) {
            list.add(WorldJsonStorage.matchSmartItem(level, uuid, chat));
            System.out.println(chat);
        }
        return list;
    }
}

