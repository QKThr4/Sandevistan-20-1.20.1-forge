package com.sandevistanmod.compat;

import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Faz o papel do DeferredRegister.Blocks do NeoForge. */
public class BlockRegister {
    private final DeferredRegister<Block> reg;

    public BlockRegister(String modid) {
        this.reg = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
    }

    public <T extends Block> DeferredBlock<T> register(String name, Supplier<T> sup) {
        return new DeferredBlock<>(reg.register(name, sup));
    }

    public void register(IEventBus bus) {
        reg.register(bus);
    }
}
