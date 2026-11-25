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
            msField.setFloat(timerField.get(mc), 1000f / rate);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            failed = true;
            SandevistanMod.LOGGER_WARN("Nao foi possivel desacelerar o relogio do cliente: " + ex);
        }
    }
}
