// MagicAttributes.java
package io.github.hutuneko.magic_chants.api.player.attribute;

import io.github.hutuneko.magic_chants.MagicChants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MagicAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, MagicChants.MODID);

    public static final DeferredHolder<Attribute,Attribute> MAGIC_POWER =
            ATTRIBUTES.register("magic_power", () ->
                    new RangedAttribute("attribute.name.magic_chants.magic_power", 0, 0, Integer.MAX_VALUE)
                            .setSyncable(true));
    public static final DeferredHolder<Attribute,Attribute> CHANT_POWER =
            ATTRIBUTES.register("chant_power", () ->
                    new RangedAttribute("attribute.name.magic_chants.chant_power", 1.0D, 0.0D, 1024.0D)
                            .setSyncable(true));
}
