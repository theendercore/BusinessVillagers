package net.marum.villagebusiness.init;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.marum.villagebusiness.network.PosOpeningData;
import net.marum.villagebusiness.screen.RequestStandScreenHandler;
import net.marum.villagebusiness.screen.SalesStandScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static net.marum.villagebusiness.VillageBusiness.id;

public class VillageBusinessScreenHandlers {
    public static final ExtendedScreenHandlerType<SalesStandScreenHandler, PosOpeningData> SALES_STAND_SCREEN_HANDLER =
            register("sales_stand", SalesStandScreenHandler::new, PosOpeningData.CODEC);
    public static final ExtendedScreenHandlerType<RequestStandScreenHandler, PosOpeningData> REQUEST_STAND_SCREEN_HANDLER =
            register("request_stand", RequestStandScreenHandler::new, PosOpeningData.CODEC);


    public static <T extends AbstractContainerMenu, D> ExtendedScreenHandlerType<T, D> register(String name, ExtendedScreenHandlerType.ExtendedFactory<T, D> menu, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        return Registry.register(BuiltInRegistries.MENU, id(name), new ExtendedScreenHandlerType<>(menu, codec));
    }

    public static void init() {
    }
}
