package com.sandevistanmod.net;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Dados do proprio jogador (implante, ligado, Mente) enviados ao cliente dele. */
public record SyncPayload(CompoundTag tag) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public static SyncPayload decode(FriendlyByteBuf buf) {
        return new SyncPayload(buf.readNbt());
    }
}
