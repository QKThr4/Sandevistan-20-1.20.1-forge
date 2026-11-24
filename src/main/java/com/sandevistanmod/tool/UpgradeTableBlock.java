package com.sandevistanmod.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Mesa de Upgrades: encaixa modulos na Ferramenta Multiuso. */
public class UpgradeTableBlock extends Block {
    public UpgradeTableBlock(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player, new SimpleMenuProvider((id, inv, p) -> new UpgradeMenu(id, inv, ContainerLevelAccess.create(level, pos)),
                Component.translatable(this.getDescriptionId())), pos);
        return InteractionResult.CONSUME;
    }
}
