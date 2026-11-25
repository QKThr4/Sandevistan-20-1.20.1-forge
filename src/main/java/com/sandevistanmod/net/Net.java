package com.sandevistanmod.net;

import com.sandevistanmod.SandevistanMod;
import com.sandevistanmod.client.ClientState;
import com.sandevistanmod.data.SandevistanData;
import com.sandevistanmod.logic.SandevistanLogic;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Canal de rede do mod (SimpleChannel do Forge 1.20.1). */
public final class Net {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SandevistanMod.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private Net() {}

    private static void toClient(Supplier<NetworkEvent.Context> sup, Runnable r) {
        NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> r));
        ctx.setPacketHandled(true);
    }

    private static void toServer(Supplier<NetworkEvent.Context> sup, java.util.function.Consumer<ServerPlayer> r) {
        NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp != null) r.accept(sp);
        });
        ctx.setPacketHandled(true);
    }

    /** Chamado uma vez na inicializacao comum (FMLCommonSetupEvent). */
    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncPayload.class, SyncPayload::encode, SyncPayload::decode,
                (m, ctx) -> toClient(ctx, () -> ClientNet.handleSync(m)));
        CHANNEL.registerMessage(id++, StatePayload.class, StatePayload::encode, StatePayload::decode,
                (m, ctx) -> toClient(ctx, () -> ClientState.set(m.uuid(), m.code())));
        CHANNEL.registerMessage(id++, TickRatePayload.class, TickRatePayload::encode, TickRatePayload::decode,
                (m, ctx) -> toClient(ctx, () -> com.sandevistanmod.client.ClientTickRate.apply(m.rate())));
        CHANNEL.registerMessage(id++, OpenDoctorPayload.class, OpenDoctorPayload::encode, OpenDoctorPayload::decode,
                (m, ctx) -> toClient(ctx, ClientNet::openDoctor));
        CHANNEL.registerMessage(id++, IntroPayload.class, IntroPayload::encode, IntroPayload::decode,
                (m, ctx) -> toClient(ctx, com.sandevistanmod.client.IntroVideo::start));
        CHANNEL.registerMessage(id++, TridentSkinPayload.class, TridentSkinPayload::encode, TridentSkinPayload::decode,
                (m, ctx) -> toClient(ctx, () -> com.sandevistanmod.client.TridentSkins.KIND.put(m.uuid(), m.kind())));
        CHANNEL.registerMessage(id++, DoctorActionPayload.class, DoctorActionPayload::encode, DoctorActionPayload::decode,
                (m, ctx) -> toServer(ctx, sp -> SandevistanLogic.doctorAction(sp, m.action())));
        CHANNEL.registerMessage(id++, BenchFillPayload.class, BenchFillPayload::encode, BenchFillPayload::decode,
                (m, ctx) -> toServer(ctx, sp -> {
                    if (sp.containerMenu instanceof com.sandevistanmod.bench.BenchMenu bm) {
                        bm.fill(sp, m.index());
                    }
                }));
        CHANNEL.registerMessage(id++, ActionPayload.class, ActionPayload::encode, ActionPayload::decode,
                (m, ctx) -> toServer(ctx, sp -> SandevistanLogic.handleAction(sp, m.action())));
    }

    public static void sync(ServerPlayer sp) {
        SandevistanData d = SandevistanData.get(sp);
        PacketDistributor.sendToPlayer(sp, new SyncPayload(d.serializeNBT()));
    }
}
