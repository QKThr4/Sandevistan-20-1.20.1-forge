package com.sandevistanmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Jetpack e Lamina Mantis como item: o mesmo modelo 3D que aparece no corpo (nao um icone 2D). */
public final class ItemMeshRenderer extends BlockEntityWithoutLevelRenderer {
    private static ItemMeshRenderer instance;

    private ItemMeshRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack ps, MultiBufferSource buffer, int light, int overlay) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        boolean jet = path.startsWith("jetpack");
        ps.pushPose();
        // o item chega com a origem no canto do cubo [0,1]: centraliza, e vira o modelo (no corpo o eixo y aponta para baixo)
        ps.translate(0.5f, 0.5f, 0.5f);
        ps.mulPose(Axis.ZP.rotationDegrees(180f));
        if (jet) {
            ps.scale(1.0f, 1.0f, 1.0f);
            ps.translate(0f, -6.5f / 16f, -3.4f / 16f);
            MeshModel.get("jetpack").render(ps, buffer, light);
        } else {
            ps.scale(0.8f, 0.8f, 0.8f);
            ps.translate(4.35f / 16f, -11.45f / 16f, 0f);
            MeshModel.get("mantis_r").render(ps, buffer, light);
        }
        ps.popPose();
    }

    /** Extensao de cliente usada pelo ExtItem do jetpack e da Mantis (chamada em Item.initializeClient). */
    public static IClientItemExtensions extension() {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (instance == null) instance = new ItemMeshRenderer();
                return instance;
            }
        };
    }
}
