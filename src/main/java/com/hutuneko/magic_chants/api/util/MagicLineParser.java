package com.hutuneko.magic_chants.api.util;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import com.ibm.icu.impl.Pair;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public final class MagicLineParser {
    private MagicLineParser(){}
    /** 詠唱文（完全一致）をJSON辞書から引く */
    public static Pair<List<List<MagicCast.Step>>, List<WorldJsonStorage.MagicDef>> parse(ServerLevel level, UUID uuid, String chant) {
        List<List<MagicCast.Step>> list = new ArrayList<>();
        List<WorldJsonStorage.MagicDef> defList = new ArrayList<>();
        List<String> chants = Arrays.stream(
                        chant.trim()
                                .replace('\u3000', ' ')  // 全角スペース→半角に正規化
                                .split("\\s+")            // 空白の連続で分割
                )
                .filter(s -> !s.isEmpty())
                .toList();

        System.out.println(chants);
        for (String chat : chants) {
            WorldJsonStorage.MagicDef def = WorldJsonStorage.matchSmartItemWithText(level, uuid, chat);
            list.add(def.steps());
            defList.add(def);
            System.out.println(chat);
        }
        return Pair.of(list,defList) ;
    }
}

