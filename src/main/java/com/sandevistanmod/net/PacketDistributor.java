package com.sandevistanmod.net;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

/** Mesma assinatura que o PacketDistributor do NeoForge, em cima do SimpleChannel do Forge 1.20.1. */
public final class PacketDistributor {
    private PacketDistributor() {}

    public static void sendToServer(Object msg) {
        Net.CHANNEL.sendToServer(msg);
    }

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAllPlayers(Object msg) {
        Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), msg);
    }
}
