package com.hutuneko.magic_chants.api.chat;

import com.hutuneko.magic_chants.api.player.attribute.MagicAttributes;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.stream.Collectors;

public class ChantScorer {

    public static float score(String chant, ServerPlayer player) {
        if (chant == null || chant.isBlank()) return 0f;

        // 全角スペース・句読点・記号を区切りに正規化
        String normalized = chant.replaceAll("[、。・.,;!?！？　]", " ").trim();
        String[] words = normalized.split("\\s+");
        if (words.length == 0) return 0f;

        // ① 繰り返し率（多いほどリズム感）
        float repScore = (float) repetitionScore(words);

        // ② 語長の安定度（標準偏差が低いほど高得点）
        float rhythmScore = (float) rhythmScore(words);

        // ③ 音の繋がり（語尾と次語の頭が一致する率）
        float flowScore = (float) flowScore(words);

        // ④ 文字多様性（ユニーク文字 / 全文字）
        float diversityScore = (float) diversityScore(normalized);

        // ⑤ 架空語率（漢字・カタカナ・記号など）
        float fictionScore = (float) fictionScore(normalized);
        float chantpower = (float) player.getAttributeValue(MagicAttributes.CHANT_POWER.get());
        // 総合スコア（重み平均）
        float finalScore = 10f * (
                0.25f * repScore +
                        0.25f * rhythmScore +
                        0.2f  * flowScore +
                        0.2f  * diversityScore +
                        0.1f  * fictionScore
        ) * chantpower;

        return Math.round(finalScore * 10f) / 10f; // 小数1桁
    }

    private static double repetitionScore(String[] words) {
        Map<String, Long> freq = Arrays.stream(words)
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        long max = freq.values().stream().max(Long::compare).orElse(1L);
        double ratio = (double) max / words.length;
        return Math.max(0, Math.min(1, ratio));
    }

    private static double rhythmScore(String[] words) {
        double[] lens = Arrays.stream(words).mapToDouble(String::length).toArray();
        double mean = Arrays.stream(lens).average().orElse(0);
        double variance = Arrays.stream(lens)
                .map(l -> Math.pow(l - mean, 2)).sum() / lens.length;
        double stddev = Math.sqrt(variance);
        return 1.0 / (1.0 + stddev / mean);
    }

    private static double flowScore(String[] words) {
        if (words.length <= 1) return 0;
        int match = 0;
        for (int i = 0; i < words.length - 1; i++) {
            String end = words[i].substring(words[i].length() - 1);
            String start = words[i + 1].substring(0, 1);
            if (end.equalsIgnoreCase(start)) match++;
        }
        return (double) match / (words.length - 1);
    }

    private static double diversityScore(String text) {
        Set<Character> unique = text.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        return Math.min(1.0, (double) unique.size() / Math.max(5, text.length()));
    }

    private static double fictionScore(String text) {
        long nonAlphaNum = text.chars()
                .filter(c -> !Character.isLetterOrDigit(c))
                .count();
        return Math.min(1.0, (double) nonAlphaNum / text.length());
    }
}
