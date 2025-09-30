package net.marum.villagebusiness.util;

import net.marum.villagebusiness.init.VillagerBusinessItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NonEmeraldSlot extends Slot {
    public NonEmeraldSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        Item item = stack.getItem();
        return item != Items.EMERALD && item != Items.EMERALD_BLOCK && item != VillagerBusinessItems.EMERALD_NUGGET;
    }
}