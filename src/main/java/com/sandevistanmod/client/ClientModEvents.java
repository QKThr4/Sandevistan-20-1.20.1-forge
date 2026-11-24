package com.sandevistanmod.client;

import com.sandevistanmod.SandevistanMod;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

@Mod.EventBusSubscriber(modid = SandevistanMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(ModKeys.SCREEN);
        e.register(ModKeys.TOGGLE);
        e.register(ModKeys.EYE);
        e.register(ModKeys.XRAY);
        e.register(ModKeys.SELECT);
        e.register(ModKeys.USE);
    }

    @SubscribeEvent
    public static void guiLayers(RegisterGuiOverlaysEvent e) {
        e.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "vision", (gui, gg, pt, w, h) -> ClientOverlay.renderVision(gg, pt));
        e.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "psychosis", (gui, gg, pt, w, h) -> PsychosisFx.renderTint(gg, pt));
        e.registerAboveAll("hud", (gui, gg, pt, w, h) -> ClientOverlay.renderHud(gg, pt));
        e.registerAboveAll("intro_video", (gui, gg, pt, w, h) -> IntroVideo.render(gg, pt));
        e.registerAboveAll("low_mind", (gui, gg, pt, w, h) -> LowMindFx.render(gg, pt));
        e.registerAboveAll("abilities", (gui, gg, pt, w, h) -> AbilityWheel.render(gg, pt));
        e.registerAboveAll("psychosis_hud", (gui, gg, pt, w, h) -> PsychosisFx.renderHud(gg, pt));
    }

    @SubscribeEvent
    public static void clientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(com.sandevistanmod.registry.ModRegistries.BENCH_MENU.get(), com.sandevistanmod.bench.BenchScreen::new);
            net.minecraft.client.gui.screens.MenuScreens.register(com.sandevistanmod.registry.ModRegistries.ENERGY_MENU.get(), com.sandevistanmod.energy.EnergyScreen::new);
            net.minecraft.client.gui.screens.MenuScreens.register(com.sandevistanmod.registry.ModRegistries.UPGRADE_MENU.get(), UpgradeScreen::new);
        });
    }

    /** Coloca o implante nas costas dos dois modelos de jogador (wide e slim). */
    @SubscribeEvent
    public static void playerLayers(EntityRenderersEvent.AddLayers e) {
        for (String skin : e.getSkins()) {
            var renderer = e.getSkin(skin);
            if (renderer instanceof PlayerRenderer pr) {
                pr.addLayer(new BackLayer(pr));
                pr.addLayer(new BodyLayer(pr));
                pr.addLayer(new GearLayer(pr));
                PlayerGlitch.installTremor(pr, e.getEntityModels().bakeLayer(
                        skin.equals("slim")
                                ? net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM
                                : net.minecraft.client.model.geom.ModelLayers.PLAYER),
                        skin.equals("slim"));
            }
        }
    }
}
