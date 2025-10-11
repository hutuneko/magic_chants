package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.chat.dictionary.IPlayerAliases;

import java.util.List;

public final class AliasParser {
    public static List<IPlayerAliases.AliasRule> parseLines(List<String> lines) {
        java.util.List<IPlayerAliases.AliasRule> out = new java.util.ArrayList<>();
        for (String line : lines) {
            String s = line.trim();
            if (s.isEmpty() || s.startsWith("#")) continue;
            String[] a = s.split("\\|", 4);
            if (a.length < 4) continue;
            String type = a[0];
            int prio = Integer.parseInt(a[1]);
            String from = a[2];
            String to   = a[3];
            out.add(new IPlayerAliases.AliasRule(type, from, to, prio));
        }
        return out;
    }
}

