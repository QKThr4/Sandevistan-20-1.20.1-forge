package com.sandevistanmod;

import com.sandevistanmod.net.Net;
import com.sandevistanmod.registry.ModRegistries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SandevistanMod.MODID)
public class SandevistanMod {
    public static final String MODID = "sandevistan";
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public static void LOGGER_WARN(String msg) {
        LOGGER.warn(msg);
    }

    public SandevistanMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistries.SOUNDS.register(modBus);
        ModRegistries.EFFECTS.register(modBus);
        ModRegistries.BLOCKS.register(modBus);
        ModRegistries.POIS.register(modBus);
        ModRegistries.PROFESSIONS.register(modBus);
        ModRegistries.MENUS.register(modBus);
        ModRegistries.BLOCK_ENTITIES.register(modBus);
        ModRegistries.ENTITIES.register(modBus);
        ModRegistries.ITEMS.register(modBus);
        ModRegistries.TABS.register(modBus);
        modBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(() -> {
            Net.register();
            ModRegistries.registerPoiStates();
        }));
    }
}
