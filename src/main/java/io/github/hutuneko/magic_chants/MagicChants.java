package io.github.hutuneko.magic_chants;

import io.github.hutuneko.magic_chants.api.player.attribute.MagicAttributes;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(MagicChants.MODID)
public class MagicChants {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "magic_chants";
    public static Identifier rl(String s){
        return Identifier.fromNamespaceAndPath(MODID,s);
    }
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public MagicChants(IEventBus modEventBus, ModContainer modContainer) {
        MagicRegister.init();
        ModRegistry.register(modEventBus);
        MagicPowerProvider.ATTACHMENT_TYPES.register(modEventBus);
        MagicAttributes.ATTRIBUTES.register(modEventBus);
    }
}
