package com.sandevistanmod.compat;

import com.sandevistanmod.SandevistanMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A 1.20.1 nao tem o "/tick rate" da 1.20.3+. Aqui o servidor e desacelerado por codigo:
 * no fim de cada tick ele espera ate completar 1000/rate ms (processando os pacotes enquanto espera, como o proprio jogo faz).
 */
@Mod.EventBusSubscriber(modid = SandevistanMod.MODID)
public final class TickRate {
    private TickRate() {}

    private static volatile float rate = 20f;
    private static long target;

    public static float get() {
        return rate;
    }

    public static void set(float r) {
        rate = Math.max(1f, Math.min(20f, r));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            if (rate >= 20f) target = 0;
            else if (target == 0) target = System.currentTimeMillis();
            return;
        }
        if (rate >= 20f || target == 0) return;
        target += (long) (1000f / rate);
        long now = System.currentTimeMillis();
        if (target < now - 1000L) target = now; // nao tenta "recuperar" ticks perdidos
        final long until = target;
        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) server.managedBlock(() -> System.currentTimeMillis() >= until || !server.isRunning());
    }
}
