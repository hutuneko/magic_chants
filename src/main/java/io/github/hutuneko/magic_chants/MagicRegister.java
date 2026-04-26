package io.github.hutuneko.magic_chants;

import io.github.hutuneko.magic_chants.api.magic.MagicClassRegistry;
import io.github.hutuneko.magic_chants.magic.MagicTest;
import io.github.hutuneko.magic_chants.magic.action.*;
import io.github.hutuneko.magic_chants.magic.addition.*;
import io.github.hutuneko.magic_chants.magic.target.*;

public class MagicRegister {
    public static void init(){
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_thunder"),
                MagicThunder.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_explosion"),
                MagicExplosion.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_selfeyes"),
                MagicSelfeyespos.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_repeat"),
                MagicRepeatNext.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_delay"),
                MagicDelayNext.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_bind_sword"),
                MagicBindSword.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_power"),
                MagicPower.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_target"),
                MagicTarget.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_self"),
                MagicSelf.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_selfpos"),
                MagicSelfpos.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_targetpos"),
                MagicTargetPos.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_exhaustion"),
                MagicExhaustion.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_move"),
                MagicMove.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_teleport"),
                MagicTeleport.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_kill"),
                MagicKill.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_wtf"),
                Magicwtf.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_replay"),
                MagicReplay.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_break"),
                MagicBreak.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_title"),
                MagicTitle.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_spend"),
                MagicSpend.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_gethealth"),
                MagicGetHealth.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_getexperience"),
                MagicGetExperience.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_insrespawn"),
                MagicInsRespawn.class
        );

        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_landmine"),
                MagicLandMine.class
        );

        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_summon"),
                MagicSummon.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_and"),
                MagicAnd.class
        );
        MagicClassRegistry.registerMagic(
                MagicChants.rl("magic_test"),
                MagicTest.class
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
                MagicChants.rl("magic_set"),
                MagicSet.class
        );
    }
}
