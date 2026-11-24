package com.sandevistanmod.compat;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

/** Equivalente ao DeferredHolder do NeoForge (R = tipo do registro, T = tipo do objeto). */
public class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final RegistryObject<T> ro;

    public DeferredHolder(RegistryObject<T> ro) {
        this.ro = ro;
    }

    @Override
    public T get() {
        return ro.get();
    }

    public ResourceLocation getId() {
        return ro.getId();
    }

    public boolean isPresent() {
        return ro.isPresent();
    }
}
