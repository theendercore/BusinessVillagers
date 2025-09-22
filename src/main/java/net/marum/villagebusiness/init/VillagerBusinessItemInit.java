package net.marum.villagebusiness.init;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.marum.villagebusiness.VillageBusiness;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class VillagerBusinessItemInit {
    public static final Item EMERALD_NUGGET = register("emerald_nugget", new Item(new FabricItemSettings()));

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.accept(EMERALD_NUGGET);
    }

    public static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, VillageBusiness.id(name), item);
    }

    public static void load() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(VillagerBusinessItemInit::addItemsToIngredientItemGroup);
    }
}