// AliasRewriter.java
package com.hutuneko.magic_chants.api.file;

import com.google.gson.*;
import java.util.*;

public final class AliasRewriter {
    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

    private AliasRewriter(){}

    /** ルール文字列（複数行 "A=B"）を元に、JSON中の chant を置換して返す */
    public static String rewriteChants(String json, String rulesText) {
        // 1) ルールを解析
        Map<String, Optional<String>> rules = parseRules(rulesText);

        // 2) JSONを読み込む（トップが配列/オブジェクトどちらでもOK）
        JsonElement root = JsonParser.parseString(json);

        if (root.isJsonArray()) {
            rewriteArray(root.getAsJsonArray(), rules);
        } else if (root.isJsonObject()) {
            JsonObject obj = root.getAsJsonObject();
            if (obj.has("magics") && obj.get("magics").isJsonArray()) {
                rewriteArray(obj.getAsJsonArray("magics"), rules);
            } else {
                // 単一オブジェクトとして chant を持っている場合
                rewriteOne(obj, rules);
            }
        }
        return GSON_PRETTY.toJson(root);
    }

    private static Map<String, Optional<String>> parseRules(String text) {
        Map<String, Optional<String>> map = new LinkedHashMap<>();
        for (String raw : text.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;
            int eq = line.indexOf('=');
            if (eq < 0) continue;
            String left = line.substring(0, eq).trim();
            String right = line.substring(eq + 1).trim(); // 空も許す
            if (!left.isEmpty()) {
                map.put(left, right.isEmpty() ? Optional.empty() : Optional.of(right));
            }
        }
        return map;
    }

    private static void rewriteArray(JsonArray arr, Map<String, Optional<String>> rules) {
        for (JsonElement e : arr) {
            if (e != null && e.isJsonObject()) {
                rewriteOne(e.getAsJsonObject(), rules);
            }
        }
    }

    private static void rewriteOne(JsonObject spell, Map<String, Optional<String>> rules) {
        if (!spell.has("chant") || !spell.get("chant").isJsonPrimitive()) return;
        JsonPrimitive prim = spell.getAsJsonPrimitive("chant");
        if (!prim.isString()) return;

        String chant = prim.getAsString();
        Optional<String> to = rules.get(chant);
        if (to != null && to.isPresent()) {
            spell.addProperty("chant", to.get()); // 右側が非空ときだけ置換
        }
        // 右側が空 (= Optional.empty) のときは何もしない（保持）
    }
    public static String toAliasLinesFromMagics(Object loaded) {
        StringBuilder sb = new StringBuilder();
        // 重複を避けて順序維持
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();

        if (loaded instanceof java.util.Map<?, ?> root) {
            Object magics = root.get("magics");
            if (magics instanceof java.util.List<?> list) {
                for (Object e : list) {
                    if (!(e instanceof java.util.Map<?, ?> one)) continue;

                    Object chant = one.get("chant");
                    // 1) chant が文字列ならそのまま採用
                    if (chant instanceof String s && !s.isEmpty()) {
                        if (seen.add(s)) sb.append(s).append("=\n");
                        continue;
                    }
                    // 2) chant が配列(=regex指定)や triggers のみ → 今回はスキップ
                    //    必要ならここでパターン文字列を "=?" などで出力する処理を足す
                }
            }
        }
        return sb.toString();
    }

}
