package com.sandevistanmod.tool;

import com.sandevistanmod.SandevistanMod;
import com.sandevistanmod.registry.ModRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** Regras das ferramentas: obsidiana do Multiuso, bonus de arco/besta, velocidade do arco e Aspectos (Congelante, Venenoso, Wither, Trovejante). */
@Mod.EventBusSubscriber(modid = SandevistanMod.MODID)
public final class ToolEvents {
    private ToolEvents() {}

    /** Multiuso quebra obsidiana, mas mais devagar que uma picareta de diamante (~50%). */
    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed e) {
        if (e.getEntity().getMainHandItem().getItem() instanceof MultiToolItem && e.getState().is(Tags.Blocks.OBSIDIAN)) {
            e.setNewSpeed(e.getNewSpeed() * (4.0f / 9.0f));
        }
    }

    private static boolean is(ItemStack st, java.util.List<com.sandevistanmod.compat.DeferredItem<Item>> l, int idx) {
        return st.is(l.get(idx).get());
    }

    /** Bigorna: Modulo de Encantamento + livro encantado = modulo que carrega o primeiro encantamento do livro (serve com encantamentos de outros mods). */
    @SubscribeEvent
    public static void anvil(net.minecraftforge.event.AnvilUpdateEvent e) {
        ItemStack left = e.getLeft();
        ItemStack right = e.getRight();
        if (!left.is(ModRegistries.MOD_ENCANTO.get()) || !right.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) return;
        java.util.Map<net.minecraft.world.item.enchantment.Enchantment, Integer> en =
                net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(right);
        for (java.util.Map.Entry<net.minecraft.world.item.enchantment.Enchantment, Integer> h : en.entrySet()) {
            net.minecraft.resources.ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getKey(h.getKey());
            if (key == null) continue;
            ItemStack out = ToolMods.modItem(new ToolMods.Mod(ToolMods.ENCH_CODE, key.toString(), h.getValue()));
            if (out.isEmpty()) continue;
            e.setOutput(out);
            e.setCost(Math.max(1, h.getValue()));
            e.setMaterialCost(1);
            return;
        }
    }

    /** Velocidade de ataque do Modulo de Velocidade (+0.4 por ponto) em armas de corpo a corpo e tridentes. */
    @SubscribeEvent
    public static void speedModule(net.minecraftforge.event.ItemAttributeModifierEvent e) {
        if (e.getSlotType() != net.minecraft.world.entity.EquipmentSlot.MAINHAND) return;
        ItemStack st = e.getItemStack();
        if (!ToolMods.isModTool(st)) return;
        int mask = ToolMods.mask(st.getItem());
        int p = ToolMods.power(st, ToolMods.SPEED);
        if (p > 0 && (mask & (ToolMods.M | ToolMods.T)) != 0) {
            e.addModifier(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(ToolMods.SPEED_UUID, "speed module", 0.4 * p,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        }
    }

    /** Tridente de Aco/Ciber arremessado: avisa os clientes para usarem a textura certa. */
    @SubscribeEvent
    public static void tridentJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof net.minecraft.world.entity.projectile.ThrownTrident t)) return;
        if (!(t.getOwner() instanceof LivingEntity o)) return;
        ItemStack st = o.getUseItem();
        int kind = st.is(ModRegistries.TRIDENTE_ACO.get()) ? 1 : st.is(ModRegistries.TRIDENTE_CIBER.get()) ? 2 : 0;
        t.getPersistentData().put("sdv_weapon", st.save(new net.minecraft.nbt.CompoundTag()));
        if (kind > 0) com.sandevistanmod.net.PacketDistributor.sendToAllPlayers(new com.sandevistanmod.net.TridentSkinPayload(t.getUUID(), kind));
    }

    /** Flechas de arcos/bestas de Aco (+10% de dano) e Ciber (+20%); modulo de Dano na besta (+10% por ponto). */
    @SubscribeEvent
    public static void arrowJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof AbstractArrow a) || a instanceof net.minecraft.world.entity.projectile.ThrownTrident) return;
        if (a.getPersistentData().getBoolean("sdv_boosted") || !(a.getOwner() instanceof LivingEntity o)) return;
        ItemStack w = o.getUseItem();
        if (!(w.getItem() instanceof net.minecraft.world.item.BowItem)) w = o.getMainHandItem();
        if (!(w.getItem() instanceof net.minecraft.world.item.BowItem || w.getItem() instanceof net.minecraft.world.item.CrossbowItem)) w = o.getOffhandItem();
        if (w.isEmpty() || !(w.getItem() instanceof net.minecraft.world.item.BowItem || w.getItem() instanceof net.minecraft.world.item.CrossbowItem)) return;
        a.getPersistentData().putBoolean("sdv_boosted", true);
        a.getPersistentData().put("sdv_weapon", w.save(new net.minecraft.nbt.CompoundTag()));
        double mult = 1.0;
        if (is(w, ModRegistries.FERRAMENTAS_ACO, 5) || is(w, ModRegistries.FERRAMENTAS_ACO, 6)) mult = 1.10;
        else if (is(w, ModRegistries.FERRAMENTAS_CIBER, 5) || is(w, ModRegistries.FERRAMENTAS_CIBER, 6)) mult = 1.20;
        if (w.getItem() instanceof net.minecraft.world.item.CrossbowItem) mult *= 1.0 + 0.1 * ToolMods.power(w, ToolMods.DAMAGE);
        if (mult != 1.0) a.setBaseDamage(a.getBaseDamage() * mult);
    }

    /** Modulo de Velocidade no arco: carrega mais rapido. */
    @SubscribeEvent
    public static void bowTick(LivingEntityUseItemEvent.Tick e) {
        ItemStack st = e.getItem();
        if (!(st.getItem() instanceof net.minecraft.world.item.BowItem) || !ToolMods.isModTool(st)) return;
        int p = ToolMods.power(st, ToolMods.SPEED);
        if (p > 0 && e.getEntity().getRandom().nextFloat() < Math.min(0.9f, 0.15f * p)) e.setDuration(e.getDuration() - 1);
    }

    @SubscribeEvent
    public static void aspects(LivingDamageEvent e) {
        DamageSource src = e.getSource();
        ItemStack w = ItemStack.EMPTY;
        if (src.getDirectEntity() != null && src.getDirectEntity().getPersistentData().contains("sdv_weapon")) {
            w = ItemStack.of(src.getDirectEntity().getPersistentData().getCompound("sdv_weapon"));
        }
        if (w.isEmpty() && src.getEntity() instanceof LivingEntity le && src.getDirectEntity() == le) w = le.getMainHandItem();
        if (w == null || w.isEmpty() || !ToolMods.isModTool(w)) return;
        LivingEntity t = e.getEntity();
        if (t.level().isClientSide || !t.isAlive()) return;
        int fr = ToolMods.maxTier(w, ToolMods.FREEZE);
        if (fr > 0 && !t.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)) {
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (2 + fr) * 20, 1));
            t.setTicksFrozen(t.getTicksRequiredToFreeze() + (2 + fr) * 40);
            if (t.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.SNOWFLAKE, t.getX(), t.getY() + t.getBbHeight() / 2, t.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
        }
        int vn = ToolMods.maxTier(w, ToolMods.VENOM);
        if (vn > 0 && !t.getType().is(EntityTypeTags.UNDEAD)) t.addEffect(new MobEffectInstance(MobEffects.POISON, 5 * 20, vn - 1));
        int wi = ToolMods.maxTier(w, ToolMods.WITHER);
        if (wi > 0) {
            MobEffectInstance eff = new MobEffectInstance(MobEffects.WITHER, 5 * 20, wi - 1);
            if (t.canBeAffected(eff)) t.addEffect(eff);
        }
        int th = ToolMods.maxTier(w, ToolMods.THUNDER);
        if (th > 0 && src.getEntity() instanceof LivingEntity at && t.level() instanceof ServerLevel sl) {
            try {
                long now = sl.getGameTime();
                var pd = at.getPersistentData();
                long next = pd.getLong("sdv_thunder");
                if (next > now + 200) next = 0; // tempo de jogo reiniciado/mundo diferente
                if (now >= next && Math.random() < new double[] {0.05, 0.08, 0.12}[Math.min(th, 3) - 1]) {
                    pd.putLong("sdv_thunder", now + 100);
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt != null) {
                        bolt.setPos(t.getX(), t.getY(), t.getZ());
                        bolt.setVisualOnly(true);
                        sl.addFreshEntity(bolt);
                    }
                    t.invulnerableTime = 0;
                    t.hurt(sl.damageSources().lightningBolt(), 5.0f);
                }
            } catch (Throwable ex) {
                com.mojang.logging.LogUtils.getLogger().warn("Thunderous aspect failed", ex);
            }
        }
    }
}
