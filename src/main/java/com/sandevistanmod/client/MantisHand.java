package com.sandevistanmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sandevistanmod.SandevistanMod;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderArmEvent;

/**
 * Primeira pessoa: o jogo so desenha o braco quando a mao esta vazia. Com a Lamina Mantis nesse braco, a mao vazia
 * ganha o mecanismo e a lamina (a "mao de Mantis").
 */
@Mod.EventBusSubscriber(modid = SandevistanMod.MODID, value = Dist.CLIENT)
public final class MantisHand {
    private MantisHand() {}

    @SubscribeEvent
    public static void arm(RenderArmEvent e) {
        AbstractClientPlayer p = e.getPlayer();
        boolean right = e.getArm() == HumanoidArm.RIGHT;
        if (!ClientState.bit(p.getUUID(), right ? 25 : 26)) return;
        PoseStack ps = e.getPoseStack();
        ps.pushPose();
        // mesma origem do braco no modelo do jogador
        ps.translate((right ? -5f : 5f) / 16f, 2f / 16f, 0f);
        GearLayer.mantisModel(ps, e.getMultiBufferSource(), e.getPackedLight(), right,
                "slim".equals(p.getModelName()));
        ps.popPose();
    }
}
