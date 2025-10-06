package net.marum.villagebusiness;

import net.marum.villagebusiness.block.entity.RequestStandBlockEntityRenderer;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntityRenderer;
import net.marum.villagebusiness.init.VillageBusinessBlockEntityTypeInit;
import net.marum.villagebusiness.init.VillageBusinessScreenHandlers;
import net.marum.villagebusiness.screen.RequestStandScreen;
import net.marum.villagebusiness.screen.SalesStandScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static net.marum.villagebusiness.VillageBusiness.MOD_ID;


@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class VillageBusinessClient {
    public VillageBusinessClient(ModContainer mod) {
        var bus = mod.getEventBus();
        if (bus != null) {
            bus.addListener(VillageBusinessClient::menuScreens);
            bus.addListener(VillageBusinessClient::renderer);
        }
    }

    public static void menuScreens(RegisterMenuScreensEvent event) {
        event.register(VillageBusinessScreenHandlers.SALES_STAND_SCREEN_HANDLER.get(), SalesStandScreen::new);
        event.register(VillageBusinessScreenHandlers.REQUEST_STAND_SCREEN_HANDLER.get(), RequestStandScreen::new);
    }

    public static void renderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(VillageBusinessBlockEntityTypeInit.SALES_STAND_ENTITY.get(), SalesStandBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(VillageBusinessBlockEntityTypeInit.REQUEST_STAND_ENTITY.get(), RequestStandBlockEntityRenderer::new);
    }
}
