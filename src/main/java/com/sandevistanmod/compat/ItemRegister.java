package com.sandevistanmod.compat;

import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Faz o papel do DeferredRegister.Items do NeoForge. */
public class ItemRegister {
    private final DeferredRegister<Item> reg;

    public ItemRegister(String modid) {
        this.reg = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
    }

    public <T extends Item> DeferredItem<T> register(String name, Supplier<T> sup) {
        return new DeferredItem<>(reg.register(name, sup));
    }

    public DeferredItem<Item> registerSimpleItem(String name, Item.Properties props) {
        return new DeferredItem<>(reg.register(name, () -> new Item(props)));
    }

    public void register(IEventBus bus) {
        reg.register(bus);
    }
}
