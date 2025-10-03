package net.marum.villagebusiness;

import net.fabricmc.api.ClientModInitializer;
import net.marum.villagebusiness.block.entity.RequestStandBlockEntityRenderer;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntityRenderer;
import net.marum.villagebusiness.init.VillageBusinessBlockEntityTypeInit;
import net.marum.villagebusiness.init.VillageBusinessScreenHandlers;
import net.marum.villagebusiness.screen.RequestStandScreen;
import net.marum.villagebusiness.screen.SalesStandScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class VillageBusinessClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Screens
        MenuScreens.register(VillageBusinessScreenHandlers.SALES_STAND_SCREEN_HANDLER, SalesStandScreen::new);
        MenuScreens.register(VillageBusinessScreenHandlers.REQUEST_STAND_SCREEN_HANDLER, RequestStandScreen::new);

        // BE renderers
        BlockEntityRenderers.register(VillageBusinessBlockEntityTypeInit.SALES_STAND_ENTITY, SalesStandBlockEntityRenderer::new);
        BlockEntityRenderers.register(VillageBusinessBlockEntityTypeInit.REQUEST_STAND_ENTITY, RequestStandBlockEntityRenderer::new);
    }
}
