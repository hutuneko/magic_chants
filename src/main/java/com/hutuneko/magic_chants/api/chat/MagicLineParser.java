package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.magic.MagicCast;

import java.util.List;

public final class MagicLineParser {
    private MagicLineParser(){}

    /** 詠唱文（完全一致）をJSON辞書から引く */
    public static List<MagicCast.Step> parse(String chant) {
        return MagicbookTxtLoader.matchSmart(chant);
    }
}

