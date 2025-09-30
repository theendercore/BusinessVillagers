package net.marum.villagebusiness.init;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class VillageBusinessEvents {
    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> addToTab(CreativeModeTabs.FUNCTIONAL_BLOCKS, entries::accept));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> addToTab(CreativeModeTabs.INGREDIENTS, entries::accept));
    }

    public static void addToTab(ResourceKey<CreativeModeTab> tab, Consumer<ItemLike> c) {
        if (tab.equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            c.accept(VillagerBusinessBlocks.SALES_STAND_BLOCK);
            c.accept(VillagerBusinessBlocks.REQUEST_STAND_BLOCK);
        }
        if (tab.equals(CreativeModeTabs.INGREDIENTS)) {
            c.accept(VillagerBusinessItems.EMERALD_NUGGET);
        }
    }
}
