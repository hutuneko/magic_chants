package io.github.hutuneko.magic_chants.api.util;

import io.github.hutuneko.magic_chants.api.file.WorldJsonStorage;
import io.github.hutuneko.magic_chants.api.magic.MagicCast;

import java.util.*;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import com.ibm.icu.impl.Pair;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

public class MagicChantsAPI {
    public static Pair<List<MagicCast.Step>, List<String>>
    mergeAndAlignC(
            List<WorldJsonStorage.MagicDef> mainList, 
            List<WorldJsonStorage.MagicDef> subList   
    ) {
        List<MagicCast.Step> outSteps = new ArrayList<>();
        List<String> outTexts = new ArrayList<>();

        if (mainList == null) mainList = Collections.emptyList();
        if (subList == null) subList = Collections.emptyList();

        int subIndex = 0; 

        
        for (WorldJsonStorage.MagicDef mainDef : mainList) {
            
            while (subIndex < subList.size()) {
                WorldJsonStorage.MagicDef subDef = subList.get(subIndex);
                subIndex++;
                if (subDef.isEmpty()) {
                    break;
                }
                addDefToResult(subDef, outSteps, outTexts);
            }
            if (!mainDef.isEmpty()) {
                addDefToResult(mainDef, outSteps, outTexts);
            }
        }

        
        System.out.println("Merged Steps: " + outSteps.size());

        return Pair.of(outSteps, outTexts);
    }

    /**
     * MagicDef から Step, Flag, Text を抽出してリストに追加するヘルパーメソッド
     * これにより Step と Text のズレを防止します。
     */
    private static void addDefToResult(
            WorldJsonStorage.MagicDef def,
            List<MagicCast.Step> stepsDest,
            List<String> textsDest
    ) {
        if (def.isEmpty() || def.steps() == null) return;

        Map<Identifier, String> textMap = def.textById(); 

        for (MagicCast.Step step : def.steps()) {
            
            stepsDest.add(step);
            
            String chantText = "";
            if (textMap != null) {
                chantText = textMap.get(step.id());
            }
            textsDest.add(chantText);
        }
    }

    public static void pullEntityTowards(Entity target, Vec3 center, double strength) {
        if (target == null || center == null) return;

        Vec3 dir = center.subtract(target.position());
        double lenSqr = dir.lengthSqr();
        if (lenSqr < 1e-4) return; 

        Vec3 motion = dir.normalize().scale(strength);

        
        if (target.onGround()) {
            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.1, 0)); 
        }

        target.setDeltaMovement(target.getDeltaMovement().add(motion));
        target.hurtMarked = true;
    }
    public static void setOwnerTagToAllItems(ServerPlayer player) {
        Inventory inventory = player.getInventory();

        final int TOTAL_SLOTS = 50;
        
        for (int i = 0; i < TOTAL_SLOTS; ++i) {

            
            ItemStack stack = inventory.getItem(i);

            if (!stack.isEmpty()) {
                
                setOwnerTag(stack, player);
            }
        }
        
        inventory.setChanged();
    }
    public static CompoundTag getOrCreateTag(ItemStack stack) {
        var cdata = stack.get(DataComponents.CUSTOM_DATA);
        if (cdata == null){
            stack.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            cdata = stack.get(DataComponents.CUSTOM_DATA);
        }
        return cdata != null ? cdata.copyTag() : new CompoundTag();
    }
    public static CompoundTag getTag(ItemStack stack) {
        var cdata = stack.get(DataComponents.CUSTOM_DATA);
        if (cdata == null){
            stack.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            cdata = stack.get(DataComponents.CUSTOM_DATA);
        }
        return cdata != null ? cdata.copyTag() : null;
    }
    public static void setOwnerTag(ItemStack stack, Player owner) {
        CompoundTag tag = getOrCreateTag(stack);

        CompoundTag customTag = new CompoundTag();
        customTag.putString("magic_chants:creativeuuid", owner.getUUID().toString());

        tag.put("magic_chants:creative", customTag);
    }
    public static <T extends Event> void post(T event) {
        NeoForge.EVENT_BUS.post(event);
    }
}
