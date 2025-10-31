package com.hutuneko.magic_chants.api.util;

import com.hutuneko.magic_chants.api.magic.MagicCast;
import com.ibm.icu.impl.Pair;

import java.util.*;

public class MagicChantsAPI {
    public static Pair<List<MagicCast.Step>, List<Boolean>> mergeWithUnknownMarkersAndFlags(
            List<MagicCast.Step> a, List<MagicCast.Step> b) {

        List<MagicCast.Step> out = new ArrayList<>();
        List<Boolean> flags = new ArrayList<>();

        int ai = 0;

        if (b != null) {
            for (MagicCast.Step s : b) {
                if (s == null) {
                    // null → A の要素を「後に」追加（false）
                    if (a != null && ai < a.size()) {
                        out.add(a.get(ai++));
                        flags.add(false);
                    }
                } else {
                    // まず B の要素を入れる（true）
                    out.add(s);
                    flags.add(true);
                }
            }
        }

        // Bが尽きたあとにAの残りを追加（false）
        if (a != null) {
            while (ai < a.size()) {
                out.add(a.get(ai++));
                flags.add(false);
            }
        }

        return Pair.of(out, flags);
    }
}
