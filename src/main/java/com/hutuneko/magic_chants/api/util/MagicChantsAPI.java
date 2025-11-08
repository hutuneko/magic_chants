package com.hutuneko.magic_chants.api.util;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import com.hutuneko.magic_chants.api.magic.MagicCast;

import java.util.*;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;   // 既存
import org.apache.commons.lang3.tuple.Triple;

public class MagicChantsAPI {


    /** 既存：out と flags（true=B, false=A）を返す */
    public static Pair<List<MagicCast.Step>, List<Boolean>> mergeWithUnknownMarkersAndFlags(
            List<MagicCast.Step> a, List<MagicCast.Step> b) {

        List<MagicCast.Step> out = new ArrayList<>();
        List<Boolean> flags = new ArrayList<>();
        int ai = 0;

        if (b != null) {
            for (MagicCast.Step s : b) {
                if (s == null) {
                    if (a != null && ai < a.size()) {
                        out.add(a.get(ai++));
                        flags.add(false); // A
                    }
                } else {
                    out.add(s);
                    flags.add(true);      // B
                }
            }
        }
        if (a != null) {
            while (ai < a.size()) {
                out.add(a.get(ai++));
                flags.add(false);         // A
            }
        }
        return Pair.of(out, flags);
    }

    /**
     * flags（true=B, false=A）の順に、c を再配置する。
     * c は A/B の要素が混在・順不同でもOK。isFromB が A/B を判定できればよい。
     * 例：flags=[B,A,B,A,A] なら、outC=[(Bキュー先頭),(Aキュー先頭),(Bキュー2番目),(Aキュー2番目),(Aキュー3番目)]
     */
    public static Triple<List<MagicCast.Step>, List<Boolean>, List<String>>
    mergeAndAlignC(
            List<WorldJsonStorage.MagicDef> a,
            List<WorldJsonStorage.MagicDef> b) { // Step→ID
        List<MagicCast.Step> sa = new ArrayList<>();
        for (WorldJsonStorage.MagicDef def : a){
           sa.addAll(def.steps());
        }
        List<MagicCast.Step> sb = new ArrayList<>();
        for (WorldJsonStorage.MagicDef def : b){
            sb.addAll(def.steps());
        }
        var merged = mergeWithUnknownMarkersAndFlags(sa, sb);
        List<MagicCast.Step> outSteps = merged.getLeft();     // ← 最終順序
        List<Boolean> flags          = merged.getRight();
        List<Map<ResourceLocation,String>> sta = new ArrayList<>();
        for (WorldJsonStorage.MagicDef def : a){
            sta.add(def.textById());
        }
        List<Map<ResourceLocation,String>> stb = new ArrayList<>();
        for (WorldJsonStorage.MagicDef def : b){
            stb.add(def.textById());
        }
        List<String> outC = alignTextsById(outSteps,flags, sta, stb);
        return Triple.of(outSteps, flags, outC);
    }
    /**
     * flags（true=B/false=A）に従い、該当サイドの辞書群から ID を引いて並べる。
     * 見つからなければ反対サイド→最後に null（またはプレースホルダ）でフォールバック。
     */
    public static List<String> alignTextsById(
            List<MagicCast.Step> merged,
            List<Boolean> flags,
            List<Map<ResourceLocation,String>> textAList,
            List<Map<ResourceLocation,String>> textBList) {

        // 先頭優先で探したいので、リスト順をそのまま走査
        List<String> out = new ArrayList<>(merged.size());

        for (int i = 0; i < merged.size(); i++) {
            MagicCast.Step st = merged.get(i);
            ResourceLocation id = st.id();
            boolean fromB = flags.get(i);

            String hit = null;

            if (fromB) {
                // まずB側の辞書群で検索
                for (Map<ResourceLocation,String> m : textBList) {
                    hit = m.get(id);
                    if (hit != null) break;
                }
                // 無ければA側で補完
                if (hit == null) {
                    for (Map<ResourceLocation,String> m : textAList) {
                        hit = m.get(id);
                        if (hit != null) break;
                    }
                }
            } else {
                // まずA側の辞書群で検索
                for (Map<ResourceLocation,String> m : textAList) {
                    hit = m.get(id);
                    if (hit != null) break;
                }
                // 無ければB側で補完
                if (hit == null) {
                    for (Map<ResourceLocation,String> m : textBList) {
                        hit = m.get(id);
                        if (hit != null) break;
                    }
                }
            }

            out.add(hit); // 見つからなければ null（必要なら "?" 等に置換）
        }

        return out;
    }
}
