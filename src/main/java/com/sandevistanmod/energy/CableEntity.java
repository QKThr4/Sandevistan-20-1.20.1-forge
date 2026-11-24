package com.sandevistanmod.energy;

import com.sandevistanmod.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

/** Bloco-entidade do cabo: so existe para que outros mods consigam entregar FE ao cabo (capability de energia). */
public class CableEntity extends BlockEntity {
    private LazyOptional<IEnergyStorage> opt = LazyOptional.empty();

    public CableEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.CABLE_BE.get(), pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ENERGY && level != null) {
            if (!opt.isPresent()) opt = LazyOptional.of(() -> CableBlock.storage(level, worldPosition));
            return opt.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        opt.invalidate();
    }
}
