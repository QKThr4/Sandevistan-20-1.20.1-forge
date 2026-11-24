package com.sandevistanmod.net;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Cliente para o servidor: 1 implantar, 2 remover implante, 3 comprar Estabilizador, 4 comprar Imunossupressor. */
public record DoctorActionPayload(int action) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(action);
    }

    public static DoctorActionPayload decode(FriendlyByteBuf buf) {
        return new DoctorActionPayload(buf.readVarInt());
    }
}
