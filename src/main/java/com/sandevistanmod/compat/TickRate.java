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

    // ---- relogio interno do servidor (MinecraftServer.nextTickTime) ----
    // O laco do servidor soma 50 ms por tick. Se um tick demora 500 ms, ele acha que esta atrasado e, ao voltar ao normal,
    // roda varios ticks seguidos para "recuperar" (o mundo acelera por um tempo). Por isso, depois de esperar, atualizamos esse relogio.
    private static java.lang.reflect.Field clock;
    private static boolean clockSearched;
    private static int attempts;
    private static java.util.Map<java.lang.reflect.Field, long[]> samples = new java.util.HashMap<>();

    /** Acha o campo nextTickTime: e o unico campo long do servidor que cresce exatamente 50 a cada tick. */
    private static void sampleClock(net.minecraft.server.MinecraftServer server) {
        if (clock != null || clockSearched) return;
        try {
            if (samples.isEmpty()) {
                for (java.lang.reflect.Field f : net.minecraft.server.MinecraftServer.class.getDeclaredFields()) {
                    if (f.getType() != long.class || java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                    f.setAccessible(true);
                    samples.put(f, new long[] {f.getLong(server), 0});
                }
                return;
            }
            java.util.Iterator<java.util.Map.Entry<java.lang.reflect.Field, long[]>> it = samples.entrySet().iterator();
            while (it.hasNext()) {
                var en = it.next();
                long v = en.getKey().getLong(server);
                long[] st = en.getValue();
                if (v - st[0] != 50L) it.remove();
                else {
                    st[0] = v;
                    st[1]++;
                }
            }
            for (var en : samples.entrySet()) {
                if (en.getValue()[1] >= 30) {
                    clock = en.getKey();
                    clockSearched = true;
                    samples.clear();
                    return;
                }
            }
            if (samples.isEmpty() && ++attempts > 40) clockSearched = true; // nao achou: segue sem o ajuste
        } catch (ReflectiveOperationException | RuntimeException ex) {
            clockSearched = true;
            SandevistanMod.LOGGER_WARN("Nao foi possivel achar o relogio do servidor: " + ex);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent e) {
        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        if (e.phase == TickEvent.Phase.START) {
            if (rate >= 20f) {
                target = 0;
                sampleClock(server);
            } else {
                samples.clear();
                if (target == 0) target = System.currentTimeMillis();
            }
            return;
        }
        if (rate >= 20f || target == 0) {
            // ticks normais: o relogio fica como o jogo deixou
            return;
        }
        target += (long) (1000f / rate);
        long now = System.currentTimeMillis();
        if (target < now - 1000L) target = now; // nao tenta "recuperar" ticks perdidos
        final long until = target;
        server.managedBlock(() -> System.currentTimeMillis() >= until || !server.isRunning());
        // diz ao laco do servidor que nao ha atraso a compensar
        if (clock != null) {
            try {
                clock.setLong(server, net.minecraft.Util.getMillis());
            } catch (ReflectiveOperationException | RuntimeException ex) {
                clock = null;
            }
        }
    }
}
