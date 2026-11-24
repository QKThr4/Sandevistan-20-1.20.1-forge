package com.sandevistanmod.client;

import com.sandevistanmod.SandevistanMod;
import com.sandevistanmod.data.SandevistanData;
import com.sandevistanmod.net.ActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import com.sandevistanmod.net.PacketDistributor;

@Mod.EventBusSubscriber(modid = SandevistanMod.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {}

    private static boolean wasActive;
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            wasActive = false;
            return;
        }
        softenHurtTilt(mc);
        PlayerGlitch.tickLocal();
        // 1.20.1: o cliente roda a 20 ticks (so o servidor desacelera), entao nao ha compensacao de movimento aqui
        boolean on = ClientState.active(mc.player.getUUID());
        wasActive = on;
        while (ModKeys.SCREEN.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new SandevistanScreen());
        }
        while (ModKeys.TOGGLE.consumeClick()) {
            PacketDistributor.sendToServer(new ActionPayload(0));
        }
        while (ModKeys.EYE.consumeClick()) {
            if (mc.screen == null && ClientData.DATA.eye() > 1) PacketDistributor.sendToServer(new ActionPayload(21));
        }
        while (ModKeys.XRAY.consumeClick()) {
            if (mc.screen == null && ClientData.DATA.extTier(SandevistanData.EXT_XRAY) != 0) PacketDistributor.sendToServer(new ActionPayload(20));
        }
    }

    // ---- acima de 100% de ciberpsicose: sem usar itens nem abrir inventarios (so o menu) ----
    private static boolean locked() {
        var d = ClientData.DATA;
        return d.psychotic && d.psy >= 100f;
    }

    @SubscribeEvent
    public static void lockScreen(ScreenEvent.Opening e) {
        if (locked() && e.getNewScreen() instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void lockUseStart(LivingEntityUseItemEvent.Start e) {
        if (e.getEntity() == Minecraft.getInstance().player && locked()) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void lockRightClickItem(PlayerInteractEvent.RightClickItem e) {
        if (e.getEntity() == Minecraft.getInstance().player && locked()) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void lockRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (e.getEntity() == Minecraft.getInstance().player && locked()
                && !ClientPath.isHandDoor(e.getLevel().getBlockState(e.getPos()))) {
            e.setCanceled(true);
        }
    }

    /** Com ciberpsicose (30% a 100%) a tela nao balanca ao tomar dano: some aos poucos ate nao balancar nada. */
    private static void softenHurtTilt(Minecraft mc) {
        var d = ClientData.DATA;
        if (!d.psychotic || d.psy < 30f) return;
        float f = Math.min(1f, (d.psy - 30f) / 70f);
        int cap = Math.round(mc.player.hurtDuration * (1f - f));
        if (mc.player.hurtTime > cap) mc.player.hurtTime = cap;
    }

    /**
     * Com a velocidade absurda o campo de visao abriria ao maximo o tempo todo: com o Sandevistan ligado
     * ele fica num valor fixo e suave.
     */
    @SubscribeEvent
    public static void fov(ComputeFovModifierEvent e) {
        if (ClientState.active(e.getPlayer().getUUID())) {
            e.setNewFovModifier(1.12f);
        }
    }

    /** Com a ciberpsicose a camera treme. */
    @SubscribeEvent
    public static void cameraShake(ViewportEvent.ComputeCameraAngles e) {
        if (!PsychosisFx.shaking()) return;
        float psy = PsychosisFx.psy();
        e.setRoll(e.getRoll() + PsychosisFx.shake(psy, 90.0) * 0.8f);
        e.setPitch(e.getPitch() + PsychosisFx.shake(psy, 57.0) * 0.4f);
        e.setYaw(e.getYaw() + PsychosisFx.shake(psy, 73.0) * 0.4f);
    }

    /** Ao sair do mundo, limpa o estado do cliente para nao vazar para o proximo mundo. */
    @SubscribeEvent
    public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientState.clear();
        Afterimages.clear();
        ClientSounds.clear();
        FlightSounds.clear();
        IntroVideo.stop();
        PlayerGlitch.clear();
        ClientData.DATA.deserializeNBT(new CompoundTag());
    }
}
