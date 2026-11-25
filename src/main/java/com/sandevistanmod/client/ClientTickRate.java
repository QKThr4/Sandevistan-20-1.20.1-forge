package com.sandevistanmod.client;

import com.sandevistanmod.SandevistanMod;
import com.sandevistanmod.compat.TickRate;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;

/** Desacelera o relogio do cliente (Minecraft.timer.msPerTick) junto com o servidor: movimento suave em camera lenta. */
public final class ClientTickRate {
    private ClientTickRate() {}

    private static Field timerField;
    private static Field msField;
    private static boolean failed;
    private static boolean viaUnsafe;

    public static void apply(float rate) {
        rate = Math.max(1f, Math.min(20f, rate));
        TickRate.set(rate);
        if (failed) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (timerField == null) {
                for (Field f : Minecraft.class.getDeclaredFields()) {
                    if (f.getType() == Timer.class && !Modifier.isStatic(f.getModifiers())) {
                        f.setAccessible(true);
                        timerField = f;
                        break;
                    }
                }
            }
            if (msField == null) {
                for (Field f : Timer.class.getDeclaredFields()) {
                    if (f.getType() == float.class && Modifier.isFinal(f.getModifiers()) && !Modifier.isStatic(f.getModifiers())) {
                        f.setAccessible(true);
                        msField = f;
                        break;
                    }
                }
            }
            Object timer = timerField.get(mc);
            float ms = 1000f / rate;
            try {
                msField.setFloat(timer, ms);
            } catch (IllegalAccessException | RuntimeException ex) {
                // segunda via: escrita direta (o campo e final)
                Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                uf.setAccessible(true);
                sun.misc.Unsafe u = (sun.misc.Unsafe) uf.get(null);
                u.putFloat(timer, u.objectFieldOffset(msField), ms);
                if (!viaUnsafe) {
                    viaUnsafe = true;
                    SandevistanMod.LOGGER_WARN("Relogio do cliente ajustado pela via alternativa.");
                }
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            failed = true;
            SandevistanMod.LOGGER_WARN("Nao foi possivel desacelerar o relogio do cliente: " + ex);
        }
    }
}
