package net.marum.villagebusiness;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.marum.villagebusiness.block.entity.RequestStandBlockEntityRenderer;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntityRenderer;
import net.marum.villagebusiness.init.VillageBusinessBlockEntityTypeInit;
import net.marum.villagebusiness.screen.RequestStandScreen;
import net.marum.villagebusiness.screen.SalesStandScreen;
import net.marum.villagebusiness.screen.VillageBusinessScreenHandlers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class VillageBusinessClient implements ClientModInitializer{

    @Override
    public void onInitializeClient() {
        MenuScreens.register(VillageBusinessScreenHandlers.SALES_STAND_SCREEN_HANDLER, SalesStandScreen::new);
        BlockEntityRendererRegistryImpl.register(VillageBusinessBlockEntityTypeInit.SALES_STAND_ENTITY, SalesStandBlockEntityRenderer::new);
        
        MenuScreens.register(VillageBusinessScreenHandlers.REQUEST_STAND_SCREEN_HANDLER, RequestStandScreen::new);
        BlockEntityRendererRegistryImpl.register(VillageBusinessBlockEntityTypeInit.REQUEST_STAND_ENTITY, RequestStandBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(VillageBusiness.MOD_ID, "sales_stand_update"), (client, handler, buf, responseSender) -> {
        
            BlockPos pos = buf.readBlockPos();
            CompoundTag nbt = buf.readNbt();

            client.execute(() -> {
                if (client.level != null) {
                    BlockEntity blockEntity = client.level.getBlockEntity(pos);
                    if (blockEntity != null) {
                        blockEntity.load(nbt);
                        VillageBusiness.LOGGER.info(nbt.getAsString());
                    }
                }
            });
        });
    }
}
