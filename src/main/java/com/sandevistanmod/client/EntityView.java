package com.sandevistanmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

/** Boneco do jogador que olha para o mouse (a versao 1.20.1 do metodo do inventario recebe so o ponto dos pes). */
public final class EntityView {
    private EntityView() {}

    public static void draw(GuiGraphics gg, int x1, int y1, int x2, int y2, int scale, int mouseX, int mouseY, LivingEntity e) {
        int x = (x1 + x2) / 2;
        int y = y2 - 3;
        InventoryScreen.renderEntityInInventoryFollowsMouse(gg, x, y, scale, (float) x - mouseX, (float) (y - 50) - mouseY, e);
    }
}
