package net.marum.villagebusiness.init;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.marum.villagebusiness.VillageBusiness.MOD_ID;

public class VillagerBusinessItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredItem<Item> EMERALD_NUGGET = register("emerald_nugget", () -> new Item(new Item.Properties()));

    public static <T extends Item> DeferredItem<T> register(String name, Supplier<T> item) {
        return ITEMS.register(name, item);

    }

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }
}