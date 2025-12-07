package com.hutuneko.magic_chants.api.util;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import com.hutuneko.magic_chants.api.magic.MagicCast;

import java.util.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;   // 既存
import org.apache.commons.lang3.tuple.Triple;

public class MagicChantsAPI {
    public static Triple<List<MagicCast.Step>, List<Boolean>, List<String>>
    mergeAndAlignC(
            List<WorldJsonStorage.MagicDef> a,
            List<WorldJsonStorage.MagicDef> b) {

        System.out.println(a + ",and," + b);

        // sa の構築 (null チェックを追加)
        List<MagicCast.Step> sa = new ArrayList<>();
        if (a != null) { // ★ 1. リスト a が null でないかチェック
            for (WorldJsonStorage.MagicDef def : a) {
                if (def != null) { // ★ 2. リストの中身 def が null でないかチェック
                    List<MagicCast.Step> steps = def.steps();
                    if (steps != null) { // ★ 3. def.steps() が null を返さないかチェック
                        sa.addAll(steps);
                    }
                }
            }
        }
        System.out.println(sa);

        // sb の構築 (null チェックを追加)
        List<MagicCast.Step> sb = new ArrayList<>();
        if (b != null) { // ★ 1. リスト b が null でないかチェック
            for (WorldJsonStorage.MagicDef def : b) {
                if (def != null) { // ★ 2. リストの中身 def が null でないかチェック
                    List<MagicCast.Step> steps = def.steps();
                    if (steps != null) { // ★ 3. def.steps() が null を返さないかチェック
                        sb.addAll(steps);
                    }
                }
            }
        }
        System.out.println(sb);

        var merged = mergeWithUnknownMarkersAndFlags(sa, sb);

        List<MagicCast.Step> outSteps = merged.getLeft();     // ← 最終順序
        List<Boolean> flags = merged.getRight();

        // sta の構築 (null チェックを追加)
        List<Map<ResourceLocation, String>> sta = new ArrayList<>();
        if (a != null) { // ★ 1. リスト a が null でないかチェック
            for (WorldJsonStorage.MagicDef def : a) {
                if (def != null) { // ★ 2. リストの中身 def が null でないかチェック
                    // def.textById() が null を返しても List への add(null) は合法
                    sta.add(def.textById());
                }
            }
        }
        System.out.println(sta);

        // stb の構築 (null チェックを追加)
        List<Map<ResourceLocation, String>> stb = new ArrayList<>();
        if (b != null) { // ★ 1. リスト b が null でないかチェック
            for (WorldJsonStorage.MagicDef def : b) {
                if (def != null) { // ★ 2. リストの中身 def が null でないかチェック
                    stb.add(def.textById());
                }
            }
        }
        System.out.println(stb);

        List<String> outC = alignTextsById(outSteps, flags, sta, stb);

        System.out.println(outSteps);
        System.out.println(flags);
        System.out.println(outC);

        return Triple.of(outSteps, flags, outC);
    }


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

    public static List<String> alignTextsById(
            List<MagicCast.Step> merged,
            List<Boolean> flags,
            List<Map<ResourceLocation, String>> textAList,
            List<Map<ResourceLocation, String>> textBList) {

        if (merged == null || flags == null) {
            return java.util.Collections.emptyList();
        }
        Map<ResourceLocation, String> flatA = new java.util.HashMap<>();
        if (textAList != null) {
            for (Map<ResourceLocation, String> m : textAList) {
                if (m == null) continue; // ★ Map 自体の null チェック
                for (Map.Entry<ResourceLocation, String> entry : m.entrySet()) {
                    if (entry.getKey() != null) {
                        flatA.putIfAbsent(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        Map<ResourceLocation, String> flatB = new java.util.HashMap<>();
        if (textBList != null) {
            for (Map<ResourceLocation, String> m : textBList) {
                if (m == null) continue; // ★ Map 自体の null チェック
                for (Map.Entry<ResourceLocation, String> entry : m.entrySet()) {
                    if (entry.getKey() != null) {
                        flatB.putIfAbsent(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        List<String> out = new ArrayList<>(merged.size());
        for (int i = 0; i < merged.size(); i++) {
            MagicCast.Step st = merged.get(i);
            if (st == null || st.id() == null) { // ★ Step と id の null チェック
                out.add(null);
                continue;
            }
            ResourceLocation id = st.id();
            boolean fromB = flags.get(i);
            String hit;
            if (fromB) {
                hit = flatB.get(id);
                if (hit == null) hit = flatA.get(id);
            } else {
                hit = flatA.get(id);
                if (hit == null) hit = flatB.get(id);
            }
            out.add(hit);
        }
        return out;
    }
    public static void pullEntityTowards(Entity target, Vec3 center, double strength) {
        if (target == null || center == null) return;

        Vec3 dir = center.subtract(target.position());
        double lenSqr = dir.lengthSqr();
        if (lenSqr < 1e-4) return; // ほぼ同位置なら動かさない

        Vec3 motion = dir.normalize().scale(strength);

        // 摩擦・AI に負けないための最低限の調整
        if (target.onGround()) {
            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.1, 0)); // 少し浮かせる
        }

        target.setDeltaMovement(target.getDeltaMovement().add(motion));
        target.hasImpulse = true; // これ重要
        target.hurtMarked = true; // サーバー→クライアント同期
    }
    public static void setOwnerTagToAllItems(ServerPlayer player) {
        Inventory inventory = player.getInventory();

        // 💡 プレイヤーインベントリの全スロット数 (36 + 4 + 1 = 41)
        final int TOTAL_SLOTS = 50;

        // 0 から 40 までループ
        for (int i = 0; i < TOTAL_SLOTS; ++i) {

            // PlayerInventory.getItem(i) は、iが 36-39 や 40 の場合でも
            // 内部で防具スロットやオフハンドスロットのアイテムを返します。
            ItemStack stack = inventory.getItem(i);

            if (!stack.isEmpty()) {
                // カスタムタグ付与ロジックを適用
                setOwnerTag(stack, player);
            }
        }

        // インベントリに変更を通知
        inventory.setChanged();
    }
    public static ItemStack setOwnerTag(ItemStack stack, Player owner) {
        // 1. アイテムの持つNBTタグを取得（なければ作成）
        CompoundTag tag = stack.getOrCreateTag();

        // 2. 独自のCompoundTagを作成し、UUIDを文字列として保存
        //    カスタムタグ名はユニークなものにしてください (例: "magic_chants")
        CompoundTag customTag = new CompoundTag();
        customTag.putUUID("magic_chants:creativeuuid", owner.getUUID());

        // 3. アイテムスタックの NBT にカスタムタグを格納
        tag.put("magic_chants:creative", customTag);

        return stack;
    }
}
