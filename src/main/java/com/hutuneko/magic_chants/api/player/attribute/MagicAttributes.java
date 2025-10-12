// MagicAttributes.java
package com.hutuneko.magic_chants.api.player.attribute;

import com.hutuneko.magic_chants.Magic_chants;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MagicAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Magic_chants.MODID);

    public static final RegistryObject<Attribute> MAGIC_POWER =
            ATTRIBUTES.register("magic_power", () ->
                    new RangedAttribute("attribute.name.magic_chants.magic_power", 0, 0, Integer.MAX_VALUE)
                            .setSyncable(true));
    public static final RegistryObject<Attribute> CHANT_POWER =
            ATTRIBUTES.register("chant_power", () ->
                    new RangedAttribute("attribute.name.magic_chants.chant_power", 0.0D, 0.0D, 1024.0D)
                            .setSyncable(true));
}
