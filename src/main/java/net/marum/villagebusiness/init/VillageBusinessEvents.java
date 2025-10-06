package net.marum.villagebusiness.init;

import net.marum.villagebusiness.VillageBusiness;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class VillageBusinessEvents {
    public static void init(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> VillageBusiness.SERVER = event.getServer());
        bus.addListener(VillageBusinessEvents::addToTab);
    }

    public static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.insertAfter(Items.BELL.getDefaultInstance(),
                    VillagerBusinessBlocks.SALES_STAND_BLOCK.toStack(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(Items.BELL.getDefaultInstance(),
                    VillagerBusinessBlocks.REQUEST_STAND_BLOCK.toStack(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
        if (event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) {
            event.insertAfter(Items.GOLD_NUGGET.getDefaultInstance(), VillagerBusinessItems.EMERALD_NUGGET.toStack(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
