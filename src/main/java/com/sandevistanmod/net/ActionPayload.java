package com.sandevistanmod.net;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** 0 liga/desliga o Sandevistan, 1 aviso do doutor, 2/3 tira chip, 4/5 visibilidade, 6 tira a bateria. */
public record ActionPayload(int action) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(action);
    }

    public static ActionPayload decode(FriendlyByteBuf buf) {
        return new ActionPayload(buf.readVarInt());
    }
}
