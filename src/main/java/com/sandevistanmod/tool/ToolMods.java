package com.sandevistanmod.tool;

import com.sandevistanmod.SandevistanMod;
import com.sandevistanmod.registry.ModRegistries;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;

/** Modulos de ferramenta: leitura/gravacao nos itens (Multiuso e ferramentas Ciber) e regras do que cada ferramenta aceita. */
public final class ToolMods {
    private ToolMods() {}

    public static final int SLOTS = 4;
    private static final String KEY = "sdv_mods";
    private static final String KEY_E = "sdv_ench";
    /** Tipos de modulo (codigo = tipo * 10 + tier). */
    public static final int SPEED = 1, DAMAGE = 2, EFFICIENCY = 3, FORTUNE = 4, UNBREAK = 5, FREEZE = 6, VENOM = 7, WITHER = 8, THUNDER = 9;
    /** Classes de ferramenta. */
    public static final int M = 1, P = 2, B = 4, X = 8, T = 16;

    /** Codigo de um Modulo de Encantamento (o encantamento e o nivel ficam guardados a parte, ex.: "minecraft:fire_aspect", 2). */
    public static final int ENCH_CODE = 100;

    /** Um modulo instalado: codigo (tipo*10+tier, ou 100 para encantamento) e, no caso do encantamento, o id e o nivel. */
    public record Mod(int code, String e, int l) {
        public static final Mod NONE = new Mod(0, "", 0);

        public boolean none() {
            return code <= 0;
        }
    }

    public static boolean isModTool(ItemStack st) {
        return !st.isEmpty() && (st.getItem() instanceof MultiToolItem || st.getItem() instanceof ModTools.Cyber);
    }

    public static int mask(Item it) {
        if (it instanceof MultiToolItem) return M | P;
        if (it instanceof SwordItem || it instanceof AxeItem) return M;
        if (it instanceof PickaxeItem || it instanceof ShovelItem || it instanceof HoeItem) return P;
        if (it instanceof BowItem) return B;
        if (it instanceof CrossbowItem) return X;
        if (it instanceof TridentItem) return T;
        return 0;
    }

    // ---- codigos ----
    public static int type(int code) {
        return code > 0 && code < 100 ? code / 10 : 0;
    }

    public static int tier(int code) {
        return code % 10;
    }

    public static boolean isEnchCode(int code) {
        return code >= ENCH_CODE;
    }

    private static final java.util.regex.Pattern ID = java.util.regex.Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    /** Modulo contido num item (ou Mod.NONE). Modulo de Encantamento vazio (sem livro na bigorna) nao vale nada. */
    public static Mod modOf(ItemStack st) {
        if (st.isEmpty()) return Mod.NONE;
        if (st.is(ModRegistries.MOD_ENCANTO.get())) {
            CompoundTag tag = st.getTag() == null ? new CompoundTag() : st.getTag();
            String id = tag.getString("e");
            int lvl = tag.getInt("l");
            return ID.matcher(id).matches() && lvl >= 1 ? new Mod(ENCH_CODE, id, lvl) : Mod.NONE;
        }
        for (int t = 0; t < ModRegistries.MODULOS.size(); t++) {
            for (int k = 0; k < 3; k++) if (st.is(ModRegistries.MODULOS.get(t).get(k).get())) return new Mod((t + 1) * 10 + k + 1, "", 0);
        }
        return Mod.NONE;
    }

    public static ItemStack modItem(Mod m) {
        if (m == null || m.none()) return ItemStack.EMPTY;
        if (isEnchCode(m.code())) {
            ItemStack st = new ItemStack(ModRegistries.MOD_ENCANTO.get());
            st.getOrCreateTag().putString("e", m.e());
            st.getOrCreateTag().putInt("l", m.l());
            return st;
        }
        int t = m.code() / 10;
        int k = m.code() % 10;
        if (t < 1 || t > ModRegistries.MODULOS.size() || k < 1 || k > 3) return ItemStack.EMPTY;
        return new ItemStack(ModRegistries.MODULOS.get(t - 1).get(k - 1).get());
    }

    // ---- dados na ferramenta ----
    public static int[] getMods(ItemStack tool) {
        int[] raw = tool.getTag() == null ? new int[0] : tool.getTag().getIntArray(KEY);
        int[] out = new int[SLOTS];
        System.arraycopy(raw, 0, out, 0, Math.min(raw.length, SLOTS));
        return out;
    }

    /** Modulos completos (com o encantamento guardado em cada encaixe). */
    public static Mod[] getModsFull(ItemStack tool) {
        CompoundTag tag = tool.getTag() == null ? new CompoundTag() : tool.getTag();
        int[] codes = getMods(tool);
        net.minecraft.nbt.ListTag l = tag.getList(KEY_E, 8);
        Mod[] out = new Mod[SLOTS];
        for (int k = 0; k < SLOTS; k++) {
            String raw = k < l.size() ? l.getString(k) : "";
            int bar = raw.lastIndexOf('|');
            if (codes[k] >= ENCH_CODE && bar > 0) {
                int lv = 1;
                try {
                    lv = Integer.parseInt(raw.substring(bar + 1));
                } catch (NumberFormatException ex) {
                    // fica nivel 1
                }
                out[k] = new Mod(codes[k], raw.substring(0, bar), lv);
            } else {
                out[k] = new Mod(codes[k], "", 0);
            }
        }
        return out;
    }

    /** Soma dos tiers dos modulos de um tipo. */
    public static int power(ItemStack tool, int type) {
        int n = 0;
        if (tool.isEmpty() || !isModTool(tool)) return 0;
        for (int m : getMods(tool)) if (type(m) == type) n += m % 10;
        return n;
    }

    /** Maior tier de um tipo (usado pelos Aspectos). */
    public static int maxTier(ItemStack tool, int type) {
        int n = 0;
        if (tool.isEmpty() || !isModTool(tool)) return 0;
        for (int m : getMods(tool)) if (type(m) == type) n = Math.max(n, m % 10);
        return n;
    }

    // ---- regras ----
    private static Enchantment keyOf(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        return rl == null ? null : ForgeRegistries.ENCHANTMENTS.getValue(rl);
    }

    /** Encantamentos que um modulo causa nesta ferramenta. */
    private static void enchantsOf(ItemStack tool, Mod m, Map<Enchantment, Integer> want) {
        int mask = mask(tool.getItem());
        int code = m.code();
        int t = type(code);
        int tr = tier(code);
        if (isEnchCode(code)) {
            Enchantment e = keyOf(m.e());
            if (e != null) want.merge(e, m.l(), Math::max);
            return;
        }
        switch (t) {
            case EFFICIENCY -> {
                if ((mask & P) != 0) want.merge(Enchantments.BLOCK_EFFICIENCY, tr, Integer::sum);
            }
            case FORTUNE -> {
                if ((mask & P) != 0) want.merge(Enchantments.BLOCK_FORTUNE, tr, Integer::sum);
            }
            case DAMAGE -> {
                if ((mask & (M | T)) != 0) want.merge(Enchantments.SHARPNESS, tr, Integer::sum);
                else if ((mask & B) != 0) want.merge(Enchantments.POWER_ARROWS, tr, Integer::sum);
            }
            case UNBREAK -> want.merge(Enchantments.UNBREAKING, tr, Integer::sum);
            case SPEED -> {
                if ((mask & X) != 0) want.merge(Enchantments.QUICK_CHARGE, tr, Integer::sum);
            }
            default -> {}
        }
    }

    private static int cap(Enchantment k) {
        if (k == Enchantments.BLOCK_EFFICIENCY) return 8;
        if (k == Enchantments.SHARPNESS) return 10;
        if (k == Enchantments.POWER_ARROWS) return 8;
        if (k == Enchantments.BLOCK_FORTUNE) return 5;
        if (k == Enchantments.UNBREAKING) return 5;
        if (k == Enchantments.QUICK_CHARGE) return 4;
        return 10;
    }

    private static boolean enchSupported(ItemStack tool, Enchantment h) {
        int mask = mask(tool.getItem());
        if ((mask & M) != 0 && h.canEnchant(new ItemStack(Items.NETHERITE_SWORD))) return true;
        if ((mask & M) != 0 && h.canEnchant(new ItemStack(Items.NETHERITE_AXE))) return true;
        if ((mask & P) != 0 && h.canEnchant(new ItemStack(Items.NETHERITE_PICKAXE))) return true;
        if ((mask & P) != 0 && h.canEnchant(new ItemStack(Items.NETHERITE_SHOVEL))) return true;
        if ((mask & B) != 0 && h.canEnchant(new ItemStack(Items.BOW))) return true;
        if ((mask & X) != 0 && h.canEnchant(new ItemStack(Items.CROSSBOW))) return true;
        if ((mask & T) != 0 && h.canEnchant(new ItemStack(Items.TRIDENT))) return true;
        // encantamentos que servem em qualquer item danificavel (Inquebravel, Remendo...)
        return h.canEnchant(new ItemStack(Items.NETHERITE_SWORD)) && h.canEnchant(new ItemStack(Items.BOW));
    }

    /** A ferramenta aceita este modulo (considerando os modulos que ja tem)? */
    public static boolean accepts(ItemStack tool, Mod m, Mod[] others, RegistryAccess ra) {
        if (m == null || m.none() || !isModTool(tool)) return false;
        int code = m.code();
        int mask = mask(tool.getItem());
        int t = type(code);
        if (isEnchCode(code)) {
            Enchantment opt = keyOf(m.e());
            if (opt == null || !enchSupported(tool, opt)) return false;
        } else {
            switch (t) {
                case SPEED, DAMAGE, FREEZE, VENOM, WITHER, THUNDER -> {
                    if ((mask & (M | B | X | T)) == 0) return false;
                }
                case EFFICIENCY, FORTUNE -> {
                    if ((mask & P) == 0) return false;
                }
                case UNBREAK -> {}
                default -> {
                    return false;
                }
            }
        }
        // incompatibilidades entre os encantamentos que os modulos dao
        Map<Enchantment, Integer> mine = new LinkedHashMap<>();
        enchantsOf(tool, m, mine);
        for (Mod o : others) {
            if (o == null || o.none()) continue;
            if (isEnchCode(code) && isEnchCode(o.code()) && o.e().equals(m.e())) return false;
            Map<Enchantment, Integer> theirs = new LinkedHashMap<>();
            enchantsOf(tool, o, theirs);
            for (Enchantment a : mine.keySet()) {
                for (Enchantment b : theirs.keySet()) {
                    if (a != b && !a.isCompatibleWith(b)) return false;
                }
            }
        }
        return true;
    }

    /** Identificador do bonus de velocidade de ataque do Modulo de Velocidade (aplicado em ToolEvents). */
    public static final java.util.UUID SPEED_UUID = java.util.UUID.fromString("8c1e2f4a-5b6d-4e7f-9a0b-1c2d3e4f5a6b");

    /** Grava os modulos na ferramenta e refaz encantamentos e velocidade de ataque que eles dao. */
    public static void setMods(ItemStack tool, Mod[] mods, RegistryAccess ra) {
        int[] ids = new int[SLOTS];
        net.minecraft.nbt.ListTag ls = new net.minecraft.nbt.ListTag();
        for (int k = 0; k < SLOTS; k++) {
            Mod m = k < mods.length && mods[k] != null ? mods[k] : Mod.NONE;
            ids[k] = m.code();
            ls.add(net.minecraft.nbt.StringTag.valueOf(isEnchCode(m.code()) ? m.e() + "|" + m.l() : ""));
        }
        tool.getOrCreateTag().putIntArray(KEY, ids);
        tool.getOrCreateTag().put(KEY_E, ls);
        Map<Enchantment, Integer> want = new LinkedHashMap<>();
        for (Mod m : mods) {
            if (m == null || m.none()) continue;
            enchantsOf(tool, m, want);
        }
        // a velocidade de ataque do Modulo de Velocidade e somada por ItemAttributeModifierEvent (ToolEvents)
        Map<Enchantment, Integer> fin = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> e : want.entrySet()) {
            fin.put(e.getKey(), Math.min(e.getValue(), Math.max(e.getKey().getMaxLevel(), cap(e.getKey()))));
        }
        EnchantmentHelper.setEnchantments(fin, tool);
    }

    /** Linhas para o tooltip: ranhuras usadas e o poder de cada tipo. */
    public static List<String> summary(ItemStack tool) {
        List<String> out = new ArrayList<>();
        int[] mods = getMods(tool);
        int used = 0;
        for (int m : mods) if (m > 0) used++;
        out.add("slots:" + used);
        for (int t = 1; t <= 9; t++) {
            int n = t >= FREEZE ? maxTier(tool, t) : power(tool, t);
            if (n > 0) out.add(t + ":" + n);
        }
        return out;
    }
}
