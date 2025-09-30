package net.marum.villagebusiness.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import static net.marum.villagebusiness.VillageBusiness.id;

public class VillagerBusinessItems {
    public static final Item EMERALD_NUGGET = register("emerald_nugget", new Item(new Item.Properties()));

    public static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, id(name), item);
    }

    public static void init() {
    }
}