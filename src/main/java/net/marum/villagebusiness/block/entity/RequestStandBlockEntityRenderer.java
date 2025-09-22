package net.marum.villagebusiness.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;

public class RequestStandBlockEntityRenderer implements BlockEntityRenderer<RequestStandBlockEntity>{

    private final BlockEntityRendererProvider.Context context;
    private final Quaternionf rotationMatrix = new Quaternionf(0.707f, 0f, 0f, 0.707f);
    private final Quaternionf slightRotationMatrix = new Quaternionf(0f, 0f, 0.0499792f, 0.9987503f);

    public RequestStandBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(RequestStandBlockEntity entity, float tickDelta, PoseStack matrices,
            MultiBufferSource vertexConsumers, int light, int overlay) {
        int lightAbove = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above());
        Level world = entity.getLevel();
        
        if (entity.getInputCount() > 0) {
            ItemStack product = entity.getItem(0);
            // Product
            int productCount = 1;
            
            matrices.pushPose();
            if (product.getItem() instanceof BlockItem) {
                productCount = 1;
                matrices.translate(0.5f, 1.2f, 0.5f);
                matrices.scale(0.75f, 0.75f, 0.75f);
            } else {
                productCount = Math.min(1+entity.getInputCount()/8, 5);
                matrices.translate(0.5f, 1.04f, 0.5f);
                matrices.scale(0.75f, 0.75f, 0.75f);
                matrices.mulPose(rotationMatrix); 
            }
            
            for (int i = 0; i < productCount; i++) {
                this.context.getItemRenderer().renderStatic(
                    product,
                    ItemDisplayContext.FIXED,
                    lightAbove,
                    overlay,
                    matrices,
                    vertexConsumers,
                    world,
                    0
                );
                matrices.translate(0f, 0f, -0.07f);
                matrices.mulPose(slightRotationMatrix);
            }
            matrices.popPose();
        }

        // Emeralds
        if (entity.getInputBlockCount() > 0) {
            ItemStack emeraldStack = entity.getItem(1);
            matrices.pushPose();
            matrices.translate(0.18f, 1.13f, 0.18f);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.mulPose(rotationMatrix);
            int emeraldPile = entity.getInputBlockCount();
            emeraldPile = Math.min(emeraldPile, 3);
            for (int i = 0; i < emeraldPile; i++) {
                this.context.getItemRenderer().renderStatic(
                    emeraldStack,
                    ItemDisplayContext.FIXED,
                    lightAbove,
                    overlay,
                    matrices,
                    vertexConsumers,
                    world,
                    0
                );
                matrices.translate(0f, 0f, -0.5f);
                matrices.mulPose(slightRotationMatrix);
            }
            matrices.popPose();
        } else if  (entity.getInputEmeraldCount() > 0) {
            ItemStack emeraldStack = entity.getItem(2);
            matrices.pushPose();
            matrices.translate(0.15f, 1.03f, 0.15f);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.mulPose(rotationMatrix);
            int emeraldPile = entity.getInputEmeraldCount();
            emeraldPile = Math.min(emeraldPile, 5);
            for (int i = 0; i < emeraldPile; i++) {
                this.context.getItemRenderer().renderStatic(
                    emeraldStack,
                    ItemDisplayContext.FIXED,
                    lightAbove,
                    overlay,
                    matrices,
                    vertexConsumers,
                    world,
                    0
                );
                matrices.translate(0f, 0f, -0.07f);
                matrices.mulPose(slightRotationMatrix);
            }
            matrices.popPose();
        } else if (entity.getInputNuggetCount() > 0) {
            ItemStack emeraldStack = entity.getItem(3);
            matrices.pushPose();
            matrices.translate(0.12f, 1.03f, 0.12f);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.mulPose(rotationMatrix);
            int emeraldPile = entity.getInputNuggetCount();
            emeraldPile = Math.min(emeraldPile, 5);
            for (int i = 0; i < emeraldPile; i++) {
                this.context.getItemRenderer().renderStatic(
                    emeraldStack,
                    ItemDisplayContext.FIXED,
                    lightAbove,
                    overlay,
                    matrices,
                    vertexConsumers,
                    world,
                    0
                );
                matrices.translate(0f, 0f, -0.07f);
                matrices.mulPose(slightRotationMatrix);
            }
            matrices.popPose();
        }
    }
}
