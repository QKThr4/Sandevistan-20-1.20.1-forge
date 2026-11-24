package com.sandevistanmod.tool;

import com.sandevistanmod.registry.ModRegistries;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/** Ferramentas de Aco (nivel entre ferro e diamante, mais perto do ferro) e Ciber (nivel de diamante, 4 modulos, sem encantar). */
public final class ModTools {
    private ModTools() {}

    /** Marca as ferramentas Ciber: aceitam 4 modulos na Mesa de Upgrades e nao podem ser encantadas. */
    public interface Cyber {}

    public static final class ModTier implements Tier {
        private final int uses;
        private final float speed;
        private final float damage;
        private final int level;
        private final int ench;

        ModTier(int uses, float speed, float damage, int level, int ench) {
            this.uses = uses;
            this.speed = speed;
            this.damage = damage;
            this.level = level;
            this.ench = ench;
        }

        @Override
        public int getUses() {
            return uses;
        }

        @Override
        public float getSpeed() {
            return speed;
        }

        @Override
        public float getAttackDamageBonus() {
            return damage;
        }

        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public int getEnchantmentValue() {
            return ench;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModRegistries.LINGOTE_ACO.get());
        }
    }

    // aco nao quebra obsidiana (como o ferro); Ciber quebra (como o diamante)
    public static final ModTier STEEL = new ModTier(450, 6.5f, 2.5f, 2, 12);
    public static final ModTier STEEL_MACE = new ModTier(600, 6.5f, 2.5f, 2, 12);
    public static final ModTier CYBER = new ModTier(1561, 8.0f, 3.0f, 3, 0);
    public static final ModTier CYBER_MACE = new ModTier(2000, 8.0f, 3.0f, 3, 0);

    private static Item.Properties props() {
        return new Item.Properties();
    }

    /** Tridente com dano proprio (o da 1.20.1 tem o dano fixo no item). */
    public static class ModTrident extends TridentItem {
        private final Multimap<Attribute, AttributeModifier> mods;

        public ModTrident(int uses, double dmg) {
            super(new Item.Properties().durability(uses).rarity(net.minecraft.world.item.Rarity.UNCOMMON));
            ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
            b.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", dmg, AttributeModifier.Operation.ADDITION));
            b.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.9, AttributeModifier.Operation.ADDITION));
            this.mods = b.build();
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
            return slot == EquipmentSlot.MAINHAND ? mods : super.getDefaultAttributeModifiers(slot);
        }
    }

    // ---------------- Aco ----------------
    public static Item steelSword() { return new SwordItem(STEEL, 3, -2.4f, props()); }
    public static Item steelPick() { return new PickaxeItem(STEEL, 1, -2.8f, props()); }
    public static Item steelAxe() { return new AxeItem(STEEL, 6.0f, -3.1f, props()); }
    public static Item steelShovel() { return new ShovelItem(STEEL, 1.5f, -3.0f, props()); }
    public static Item steelHoe() { return new HoeItem(STEEL, -2, -1.0f, props()); }
    public static Item steelBow() { return new BowItem(new Item.Properties().durability(440)); }
    public static Item steelCrossbow() { return new CrossbowItem(new Item.Properties().durability(540)); }
    // vanilla: arco 384, besta 465, tridente 250 (maca/tridente ~10% mais dano base)
    public static Item steelTrident() { return new ModTrident(400, 7.8); }
    public static Item steelMace() { return new Mace(STEEL_MACE); }

    /** Flecha de aco: +25% de dano, atravessa 1 mob e serve em qualquer arco/besta. */
    public static class SteelArrow extends ArrowItem {
        public SteelArrow(Item.Properties p) {
            super(p);
        }

        @Override
        public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter) {
            AbstractArrow a = super.createArrow(level, ammo, shooter);
            a.setBaseDamage(a.getBaseDamage() * 1.25);
            a.setPierceLevel((byte) 1);
            return a;
        }
    }

    public static class Mace extends AxeItem {
        public Mace(ModTier t) {
            super(t, 7.0f, -3.2f, props());
        }
    }

    // ---------------- Ciber (nao encantaveis, 4 modulos) ----------------
    public static class CySword extends SwordItem implements Cyber {
        public CySword() { super(CYBER, 3, -2.4f, props()); }
        @Override public boolean isEnchantable(ItemStack s) { return false; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack s, Enchantment e) { return false; }
        @Override public boolean isBookEnchantable(ItemStack s, ItemStack book) { return false; }
    }

    public static class CyPick extends PickaxeItem implements Cyber {
        public CyPick() { super(CYBER, 1, -2.8f, props()); }
        @Override public boolean isEnchantable(ItemStack s) { return false; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack s, Enchantment e) { return false; }
        @Override public boolean isBookEnchantable(ItemStack s, ItemStack book) { return false; }
    }

    public static class CyAxe extends AxeItem implements Cyber {
        public CyAxe() { super(CYBER, 6.0f, -3.1f, props()); }
        @Override public boolean isEnchantable(ItemStack s) { return false; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack s, Enchantment e) { return false; }
        @Override public boolean isBookEnchantable(ItemStack s, ItemStack book) { return false; }
    }

    public static class CyShovel extends ShovelItem implements Cyber {
        public CyShovel() { super(CYBER, 1.5f, -3.0f, props()); }
        @Override public boolean isEnchantable(ItemStack s) { return false; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack s, Enchantment e) { return false; }
        @Override public boolean isBookEnchantable(ItemStack s, ItemStack book) { return false; }
    }

    public static class CyHoe extends HoeItem implements Cyber {
        public CyHoe() { super(CYBER, -2, -1.0f, props()); }
        @Override public boolean isEnchantable(ItemStack s) { return false; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack s, Enchantment e) { return false; }
        @Override public boolean isBookEnchantable(ItemStack s, ItemStack book) { return false; }
    }

    public static class CyBow extends BowItem implements Cyber {
        public CyBow() { super(new Item.Properties().durability(900)); }
        @Override public boolean isEnchantable(ItemStack s) { return false; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack s, Enchantment e) { return false; }
        @Override public boolean isBookEnchantable(ItemStack s, ItemStack book) { return false; }
    }

    public static class CyCrossbow extends CrossbowItem implements Cyber {
        public CyCrossbow() { super(new Item.Properties().durability(900)); }
        @Override public boolean isEnchantable(ItemStack s) { return false; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack s, Enchantment e) { return false; }
        @Override public boolean isBookEnchantable(ItemStack s, ItemStack book) { return false; }
    }

    public static class CyTrident extends ModTrident implements Cyber {
        public CyTrident() { super(900, 8.4); }
        @Override public boolean isEnchantable(ItemStack s) { return false; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack s, Enchantment e) { return false; }
        @Override public boolean isBookEnchantable(ItemStack s, ItemStack book) { return false; }
    }

    public static class CyMace extends Mace implements Cyber {
        public CyMace() { super(CYBER_MACE); }
        @Override public boolean isEnchantable(ItemStack s) { return false; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack s, Enchantment e) { return false; }
        @Override public boolean isBookEnchantable(ItemStack s, ItemStack book) { return false; }
    }
}
