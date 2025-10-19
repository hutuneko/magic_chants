package com.hutuneko.magic_chants;

import com.hutuneko.magic_chants.api.magic.MagicClassRegistry;
import com.hutuneko.magic_chants.magic.action.*;
import com.hutuneko.magic_chants.magic.addition.Magic_DelayNext;
import com.hutuneko.magic_chants.magic.addition.Magic_RepeatNext;
import com.hutuneko.magic_chants.magic.addition.Magic_Target;
import com.hutuneko.magic_chants.magic.addition.Magic_TargetPos;
import com.hutuneko.magic_chants.magic.target.MagicT_Self;
import com.hutuneko.magic_chants.magic.target.MagicT_Selfeyespos;
import com.hutuneko.magic_chants.magic.target.MagicT_Selfpos;
import net.minecraft.resources.ResourceLocation;

public class MagicRegister {
    public static void init(){
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_thunder"),
                Magic_Thunder.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_explosion"),
                Magic_Explosion.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_selfeyes"),
                MagicT_Selfeyespos.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_repeat"),
                Magic_RepeatNext.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_delay"),
                Magic_DelayNext.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_bind_sword"),
                Magic_BindSword.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_power"),
                Magic_Power.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_target"),
                Magic_Target.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_self"),
                MagicT_Self.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_selfpos"),
                MagicT_Selfpos.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_targetpos"),
                Magic_TargetPos.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_exhaustion"),
                Magic_Exhaustion.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_move"),
                Magic_Move.class
        );
        MagicClassRegistry.registerSpell(
                new ResourceLocation(Magic_chants.MODID, "magic_teleport"),
                Magic_Teleport.class
        );
    }
}
