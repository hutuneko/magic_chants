package com.hutuneko.magic_chants;

import com.hutuneko.magic_chants.api.magic.MagicClassRegistry;
import com.hutuneko.magic_chants.magic.action.*;
import com.hutuneko.magic_chants.magic.addition.*;
import com.hutuneko.magic_chants.magic.target.*;
import net.minecraft.resources.ResourceLocation;

public class MagicRegister {
    public static void init(){
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_thunder"),
                Magic_Thunder.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_explosion"),
                Magic_Explosion.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_selfeyes"),
                Magic_Selfeyespos.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_repeat"),
                Magic_RepeatNext.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_delay"),
                Magic_DelayNext.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_bind_sword"),
                Magic_BindSword.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_power"),
                Magic_Power.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_target"),
                Magic_Target.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_self"),
                Magic_Self.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_selfpos"),
                Magic_Selfpos.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_targetpos"),
                Magic_TargetPos.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_exhaustion"),
                Magic_Exhaustion.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_move"),
                Magic_Move.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_teleport"),
                Magic_Teleport.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_kill"),
                Magic_Kill.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_wtf"),
                Magic_wtf.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_replay"),
                Magic_Replay.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_break"),
                Magic_Break.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_title"),
                Magic_Title.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_spend"),
                Magic_Spend.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_gethealth"),
                Magic_GetHealth.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_getexperience"),
                Magic_GetExperience.class
        );
        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_infrespawn"),
                Magic_InfRespawn.class
        );

//        MagicClassRegistry.registerMagic(
//                new ResourceLocation(Magic_chants.MODID, "magic_suction"),
//                Magic_Suction.class
//        );
//        MagicClassRegistry.registerMagic(
//                new ResourceLocation(Magic_chants.MODID, "magic_accelerate"),
//                Magic_Accelerate.class
//        );


        MagicClassRegistry.registerMagic(
                new ResourceLocation(Magic_chants.MODID, "magic_set"),
                Magic_Set.class
        );
    }
}
