package com.sandevistanmod.net;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Servidor para o cliente: toca o video da primeira vez que o jogador usa o Sandevistan depois de implantar. */
public record IntroPayload() {
    public void encode(FriendlyByteBuf buf) {

    }

    public static IntroPayload decode(FriendlyByteBuf buf) {
        return new IntroPayload();
    }
}
