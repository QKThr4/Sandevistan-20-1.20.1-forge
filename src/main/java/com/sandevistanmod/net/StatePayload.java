package com.sandevistanmod.net;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Estado visivel de um jogador para todos: bit 1 tem o implante nas costas, bit 2 esta com o Sandevistan ligado. */
public record StatePayload(UUID uuid, int code) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeVarInt(code);
    }

    public static StatePayload decode(FriendlyByteBuf buf) {
        return new StatePayload(buf.readUUID(), buf.readVarInt());
    }
}
