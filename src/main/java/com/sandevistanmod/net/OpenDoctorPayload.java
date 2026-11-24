package com.sandevistanmod.net;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Servidor para o cliente: abre a tela do Ripperdoc (Loja / Implantes). */
public record OpenDoctorPayload() {
    public void encode(FriendlyByteBuf buf) {

    }

    public static OpenDoctorPayload decode(FriendlyByteBuf buf) {
        return new OpenDoctorPayload();
    }
}
