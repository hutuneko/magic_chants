package com.hutuneko.magic_chants.api.chat.dictionary;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AliasApplier {
    private AliasApplier(){}

    public static String normalize(ServerPlayer player, String chantRaw){
        var cap = player.getCapability(PlayerAliasesCapability.CAP).orElse(null);
        if (cap == null) return chantRaw;
        String s = chantRaw;
        for (var rule : cap.getRules()) {
            switch (rule.type()) {
                case "literal" ->
                        s = s.replace(rule.from(), rule.to());
                case "regex" -> {
                    // ${name} 形式の置換に対応
                    var p = Pattern.compile(rule.from());
                    var m = p.matcher(s);
                    if (m.find()) {
                        String to = rule.to();
                        // ${group} 展開
                        StringBuilder sb = new StringBuilder();
                        do {
                            String replaced = to;
                            for (int i = 1; i <= m.groupCount(); i++) {
                                replaced = replaced.replace("${"+i+"}", m.group(i));
                            }
                            if (m.group("power") != null) {
                                replaced = replaced.replace("${power}", m.group("power"));
                            }
                            m.appendReplacement(sb, Matcher.quoteReplacement(replaced));
                        } while (m.find());
                        m.appendTail(sb);
                        s = sb.toString();
                    }
                }
            }
        }
        return s;
    }
    // 任意のルールリストを使って正規化するヘルパ
    public static String normalizeWithRules(String chantRaw, List<IPlayerAliases.AliasRule> rules) {
        if (rules == null || rules.isEmpty()) return chantRaw;
        String s = chantRaw;
        for (var rule : rules) {
            switch (rule.type()) {
                case "literal" -> s = s.replace(rule.from(), rule.to());
                case "regex" -> {
                    var p = Pattern.compile(rule.from());
                    var m = p.matcher(s);
                    if (m.find()) {
                        String to = rule.to();
                        StringBuilder sb = new StringBuilder();
                        do {
                            String replaced = to;
                            for (int i = 1; i <= m.groupCount(); i++) {
                                replaced = replaced.replace("${"+i+"}", m.group(i));
                            }
                            if (m.group("power") != null) {
                                replaced = replaced.replace("${power}", m.group("power"));
                            }
                            m.appendReplacement(sb, Matcher.quoteReplacement(replaced));
                        } while (m.find());
                        m.appendTail(sb);
                        s = sb.toString();
                    }
                }
            }
        }
        return s;
    }

}
