package com.sandevistanmod.net;

import net.minecraft.network.FriendlyByteBuf;

/** Taxa de ticks do servidor, para o cliente desacelerar junto (a 1.20.1 nao tem o /tick rate). */
public record TickRatePayload(float rate) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(rate);
    }

    public static TickRatePayload decode(FriendlyByteBuf buf) {
        return new TickRatePayload(buf.readFloat());
    }
}
