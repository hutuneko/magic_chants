package com.hutuneko.magic_chants.api.util;

import com.hutuneko.magic_chants.api.magic.MagicCast;

import java.util.*;

public class MagicChantsAPI {
        public static List<MagicCast.Step> mergeWithUnknownMarkers(List<MagicCast.Step> a, List<MagicCast.Step> b) {
            List<List<MagicCast.Step>> bGroups = new ArrayList<>();
            List<MagicCast.Step> currentGroup = new ArrayList<>();

            for (MagicCast.Step item : b) {
                if (item == null) {
                    bGroups.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                } else {
                    currentGroup.add(item);
                }
            }
            if (!currentGroup.isEmpty()) {
                bGroups.add(currentGroup);
            }

            List<MagicCast.Step> result = new ArrayList<>();
            int groupIndex = 0;

            for (MagicCast.Step itemA : a) {
                if (groupIndex < bGroups.size()) {
                    result.addAll(bGroups.get(groupIndex));
                    groupIndex++;
                }
                result.add(itemA);
            }

            // aの要素が尽きたあとに、まだbのグループが残ってたら追加
            while (groupIndex < bGroups.size()) {
                result.addAll(bGroups.get(groupIndex));
                groupIndex++;
            }

            return result;
        }
}
