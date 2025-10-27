package com.hutuneko.magic_chants.api.file;

import com.google.gson.*;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ワールドセーブフォルダ内に JSON ファイルを保存／読み込みするユーティリティ。
 * 保存先例:  world/data/magic_chants/<category>/<uuid>.json
 */
public class WorldJsonStorage {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final String BASE_DIR = "data/magic_chants";

    // =====================================================
    // 保存
    // =====================================================
    /**
     * 任意のオブジェクトをワールドデータにJSONとして保存する
     *
     * @param level        サーバーレベル
     * @param relativePath "aliases/xxxx.json" のようなパス
     * @param data         保存したいオブジェクト
     */
    public static void save(ServerLevel level, String relativePath, Object data) {
        try {
            File file = getWorldFile(level, relativePath);
            file.getParentFile().mkdirs(); // ディレクトリ自動生成
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            System.out.println("[WorldJsonStorage] Saved JSON → " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[WorldJsonStorage] Failed to save JSON: " + relativePath);
            e.printStackTrace();
        }
    }

    // =====================================================
    // 読み込み
    // =====================================================
    /**
     * ワールドデータからJSONを読み込む
     *
     * @param level        サーバーレベル
     * @param relativePath "aliases/xxxx.json" のようなパス
     * @param clazz        変換先の型（Map.class や CustomClass.class）
     */
    public static <T> T load(ServerLevel level, String relativePath, Class<T> clazz) {
        File file = getWorldFile(level, relativePath);
        if (!file.exists()) {
            System.out.println("[WorldJsonStorage] No file found for " + relativePath);
            return null;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            T obj = GSON.fromJson(reader, clazz);
            System.out.println("[WorldJsonStorage] Loaded JSON ← " + file.getAbsolutePath());
            return obj;
        } catch (Exception e) {
            System.err.println("[WorldJsonStorage] Failed to load JSON: " + relativePath);
            e.printStackTrace();
        }
        return null;
    }

    // =====================================================
    // 内部ユーティリティ
    // =====================================================
    private static File getWorldFile(ServerLevel level, String relativePath) {
        MinecraftServer server = level.getServer();
        Path worldDir = server.getWorldPath(LevelResource.ROOT);
        return worldDir.resolve(BASE_DIR).resolve(relativePath).toFile();
    }
    public static void savePlayerAliases(ServerLevel level, UUID uuid, Map<String, String> data) {
        WorldJsonStorage.save(level, "magics/" + uuid + ".json", data);
    }

    public static Map<String, String> loadPlayerAliases(ServerLevel level, UUID uuid) {
        return WorldJsonStorage.load(level, "magics/" + uuid + ".json", Map.class);
    }

    // ====== ここから追記：UUIDファイル用のスマートマッチ ======

    /** アイテムUUIDごとのキャッシュ（完全一致とトリガー） */
    private static final Map<UUID, Map<String, List<MagicCast.Step>>> ITEM_EXACT = new HashMap<>();
    private static final Map<UUID, List<TriggerEntry>> ITEM_TRIGGERS = new HashMap<>();

    /**
     * UUID の定義を再読み込みしてキャッシュへ構築します。
     * 想定する配置：
     *   1) 単一ファイル … world/data/magic_chants/items/<uuid>.json
     *   2) ディレクトリ … world/data/magic_chants/items/<uuid>/*.json
     * どちらも存在すれば、両方マージします（先に単一ファイル → 次にディレクトリ）。
     */
    public static void reloadItemMagics(ServerLevel level, UUID uuid) {
        Path base = level.getServer().getWorldPath(LevelResource.ROOT)
                .resolve(BASE_DIR).resolve("magics");

        Map<String, List<MagicCast.Step>> nextExact = new LinkedHashMap<>();
        List<TriggerEntry> nextTriggers = new ArrayList<>();

        // 1) 単一ファイル
        Path single = base.resolve(uuid.toString() + ".json");
        if (Files.exists(single) && Files.isRegularFile(single)) {
            try (Reader r = Files.newBufferedReader(single, StandardCharsets.UTF_8)) {
                JsonElement rootEl = GSON.fromJson(r, JsonElement.class);
                if (rootEl != null && !rootEl.isJsonNull()) {
                    if (rootEl.isJsonArray()) {
                        for (JsonElement el : rootEl.getAsJsonArray())
                            parseSpellObject(nextExact, nextTriggers, el);
                    } else {
                        JsonObject obj = rootEl.getAsJsonObject();
                        if (obj.has("magics") && obj.get("magics").isJsonArray()) {
                            for (JsonElement el : obj.getAsJsonArray("magics"))
                                parseSpellObject(nextExact, nextTriggers, el);
                        } else {
                            parseSpellObject(nextExact, nextTriggers, obj);
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("[WorldJsonStorage] Failed to parse item file: " + single + " : " + ex);
            }
        }

        // 2) ディレクトリ
        Path dir = base.resolve(uuid.toString());
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (var stream = Files.walk(dir)) {
                stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                    try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                        JsonElement rootEl = GSON.fromJson(r, JsonElement.class);
                        if (rootEl == null || rootEl.isJsonNull()) return;

                        if (rootEl.isJsonArray()) {
                            for (JsonElement el : rootEl.getAsJsonArray())
                                parseSpellObject(nextExact, nextTriggers, el);
                        } else {
                            JsonObject obj = rootEl.getAsJsonObject();
                            if (obj.has("magics") && obj.get("magics").isJsonArray()) {
                                for (JsonElement el : obj.getAsJsonArray("magics"))
                                    parseSpellObject(nextExact, nextTriggers, el);
                            } else {
                                parseSpellObject(nextExact, nextTriggers, obj);
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("[WorldJsonStorage] Failed to parse: " + p + " : " + ex);
                    }
                });
            } catch (IOException io) {
                System.err.println("[WorldJsonStorage] Failed to walk: " + dir + " : " + io);
            }
        }

        ITEM_EXACT.put(uuid, nextExact);
        ITEM_TRIGGERS.put(uuid, nextTriggers);
        System.out.println("[WorldJsonStorage] Reloaded item magics for " + uuid +
                " : exact=" + nextExact.size() + ", triggers=" + nextTriggers.size());
    }

    /** 完全一致（UUID版） */
    public static List<MagicCast.Step> matchExactItem(ServerLevel level, UUID uuid, String chant) {
        if (chant == null) return List.of();
        var exact = ITEM_EXACT.getOrDefault(uuid, Map.of());
        var v = exact.get(chant.trim());
        return v != null ? v : List.of();
    }

    public static List<MagicCast.Step> matchSmartItem(ServerLevel level, UUID uuid, String raw) {
        if (raw == null) return List.of();
        String s = raw.trim();
        reloadItemMagics(level, uuid);


        var exact = ITEM_EXACT.getOrDefault(uuid, Map.of());
        var v = exact.get(s);
        if (v != null) return v;

        var triggers = ITEM_TRIGGERS.getOrDefault(uuid, List.of());
        for (TriggerEntry te : triggers) {
            var bag = te.match(s);
            if (bag != null) {
                return te.buildSteps(bag);
            }
        }
        return List.of();
    }
    private record StepDef(ResourceLocation id, CompoundTag args, @Nullable Map<String, String> argsFrom) {}

    /** トリガー1件の束（OR 条件）＋発動ステップ */
    private static final class TriggerEntry {
        private final List<OneTrigger> triggers;
        private final List<StepDef> steps;

        TriggerEntry(List<OneTrigger> triggers, List<StepDef> steps) {
            this.triggers = triggers;
            this.steps = steps;
        }

        /** マッチしたら名前付きグループ値の袋を返す／しなければ null */
        @Nullable Map<String, String> match(String s) {
            for (OneTrigger t : triggers) {
                Map<String, String> cap = t.test(s);
                if (cap != null) return cap; // 先勝ち
            }
            return null;
        }

        /** argsFrom を反映して最終 Step を生成 */
        List<MagicCast.Step> buildSteps(@Nullable Map<String, String> captured) {
            return toSteps(steps, captured);
        }
    }

    /** 単一トリガー（contains / startswith / regex） */
    private interface OneTrigger {
        /** マッチしたら（必要に応じて）捕捉値。非マッチは null */
        @Nullable Map<String, String> test(String s);

        static OneTrigger contains(String pat) {
            String needle = pat.toLowerCase(Locale.ROOT);
            return s -> s.toLowerCase(Locale.ROOT).contains(needle) ? Collections.emptyMap() : null;
        }

        static OneTrigger startswith(String pat) {
            String head = pat.toLowerCase(Locale.ROOT);
            return s -> s.toLowerCase(Locale.ROOT).startsWith(head) ? Collections.emptyMap() : null;
        }

        static OneTrigger regex(String pat) {
            Pattern compiled = Pattern.compile(pat, Pattern.CASE_INSENSITIVE);
            Map<String, Integer> nameToIndex = parseNamedGroupIndexMap(pat);
            return s -> {
                Matcher m = compiled.matcher(s);
                if (!m.find()) return null;
                if (nameToIndex.isEmpty()) return Collections.emptyMap();

                Map<String, String> out = new LinkedHashMap<>();
                for (var e : nameToIndex.entrySet()) {
                    int idx = e.getValue();
                    if (idx <= m.groupCount()) {
                        String v = m.group(idx);
                        if (v != null) out.put(e.getKey(), v);
                    }
                }
                return out;
            };
        }
    }

    /** JSON 1エントリ（spell定義）をパースして exact/triggers へ格納 */
    private static void parseSpellObject(
            Map<String, List<MagicCast.Step>> exactOut,
            List<TriggerEntry> triggersOut,
            JsonElement el
    ) {
        if (el == null || !el.isJsonObject()) return;
        JsonObject obj = el.getAsJsonObject();

        // 1) steps を先に作る（空ならスキップ）
        List<StepDef> stepDefs = parseStepDefs(obj.getAsJsonArray("steps"));
        if (stepDefs.isEmpty()) return;

        // 2) 完全一致 "chant"
        if (obj.has("chant")) {
            JsonElement c = obj.get("chant");
            if (c.isJsonPrimitive()) {
                String chant = c.getAsString().trim();
                if (!chant.isEmpty()) {
                    exactOut.put(chant, toSteps(stepDefs, null));
                }
            } else if (c.isJsonArray()) {
                for (JsonElement ce : c.getAsJsonArray()) {
                    if (ce.isJsonPrimitive()) {
                        String chant = ce.getAsString().trim();
                        if (!chant.isEmpty()) {
                            exactOut.put(chant, toSteps(stepDefs, null));
                        }
                    }
                }
            }
        }

        // 3) triggers（ORの配列）— contains / startswith / regex をサポート
        if (obj.has("triggers") && obj.get("triggers").isJsonArray()) {
            List<OneTrigger> list = parseTriggers(obj.getAsJsonArray("triggers"));
            if (!list.isEmpty()) {
                triggersOut.add(new TriggerEntry(list, stepDefs));
            }
        }
    }

    /** steps 配列を StepDef 化 */
    private static List<StepDef> parseStepDefs(@Nullable JsonArray arr) {
        if (arr == null) return List.of();
        List<StepDef> list = new ArrayList<>();
        for (JsonElement se : arr) {
            if (se == null || !se.isJsonObject()) continue;
            JsonObject so = se.getAsJsonObject();
            if (!so.has("id")) continue;

            ResourceLocation id = new ResourceLocation(so.get("id").getAsString());
            CompoundTag args = (so.has("args") && so.get("args").isJsonObject())
                    ? jsonToNbt(so.getAsJsonObject("args"))
                    : new CompoundTag();

            Map<String, String> argsFrom = null;
            if (so.has("argsFrom") && so.get("argsFrom").isJsonObject()) {
                argsFrom = new LinkedHashMap<>();
                for (var e : so.getAsJsonObject("argsFrom").entrySet()) {
                    argsFrom.put(e.getKey(), e.getValue().getAsString()); // int/float/double/bool/string
                }
            }
            list.add(new StepDef(id, args, argsFrom));
        }
        return list;
    }

    /** triggers 配列を OneTrigger のリストへ */
    private static List<OneTrigger> parseTriggers(JsonArray arr) {
        List<OneTrigger> out = new ArrayList<>();
        for (JsonElement te : arr) {
            if (te == null || !te.isJsonObject()) continue;
            JsonObject to = te.getAsJsonObject();
            String type = to.has("type") ? to.get("type").getAsString() : "";
            String pattern = to.has("pattern") ? to.get("pattern").getAsString() : "";
            if (type.isEmpty() || pattern.isEmpty()) continue;

            switch (type) {
                case "contains" -> out.add(OneTrigger.contains(pattern));
                case "startswith" -> out.add(OneTrigger.startswith(pattern));
                case "regex" -> out.add(OneTrigger.regex(pattern));
            }
        }
        return out;
    }

    /** StepDef + captured を最終 Step（MagicCast.Step）へ */
    private static List<MagicCast.Step>
    toSteps(List<StepDef> defs, @Nullable Map<String, String> captured) {
        List<MagicCast.Step> out = new ArrayList<>();
        for (StepDef d : defs) {
            CompoundTag args = d.args.copy();
            if (d.argsFrom != null && captured != null) {
                for (var e : d.argsFrom.entrySet()) {
                    String name = e.getKey();
                    String kind = e.getValue();
                    String val = captured.get(name);
                    if (val == null) continue;
                    switch (kind) {
                        case "int" -> args.putInt(name, parseInt(val));
                        case "float" -> args.putFloat(name, parseFloat(val));
                        case "double" -> args.putDouble(name, parseDouble(val));
                        case "bool" -> args.putBoolean(name, Boolean.parseBoolean(val));
                        default -> args.putString(name, val); // string
                    }
                }
            }
            out.add(new MagicCast.Step(d.id, args));
        }
        return out;
    }

    private static int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
    private static float parseFloat(String s) { try { return Float.parseFloat(s); } catch (Exception e) { return 0f; } }
    private static double parseDouble(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0d; } }

    /** JSON -> NBT（数値は int 優先／それ以外は double、配列・入れ子対応） */
    private static CompoundTag jsonToNbt(JsonObject jo) {
        CompoundTag tag = new CompoundTag();
        for (var e : jo.entrySet()) {
            String k = e.getKey();
            JsonElement v = e.getValue();
            if (v == null || v.isJsonNull()) continue;

            if (v.isJsonPrimitive()) {
                var p = v.getAsJsonPrimitive();
                if (p.isBoolean()) tag.putBoolean(k, p.getAsBoolean());
                else if (p.isNumber()) {
                    String ns = p.getAsString();
                    if (ns.matches("^-?\\d+$")) tag.putInt(k, p.getAsInt());
                    else tag.putDouble(k, p.getAsDouble());
                } else {
                    tag.putString(k, p.getAsString());
                }
            } else if (v.isJsonObject()) {
                tag.put(k, jsonToNbt(v.getAsJsonObject()));
            } else if (v.isJsonArray()) {
                ListTag arr = new ListTag();
                for (JsonElement ae : v.getAsJsonArray()) {
                    if (ae == null || ae.isJsonNull()) continue;
                    if (ae.isJsonObject()) arr.add(jsonToNbt(ae.getAsJsonObject()));
                    else if (ae.isJsonPrimitive()) {
                        var ap = ae.getAsJsonPrimitive();
                        if (ap.isBoolean()) arr.add(ByteTag.valueOf((byte) (ap.getAsBoolean() ? 1 : 0)));
                        else if (ap.isNumber()) {
                            String s = ap.getAsString();
                            if (s.matches("^-?\\d+$")) arr.add(IntTag.valueOf(ap.getAsInt()));
                            else arr.add(DoubleTag.valueOf(ap.getAsDouble()));
                        } else {
                            arr.add(StringTag.valueOf(ap.getAsString()));
                        }
                    } else {
                        arr.add(StringTag.valueOf(ae.toString()));
                    }
                }
                tag.put(k, arr);
            } else {
                tag.putString(k, v.toString());
            }
        }
        return tag;
    }

    /** Java17向け：(?<name>...) の “名前→groupIndex” を素朴に算出 */
    private static Map<String, Integer> parseNamedGroupIndexMap(String pattern) {
        Map<String, Integer> nameToIndex = new LinkedHashMap<>();
        int len = pattern.length();
        int groupIndex = 0;
        for (int i = 0; i < len; i++) {
            char c = pattern.charAt(i);
            if (c != '(') continue;
            if (i > 0 && pattern.charAt(i - 1) == '\\') continue;

            boolean isQ = (i + 1 < len && pattern.charAt(i + 1) == '?');
            if (isQ) {
                if (i + 2 < len && pattern.charAt(i + 2) == '<') {
                    int nameStart = i + 3, nameEnd = nameStart;
                    while (nameEnd < len) {
                        char nc = pattern.charAt(nameEnd);
                        if (nc == '>') break;
                        if (!(nc == '_' || Character.isLetterOrDigit(nc))) { nameEnd = -1; break; }
                        nameEnd++;
                    }
                    if (nameEnd != -1 && nameEnd < len && pattern.charAt(nameEnd) == '>') {
                        groupIndex++; // このグループはカウント対象
                        String name = pattern.substring(nameStart, nameEnd);
                        nameToIndex.put(name, groupIndex);
                        continue;
                    }
                }
                // ?: や ?= など非捕捉・先読みは groupIndex を増やさない
                continue;
            }
            // 通常の捕捉
            groupIndex++;
        }
        return nameToIndex;
    }
    // WorldJsonStorage に追加
    public static @Nullable JsonElement loadDataJson(MinecraftServer server, String namespace, String path) {
        ResourceLocation loc = new ResourceLocation(namespace, path); // e.g. magic_chants:magics/magic.json
        try {
            var opt = server.getResourceManager().getResource(loc); // Optional<Resource>
            if (opt.isEmpty()) return null;
            try (var in = opt.get().open();
                 var rd = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(rd);
            }
        } catch (Exception e) {
            System.err.println("[WorldJsonStorage] loadDataJson failed: " + loc + " : " + e);
            return null;
        }
    }

    // WorldJsonStorage に追加
    public static void saveJson(ServerLevel level, String relative, JsonElement json) {
        Path file = getWorldFile(level, relative).toPath();
        try {
            Files.createDirectories(file.getParent());
            try (var w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, w);
            }
        } catch (IOException e) {
            System.err.println("[WorldJsonStorage] saveJson failed: " + file + " : " + e);
        }
    }

}
