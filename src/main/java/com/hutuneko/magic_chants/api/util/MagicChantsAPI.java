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
            List<WorldJsonStorage.MagicDef> mainList, // A (メイン)
            List<WorldJsonStorage.MagicDef> subList   // B (サブ、null区切り)
    ) {
        // 結果を格納するリスト
        List<MagicCast.Step> outSteps = new ArrayList<>();
        List<Boolean> outFlags = new ArrayList<>(); // false=Main, true=Sub
        List<String> outTexts = new ArrayList<>();

        // null ガード
        if (mainList == null) mainList = Collections.emptyList();
        if (subList == null) subList = Collections.emptyList();

        int subIndex = 0; // サブ側の読み取り位置カーソル

        // メインの魔法を1つずつ処理する
        for (WorldJsonStorage.MagicDef mainDef : mainList) {

            // -------------------------------------------------
            // 1. 先にサブ効果 (B) をすべて回収して追加する
            // -------------------------------------------------
            while (subIndex < subList.size()) {
                WorldJsonStorage.MagicDef subDef = subList.get(subIndex);
                subIndex++; // カーソルを進める

                if (subDef == null) {
                    // null は「このメイン魔法に対するサブ効果の終わり」を意味する
                    break;
                }

                // サブ効果を展開して登録 (Flag = true)
                addDefToResult(subDef, true, outSteps, outFlags, outTexts);
            }

            // -------------------------------------------------
            // 2. その後にメイン効果 (A) を追加する
            // -------------------------------------------------
            if (mainDef != null) {
                // メイン効果を展開して登録 (Flag = false)
                addDefToResult(mainDef, false, outSteps, outFlags, outTexts);
            }
        }

        // デバッグ出力
        System.out.println("Merged Steps: " + outSteps.size());
        System.out.println("Merged Flags: " + outFlags);

        return Triple.of(outSteps, outFlags, outTexts);
    }

    /**
     * MagicDef から Step, Flag, Text を抽出してリストに追加するヘルパーメソッド
     * これにより Step と Text のズレを防止します。
     */
    private static void addDefToResult(
            WorldJsonStorage.MagicDef def,
            boolean isSub,
            List<MagicCast.Step> stepsDest,
            List<Boolean> flagsDest,
            List<String> textsDest
    ) {
        if (def == null || def.steps() == null) return;

        Map<ResourceLocation, String> textMap = def.textById(); // Stepに対応するテキスト辞書

        for (MagicCast.Step step : def.steps()) {
            // 1. Step 追加
            stepsDest.add(step);

            // 2. Flag 追加 (Mainならfalse, Subならtrue)
            flagsDest.add(isSub);

            // 3. Text 追加 (辞書から検索、なければ空文字やデフォルト)
            String chantText = "";
            if (textMap != null) {
                chantText = textMap.get(step.id());
            }
            textsDest.add(chantText);
        }
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
