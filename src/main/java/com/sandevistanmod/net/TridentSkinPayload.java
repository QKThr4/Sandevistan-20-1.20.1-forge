package com.sandevistanmod.net;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Servidor para o cliente: o tridente arremessado (uuid) e de Aco (1) ou Ciber (2) e deve usar a textura propria. */
public record TridentSkinPayload(UUID uuid, int kind) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeVarInt(kind);
    }

    public static TridentSkinPayload decode(FriendlyByteBuf buf) {
        return new TridentSkinPayload(buf.readUUID(), buf.readVarInt());
    }
}
