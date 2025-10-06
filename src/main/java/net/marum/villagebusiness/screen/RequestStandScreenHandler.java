package net.marum.villagebusiness.screen;

import net.marum.villagebusiness.block.entity.RequestStandBlockEntity;
import net.marum.villagebusiness.init.VillageBusinessScreenHandlers;
import net.marum.villagebusiness.init.VillagerBusinessItems;
import net.marum.villagebusiness.network.PosOpeningData;
import net.marum.villagebusiness.util.NonEmeraldSlot;
import net.marum.villagebusiness.util.OutputOnlySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class RequestStandScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    public final RequestStandBlockEntity blockEntity;

    public RequestStandScreenHandler(int syncId, Inventory inventory, FriendlyByteBuf buf) {
        this(syncId, inventory, PosOpeningData.get(buf).pos());
    }

    public RequestStandScreenHandler(int syncId, Inventory inventory, BlockPos pos) {
        //noinspection resource
        this(syncId, inventory, inventory.player.level().getBlockEntity(pos));
    }

    public RequestStandScreenHandler(int syncId, Inventory playerInventory, BlockEntity blockEntity) {
        super(VillageBusinessScreenHandlers.REQUEST_STAND_SCREEN_HANDLER.get(), syncId);
        this.inventory = (Container) blockEntity;
        inventory.startOpen(playerInventory.player);
        this.blockEntity = (RequestStandBlockEntity) blockEntity;

        this.addSlot(new NonEmeraldSlot(inventory, 0, 145, 21));
        this.addSlot(new OutputOnlySlot(inventory, 3, 15, 21, VillagerBusinessItems.EMERALD_NUGGET.get()));
        this.addSlot(new OutputOnlySlot(inventory, 2, 33, 21, Items.EMERALD));
        this.addSlot(new OutputOnlySlot(inventory, 1, 51, 21, Items.EMERALD_BLOCK));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    public ItemStack getFilterItem() {
        return blockEntity.getFilterItem();
    }

    public void setFilterItem(ItemStack filter) {
        blockEntity.setFilterItem(filter);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot >= this.inventory.getContainerSize()) {
                if (newStack.getItem() != Items.EMERALD && newStack.getItem() != Items.EMERALD_BLOCK && newStack.getItem() != VillagerBusinessItems.EMERALD_NUGGET.get()) {
                    blockEntity.sendRequestToServer(newStack.copy());
                    return ItemStack.EMPTY;
                }
            }

            if (invSlot < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.inventory.stillValid(player);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.getContainerSlot() == 0;
    }

    @Override
    public boolean canTakeItemForPickAll(@NotNull ItemStack stack, Slot slot) {
        return slot.getContainerSlot() == 0;
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
