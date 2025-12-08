package com.hutuneko.magic_chants;

import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.player.attribute.MagicAttributes;
import com.hutuneko.magic_chants.entity.InvisibleLandMineRenderer;
import com.hutuneko.magic_chants.item.MagicItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Magic_chants.MODID)
public class Magic_chants {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "magic_chants";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public Magic_chants() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MagicRegister.init();
        MagicItems.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        ModRegistry.register(modEventBus);
        MagicAttributes.ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
    }
    private void setup(final FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            MagicNetwork.init();
        });
    }
    private void clientSetup(final FMLClientSetupEvent e) {
        e.enqueueWork(() -> {

        });
    }
}
