package com.sandevistanmod.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

/** Atalhos para ler a capability de energia (FE) de blocos e itens, ou null se nao houver. */
public final class Energy {
    private Energy() {}

    public static IEnergyStorage at(BlockGetter level, BlockPos pos, Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;
        return be.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);
    }

    public static IEnergyStorage item(ItemStack st) {
        if (st.isEmpty()) return null;
        return st.getCapability(ForgeCapabilities.ENERGY).orElse(null);
    }
}
