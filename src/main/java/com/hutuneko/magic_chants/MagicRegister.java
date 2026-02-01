package com.hutuneko.magic_chants;

import com.hutuneko.magic_chants.api.magic.MagicClassRegistry;
import com.hutuneko.magic_chants.magic.action.*;
import com.hutuneko.magic_chants.magic.addition.*;
import com.hutuneko.magic_chants.magic.target.*;

public class MagicRegister {
    public static void init(){
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_thunder"),
                Magic_Thunder.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_explosion"),
                Magic_Explosion.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_selfeyes"),
                Magic_Selfeyespos.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_repeat"),
                Magic_RepeatNext.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_delay"),
                Magic_DelayNext.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_bind_sword"),
                Magic_BindSword.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_power"),
                Magic_Power.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_target"),
                Magic_Target.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_self"),
                Magic_Self.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_selfpos"),
                Magic_Selfpos.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_targetpos"),
                Magic_TargetPos.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_exhaustion"),
                Magic_Exhaustion.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_move"),
                Magic_Move.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_teleport"),
                Magic_Teleport.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_kill"),
                Magic_Kill.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_wtf"),
                Magic_wtf.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_replay"),
                Magic_Replay.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_break"),
                Magic_Break.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_title"),
                Magic_Title.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_spend"),
                Magic_Spend.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_gethealth"),
                Magic_GetHealth.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_getexperience"),
                Magic_GetExperience.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_insrespawn"),
                Magic_InsRespawn.class
        );

        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_landmine"),
                Magic_LandMine.class
        );

        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_summon"),
                Magic_Summon.class
        );
        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_and"),
                Magic_And.class
        );

//        MagicClassRegistry.registerMagic(
//                Magic_chants.rl("magic_suction"),
//                Magic_Suction.class
//        );
//        MagicClassRegistry.registerMagic(
//                Magic_chants.rl("magic_accelerate"),
//                Magic_Accelerate.class
//        );

        //上のコメントアウトしてる奴の
//        {
//            "chant": "吸引",
//                "steps": [{ "id": "magic_chants:magic_suction" }]
//        },
//        {
//            "chant": "suction",
//                "steps": [{ "id": "magic_chants:magic_suction" }]
//        },
//        {
//            "chant": "加速",
//                "steps": [{ "id": "magic_chants:magic_accelerate" }]
//        },
//        {
//            "chant": "accelerate",
//                "steps": [{ "id": "magic_chants:magic_accelerate" }]
//        },

        MagicClassRegistry.registerMagic(
                Magic_chants.rl("magic_set"),
                Magic_Set.class
        );
    }
}
