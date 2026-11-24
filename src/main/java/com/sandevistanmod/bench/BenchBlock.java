package com.sandevistanmod.bench;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Bancada do mod: 0 Montagem, 1 Medicinal (plantas), 2 Reciclador de Sucata. Abre um menu 3x3 so com itens das receitas dela. */
public class BenchBlock extends Block {
    public static final int ASSEMBLY = 0;
    public static final int MEDICINAL = 1;
    public static final int RECYCLER = 2;

    private final int kind;

    public BenchBlock(Properties properties, int kind) {
        super(properties);
        this.kind = kind;
    }

    /** Reciclador: clicar com sucata na mao transforma CADA sucata em 2 lingotes (50% ferro, 50% aco). */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (kind == RECYCLER && stack.is(com.sandevistanmod.registry.ModRegistries.SUCATA.get())) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            int n = stack.getCount();
            int steel = 0;
            int iron = 0;
            for (int k = 0; k < n * 2; k++) {
                if (level.random.nextBoolean()) steel++;
                else iron++;
            }
            if (!player.getAbilities().instabuild) stack.shrink(n);
            give(player, new ItemStack(com.sandevistanmod.registry.ModRegistries.LINGOTE_ACO.get(), steel));
            give(player, new ItemStack(Items.IRON_INGOT, iron));
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_STONECUTTER_TAKE_RESULT, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.CONSUME;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player,
                new SimpleMenuProvider((id, inv, p) -> new BenchMenu(id, inv, ContainerLevelAccess.create(level, pos), kind),
                        Component.translatable(this.getDescriptionId())), buf -> buf.writeVarInt(kind));
        return InteractionResult.CONSUME;
    }

    private static void give(Player p, ItemStack st) {
        while (!st.isEmpty()) {
            ItemStack part = st.split(Math.min(st.getCount(), st.getMaxStackSize()));
            if (!p.getInventory().add(part)) p.drop(part, false);
        }
    }
}
