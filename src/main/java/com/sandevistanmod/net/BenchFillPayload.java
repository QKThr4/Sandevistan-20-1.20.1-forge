package com.sandevistanmod.net;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Cliente para o servidor: preencher a bancada aberta com a receita de indice 'index' (usando itens do inventario). */
public record BenchFillPayload(int index) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
    }

    public static BenchFillPayload decode(FriendlyByteBuf buf) {
        return new BenchFillPayload(buf.readVarInt());
    }
}
