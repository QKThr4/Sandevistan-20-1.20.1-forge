package com.sandevistanmod.data;

import com.sandevistanmod.SandevistanMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Dados do jogador (equivalente ao AttachmentType do NeoForge): capability anexada a todo Player, copiada ao morrer. */
@Mod.EventBusSubscriber(modid = SandevistanMod.MODID)
public final class SandevistanCaps {
    public static final Capability<SandevistanData> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation KEY = new ResourceLocation(SandevistanMod.MODID, "sandevistan");

    private SandevistanCaps() {}

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final SandevistanData data = new SandevistanData();
        private final LazyOptional<SandevistanData> opt = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == CAP ? opt.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.deserializeNBT(nbt);
        }
    }

    @Mod.EventBusSubscriber(modid = SandevistanMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void register(RegisterCapabilitiesEvent e) {
            e.register(SandevistanData.class);
        }
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player) {
            e.addCapability(KEY, new Provider());
        }
    }

    /** copyOnDeath: ao renascer, leva os dados do jogador antigo para o novo. */
    @SubscribeEvent
    public static void clone(PlayerEvent.Clone e) {
        e.getOriginal().reviveCaps();
        e.getOriginal().getCapability(CAP).ifPresent(old ->
                e.getEntity().getCapability(CAP).ifPresent(neu -> neu.deserializeNBT(old.serializeNBT())));
        e.getOriginal().invalidateCaps();
    }
}
