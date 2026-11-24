package com.sandevistanmod.compat;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class DeferredBlock<T extends Block> implements Supplier<T>, ItemLike {
    private final RegistryObject<T> ro;

    public DeferredBlock(RegistryObject<T> ro) {
        this.ro = ro;
    }

    @Override
    public T get() {
        return ro.get();
    }

    @Override
    public Item asItem() {
        return ro.get().asItem();
    }

    public ResourceLocation getId() {
        return ro.getId();
    }

    public BlockState defaultBlockState() {
        return ro.get().defaultBlockState();
    }
}
