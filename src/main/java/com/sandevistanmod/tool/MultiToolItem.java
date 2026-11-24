package com.sandevistanmod.tool;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sandevistanmod.data.Tx;
import com.sandevistanmod.registry.ModRegistries;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

/** Ferramenta 3 em 1 (picareta + machado + pa): serve para qualquer bloco e como arma. Aceita ate 4 modulos na Mesa de Upgrades. */
public class MultiToolItem extends Item {
    public static final int SLOTS = ToolMods.SLOTS;
    private final Multimap<Attribute, AttributeModifier> mods;

    public MultiToolItem(Properties p) {
        super(p);
        // Dano 8 e velocidade de ataque 0.7 (o Modulo de Velocidade soma 0.4 por ponto, via ToolEvents)
        ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
        b.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0, AttributeModifier.Operation.ADDITION));
        b.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.3, AttributeModifier.Operation.ADDITION));
        this.mods = b.build();
    }

    public static Properties props() {
        return new Properties().durability(2031).fireResistant();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? mods : super.getDefaultAttributeModifiers(slot);
    }

    // ---- mineracao: serve para tudo que uma picareta, um machado ou uma pa minera (nivel netherite) ----
    private static boolean mines(BlockState s) {
        return s.is(BlockTags.MINEABLE_WITH_PICKAXE) || s.is(BlockTags.MINEABLE_WITH_AXE) || s.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return mines(state) ? 9.0f : 1.0f;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return mines(state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return mines(state);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(action) || ToolActions.DEFAULT_AXE_ACTIONS.contains(action)
                || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(action);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0f) {
            stack.hurtAndBreak(1, miner, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    // ---- comportamento ----
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        InteractionResult r = Items.NETHERITE_AXE.useOn(ctx); // descascar troncos
        if (r.consumesAction()) return r;
        return Items.NETHERITE_SHOVEL.useOn(ctx); // fazer caminho de terra
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModRegistries.LINGOTE_ACO.get());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.world.item.enchantment.Enchantment e) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Tx.t(ChatFormatting.GRAY, "multi.tip"));
    }
}
