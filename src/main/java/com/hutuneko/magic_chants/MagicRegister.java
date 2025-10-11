package com.hutuneko.magic_chants;

import com.hutuneko.magic_chants.api.magic.MagicClassRegistry;
import com.hutuneko.magic_chants.magic.action.Magic_BindSword;
import com.hutuneko.magic_chants.magic.action.Magic_Explosion;
import com.hutuneko.magic_chants.magic.action.Magic_Thunder;
import com.hutuneko.magic_chants.magic.addition.Magic_DelayNext;
import com.hutuneko.magic_chants.magic.addition.Magic_RepeatNext;
import com.hutuneko.magic_chants.magic.target.MagicT_Selfeyes;
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
                MagicT_Selfeyes.class
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
                Magic_BindSword.class);
    }
}
