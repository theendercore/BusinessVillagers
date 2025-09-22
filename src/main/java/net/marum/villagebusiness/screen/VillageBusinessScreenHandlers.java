package net.marum.villagebusiness.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.marum.villagebusiness.VillageBusiness;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public class VillageBusinessScreenHandlers {
    public static final MenuType<SalesStandScreenHandler> SALES_STAND_SCREEN_HANDLER =
        Registry.register(BuiltInRegistries.MENU, VillageBusiness.id("sales_stand"),
        new ExtendedScreenHandlerType<>(SalesStandScreenHandler::new));
    public static final MenuType<RequestStandScreenHandler> REQUEST_STAND_SCREEN_HANDLER =
        Registry.register(BuiltInRegistries.MENU, VillageBusiness.id("request_stand"),
        new ExtendedScreenHandlerType<>(RequestStandScreenHandler::new));
    
    public static void load() {}
}
