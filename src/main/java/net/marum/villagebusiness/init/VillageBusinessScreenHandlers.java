package net.marum.villagebusiness.init;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.marum.villagebusiness.screen.RequestStandScreenHandler;
import net.marum.villagebusiness.screen.SalesStandScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static net.marum.villagebusiness.VillageBusiness.id;

public class VillageBusinessScreenHandlers {
    public static final ExtendedScreenHandlerType<SalesStandScreenHandler> SALES_STAND_SCREEN_HANDLER =
            register("sales_stand", SalesStandScreenHandler::new);
    public static final ExtendedScreenHandlerType<RequestStandScreenHandler> REQUEST_STAND_SCREEN_HANDLER =
            register("request_stand", RequestStandScreenHandler::new);


    public static <T extends AbstractContainerMenu> ExtendedScreenHandlerType<T> register(String name, ExtendedScreenHandlerType.ExtendedFactory<T> item) {
        return Registry.register(BuiltInRegistries.MENU, id(name), new ExtendedScreenHandlerType<>(item));
    }

    public static void init() { }
}
