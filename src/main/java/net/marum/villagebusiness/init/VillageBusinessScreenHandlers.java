package net.marum.villagebusiness.init;

import net.marum.villagebusiness.screen.RequestStandScreenHandler;
import net.marum.villagebusiness.screen.SalesStandScreenHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.marum.villagebusiness.VillageBusiness.MOD_ID;

public class VillageBusinessScreenHandlers {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<SalesStandScreenHandler>> SALES_STAND_SCREEN_HANDLER =
            register("sales_stand", SalesStandScreenHandler::new);
    public static final DeferredHolder<MenuType<?>, MenuType<RequestStandScreenHandler>> REQUEST_STAND_SCREEN_HANDLER =
            register("request_stand", RequestStandScreenHandler::new);


    public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> register(String name, IContainerFactory<T> menu) {
        return MENU_TYPES.register(name, () -> IMenuTypeExtension.create(menu));
    }

    public static void init(IEventBus bus) {
        MENU_TYPES.register(bus);
    }
}
