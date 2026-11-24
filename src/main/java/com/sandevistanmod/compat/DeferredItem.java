package com.sandevistanmod.compat;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

/** Equivalente ao DeferredItem do NeoForge, em cima do RegistryObject do Forge 1.20.1. */
public class DeferredItem<T extends Item> implements Supplier<T>, ItemLike {
    private final RegistryObject<T> ro;

    public DeferredItem(RegistryObject<T> ro) {
        this.ro = ro;
    }

    @Override
    public T get() {
        return ro.get();
    }

    @Override
    public Item asItem() {
        return ro.get();
    }

    public ResourceLocation getId() {
        return ro.getId();
    }

    public boolean isPresent() {
        return ro.isPresent();
    }

    public ItemStack toStack() {
        return new ItemStack(ro.get());
    }

    public ItemStack toStack(int count) {
        return new ItemStack(ro.get(), count);
    }
}
