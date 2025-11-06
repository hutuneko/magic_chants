package com.hutuneko.magic_chants.api.util;

import com.hutuneko.magic_chants.api.magic.MagicCast;
import org.antlr.v4.runtime.misc.Triple;

import java.util.*;

public class MagicChantsAPI {
    public static Triple<List<MagicCast.Step>, List<Boolean>, List<String>> mergeWithUnknownMarkersAndFlagsAndC(
            List<MagicCast.Step> a, List<MagicCast.Step> b, List<String> c) {

        List<MagicCast.Step> out = new ArrayList<>();
        List<Boolean> flags = new ArrayList<>();
        List<String> outC = new ArrayList<>();

        int ai = 0; // aの読み取り位置
        int ci = 0; // cの読み取り位置

        if (b != null) {
            for (MagicCast.Step s : b) {
                if (s == null) {
                    // null: Aの要素を後に追加
                    if (a != null && ai < a.size()) {
                        out.add(a.get(ai));
                        flags.add(false);
                        // Cは a と b の順に並んでいるので、Aの要素を見つけて対応する c を探す
                        if (ci < c.size()) {
                            // Aに対応するCを探す
                            outC.add(c.get(ci++)); // A1*
                            // もし c が [A*, B*, A*, B*, A*] なら、次がB対応なので1つ飛ばす
                            if (ci < c.size())
                                ci++; // B1*スキップ
                        }
                        ai++;
                    }
                } else {
                    // Bの要素を先に入れる
                    out.add(s);
                    flags.add(true);
                    // Bに対応するCを追加
                    if (ci < c.size()) {
                        outC.add(c.get(ci++)); // B1*
                    }
                }
            }
        }

        // 残りのAを処理
        if (a != null) {
            while (ai < a.size()) {
                out.add(a.get(ai));
                flags.add(false);
                if (ci < c.size())
                    outC.add(c.get(ci++)); // A3*
                ai++;
            }
        }

        return new Triple<>(out, flags, outC);
    }

}
