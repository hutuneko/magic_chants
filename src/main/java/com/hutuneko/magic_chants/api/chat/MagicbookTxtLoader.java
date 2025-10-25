package com.hutuneko.magic_chants.api.chat;

import com.google.gson.*;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** data/<ns>/spells/*.json を読み、詠唱→Step列（or triggers）を構築するローダ */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MagicbookTxtLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // 1) 完全一致（chant） → steps
    private static final Map<String, List<MagicCast.Step>> EXACT = new LinkedHashMap<>();

    // 2) トリガー（contains/startswith/regex）群
    private static final List<TriggerEntry> TRIGGERS = new ArrayList<>();

    public MagicbookTxtLoader() { super(GSON, "magics"); }

    @SubscribeEvent
    public static void onAddReload(AddReloadListenerEvent e) {
        e.addListener(new MagicbookTxtLoader());
    }

    /** 完全一致（まずこれを引く） */
    public static List<MagicCast.Step> matchExact(String chant) {
        if (chant == null) return List.of();
        var v = EXACT.get(chant.trim());
        return v != null ? v : List.of();
    }

    /** triggers 含めてマッチ（完全一致 → triggers の順に評価） */
    public static List<MagicCast.Step> matchSmart(String raw) {
        if (raw == null) return List.of();
        String s = raw.trim();
        var exact = EXACT.get(s);
        if (exact != null) return exact;

        for (TriggerEntry te : TRIGGERS) {
            var bag = te.match(s);
            if (bag != null) {
                return te.buildSteps(bag);
            }
        }
        return List.of();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files,
                         @NotNull ResourceManager rm, @NotNull ProfilerFiller profiler) {
        Map<String, List<MagicCast.Step>> nextExact = new LinkedHashMap<>();
        List<TriggerEntry> nextTriggers = new ArrayList<>();

        for (var e : files.entrySet()) {
            try {
                JsonElement root = e.getValue();
                if (root == null || root.isJsonNull()) continue;

                if (root.isJsonArray()) {
                    for (JsonElement el : root.getAsJsonArray())
                        parseSpellObject(nextExact, nextTriggers, el);
                } else {
                    JsonObject obj = root.getAsJsonObject();
                    if (obj.has("magics") && obj.get("magics").isJsonArray()) {
                        for (JsonElement el : obj.getAsJsonArray("magics"))
                            parseSpellObject(nextExact, nextTriggers, el);
                    } else {
                        parseSpellObject(nextExact, nextTriggers, obj);
                    }
                }
            } catch (Exception ex) {
                // ここで e.getKey() 付きでログすると良い
            }
        }

        EXACT.clear(); EXACT.putAll(nextExact);
        TRIGGERS.clear(); TRIGGERS.addAll(nextTriggers);
    }

    /* ---- 1スペル定義を取り込む ---- */
    private void parseSpellObject(Map<String, List<MagicCast.Step>> exactOut,
                                  List<TriggerEntry> triggersOut, JsonElement el) {
        if (el == null || !el.isJsonObject()) return;
        JsonObject obj = el.getAsJsonObject();

        // steps を先に組み立てておく
        List<StepDef> stepDefs = parseStepDefs(obj.getAsJsonArray("steps"));
        if (stepDefs.isEmpty()) return;

        // chant（完全一致）か triggers（柔軟）か
        if (obj.has("chant")) {
            String chant = obj.get("chant").getAsString().trim();
            if (!chant.isEmpty()) {
                exactOut.put(chant, toSteps(stepDefs, null)); // argsFrom は使わない
            }
        }

        if (obj.has("triggers") && obj.get("triggers").isJsonArray()) {
            List<OneTrigger> list = parseTriggers(obj.getAsJsonArray("triggers"));
            if (!list.isEmpty()) {
                triggersOut.add(new TriggerEntry(list, stepDefs));
            }
        }
    }

    /* ---- steps のパース ---- */
    private List<StepDef> parseStepDefs(@Nullable JsonArray arr) {
        if (arr == null) return List.of();
        List<StepDef> list = new ArrayList<>();
        for (JsonElement se : arr) {
            if (se == null || !se.isJsonObject()) continue;
            JsonObject so = se.getAsJsonObject();
            if (!so.has("id")) continue;
            ResourceLocation id = new ResourceLocation(so.get("id").getAsString());
            CompoundTag args = so.has("args") && so.get("args").isJsonObject()
                    ? jsonToNbt(so.getAsJsonObject("args")) : new CompoundTag();
            Map<String, String> argsFrom = null;
            if (so.has("argsFrom") && so.get("argsFrom").isJsonObject()) {
                argsFrom = new LinkedHashMap<>();
                for (var e : so.getAsJsonObject("argsFrom").entrySet()) {
                    argsFrom.put(e.getKey(), e.getValue().getAsString());
                }
            }
            list.add(new StepDef(id, args, argsFrom));
        }
        return list;
    }

    /* ---- triggers のパース ---- */
    private List<OneTrigger> parseTriggers(JsonArray arr) {
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

    /* ---- ユーティリティ ---- */
    private static List<MagicCast.Step> toSteps(List<StepDef> defs, @Nullable Map<String, String> captured) {
        List<MagicCast.Step> out = new ArrayList<>();
        for (StepDef d : defs) {
            CompoundTag args = d.args.copy(); // ベース
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
                        default -> args.putString(name, val);
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

    private static CompoundTag jsonToNbt(JsonObject jo) {
        CompoundTag tag = new CompoundTag();
        for (var e : jo.entrySet()) {
            String k = e.getKey();
            JsonElement v = e.getValue();
            if (v == null || v.isJsonNull()) continue;

            if (v.isJsonPrimitive()) {
                JsonPrimitive p = v.getAsJsonPrimitive();
                if (p.isBoolean()) tag.putBoolean(k, p.getAsBoolean());
                else if (p.isNumber()) {
                    String ns = p.getAsString();
                    if (ns.matches("^-?\\d+$")) tag.putInt(k, p.getAsInt());
                    else tag.putDouble(k, p.getAsDouble());
                } else tag.putString(k, p.getAsString());
            } else if (v.isJsonObject()) {
                tag.put(k, jsonToNbt(v.getAsJsonObject()));
            } else if (v.isJsonArray()) {
                ListTag arr = new ListTag();
                for (JsonElement ae : v.getAsJsonArray()) {
                    if (ae == null || ae.isJsonNull()) continue;
                    if (ae.isJsonObject()) arr.add(jsonToNbt(ae.getAsJsonObject()));
                    else if (ae.isJsonPrimitive()) {
                        JsonPrimitive ap = ae.getAsJsonPrimitive();
                        if (ap.isBoolean()) arr.add(ByteTag.valueOf((byte)(ap.getAsBoolean() ? 1 : 0)));
                        else if (ap.isNumber()) {
                            String s = ap.getAsString();
                            if (s.matches("^-?\\d+$")) arr.add(IntTag.valueOf(ap.getAsInt()));
                            else arr.add(DoubleTag.valueOf(ap.getAsDouble()));
                        } else arr.add(StringTag.valueOf(ap.getAsString()));
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

    private record StepDef(ResourceLocation id, CompoundTag args, @Nullable Map<String,String> argsFrom) {}

    public static final class TriggerEntry {
        private final List<OneTrigger> triggers;
        private final List<StepDef> steps;
        TriggerEntry(List<OneTrigger> triggers, List<StepDef> steps) {
            this.triggers = triggers; this.steps = steps;
        }
        /** マッチしたら「名前→値」のキャプチャ表（regexは name→value、contains/startswithは空Map） */
        @Nullable public Map<String,String> match(String s) {
            for (OneTrigger t : triggers) {
                var cap = t.test(s);
                if (cap != null) return cap;
            }
            return null;
        }
        public List<MagicCast.Step> buildSteps(@Nullable Map<String,String> captured) {
            return toSteps(steps, captured);
        }
    }

    private interface OneTrigger {
        /** マッチしなければ null を返す。contains/startswith は空Map、regex は name→value */
        @Nullable Map<String,String> test(String s);

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
            Map<String,Integer> nameToIndex = parseNamedGroupIndexMap(pat); // ★ Java17向け
            return new OneTrigger() {
                @Override public @Nullable Map<String,String> test(String s) {
                    Matcher m = compiled.matcher(s);
                    if (!m.find()) return null;

                    // 名前付きキャプチャを name→value で返す
                    if (!nameToIndex.isEmpty()) {
                        Map<String,String> out = new HashMap<>();
                        for (var e : nameToIndex.entrySet()) {
                            int idx = e.getValue();
                            if (idx <= m.groupCount()) {
                                String v = m.group(idx);
                                if (v != null) out.put(e.getKey(), v);
                            }
                        }
                        return out;
                    }
                    // 名前付きが無い場合は、空Map（argsFrom なしで使う想定）
                    return Collections.emptyMap();
                }
            };
        }
    }

    /* ========= 名前付きキャプチャ解析（Java17対応） ========= */
    /**
     * 正規表現ソース文字列を走査して、 "name" -> groupIndex を作る。
     * - 通常キャプチャ (...) は groupIndex++
     * - 非キャプチャ (?:...), (?=...), (?!...), (?<=...), (?<!) は番号を進めない
     * - 名前付き   (?<name>...) は groupIndex++ し、name をその番号に対応づける
     * - エスケープされた '(' は無視
     */
    private static Map<String,Integer> parseNamedGroupIndexMap(String pattern) {
        Map<String,Integer> nameToIndex = new LinkedHashMap<>();
        int len = pattern.length();
        int groupIndex = 0;
        for (int i = 0; i < len; i++) {
            char c = pattern.charAt(i);
            if (c != '(') continue;

            // 直前がエスケープなら無視
            if (i > 0 && pattern.charAt(i - 1) == '\\') continue;

            // "(?" で始まるか判定
            boolean isQuestion = (i + 1 < len && pattern.charAt(i + 1) == '?');
            if (isQuestion) {
                // lookahead/lookbehind/comment/非キャプチャなど
                if (i + 2 < len) {
                    char c2 = pattern.charAt(i + 2);

                    // 名前付き ?<name>
                    if (c2 == '<') {
                        // さらに name を読む
                        int nameStart = i + 3;
                        int nameEnd = nameStart;
                        while (nameEnd < len) {
                            char nc = pattern.charAt(nameEnd);
                            if (nc == '>') break;
                            // 許容: [A-Za-z0-9_]
                            if (!(nc == '_' ||
                                    (nc >= 'A' && nc <= 'Z') ||
                                    (nc >= 'a' && nc <= 'z') ||
                                    (nc >= '0' && nc <= '9'))) {
                                // 名前として不正 → 諦める
                                nameEnd = -1;
                                break;
                            }
                            nameEnd++;
                        }
                        if (nameEnd != -1 && nameEnd < len && pattern.charAt(nameEnd) == '>') {
                            String name = pattern.substring(nameStart, nameEnd);
                            groupIndex++; // ★ 名前付きはキャプチャとしてカウント
                            nameToIndex.put(name, groupIndex);
                            continue; // 次の '(' をスキャン
                        } else {
                            // (?< の後が不正 → 非キャプチャ扱いで番号は進めない
                            continue;
                        }
                    }

                    // 非キャプチャ/ルック系か？  (?:  (?=  (?!  (?>  (?#  (?<=  (?<!  など）
                    // 先頭2文字 "(?" の次が ':' or '=' or '!' or '>' or '#'
                    // あるいは '<' + (= or !) は非キャプチャ系（上で名前付きは処理済み）
                    if (c2 == ':' || c2 == '=' || c2 == '!' || c2 == '>' || c2 == '#'
                            || (c2 == '<' && i + 3 < len && (pattern.charAt(i + 3) == '=' || pattern.charAt(i + 3) == '!'))) {
                        // 非キャプチャ → 番号は進めない
                        continue;
                    }

                    // それ以外の "(?" は、Javaの仕様上ほぼ拡張構文だが、万一に備え番号を進めないでおく
                    continue;
                }
            }

            // ここに来たら通常キャプチャ "("
            groupIndex++;
        }
        return nameToIndex;
    }
}
