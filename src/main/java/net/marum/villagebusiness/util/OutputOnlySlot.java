package net.marum.villagebusiness.util;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class OutputOnlySlot extends Slot {
    private final Item allowedItem;

    public OutputOnlySlot(Container inventory, int index, int x, int y, Item allowedItem) {
        super(inventory, index, x, y);
        this.allowedItem = allowedItem;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() == allowedItem;
    }
}