package net.marum.villagebusiness.block.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.marum.villagebusiness.init.VillageBusinessBlockEntityTypeInit;
import net.marum.villagebusiness.init.VillagerBusinessItemInit;
import net.marum.villagebusiness.network.VillageBusinessNetworking;
import net.marum.villagebusiness.pricing.ItemPrice;
import net.marum.villagebusiness.pricing.ItemPrices;
import net.marum.villagebusiness.screen.SalesStandScreenHandler;
import net.marum.villagebusiness.util.VillagerLure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SalesStandBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, SidedStorageBlockEntity {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    private static final int INPUT_SLOT = 3;
    private static final int OUTPUT_SLOT_NUGGETS = 2;
    private static final int OUTPUT_SLOT_EMERALDS = 1;
    private static final int OUTPUT_SLOT_BLOCKS = 0;

    private List<Villager> foundVillagers = new ArrayList<Villager>();
    private Set<VillagerLure> luringVillagers = new HashSet<VillagerLure>();
    private Set<VillagerLure> markedForRemovalVillagers = new HashSet<VillagerLure>();

    private static final int RADIUS = 50;
    private static final int SUCCESSFUL_PURCHASE_COOLDOWN = 1;
    private static final int REJECTED_PURCHASE_COOLDOWN = 1;
    private static final int ATTRACT_CHANCE = 5;
    private static final int LURED_BY_SALES_COOLDOWN = 1;

    private int ticks = 0;

    private int inputCount = 0;
    private int outputNuggetCount = 0;
    private int outputEmeraldCount = 0;
    private int outputBlockCount = 0;

    private ItemPrice itemPrice;
    private int priceSetting = 1;

    private int lastUpdatedInputCount = 0;
    private int lastUpdatedInputRawId = 0;
    private int lastUpdatedNuggetCount = 0;
    private int lastUpdatedEmeraldCount = 0;
    private int lastUpdatedBlockCount = 0;

    public SalesStandBlockEntity(BlockPos pos, BlockState state) {
        super(VillageBusinessBlockEntityTypeInit.SALES_STAND_ENTITY, pos, state);
    }

    public ItemPrice getItemPrice() {
        return itemPrice;
    }

    public int getPriceSetting() {
        return priceSetting;
    }

    public void sendPriceSettingToServer(int newValue) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(worldPosition);
        buf.writeInt(newValue);
        ClientPlayNetworking.send(VillageBusinessNetworking.PRICE_SETTING_PACKET, buf);

        priceSetting = newValue;
        updatePrices();
    }

    public void serverSetPriceSetting(int newValue) {
        priceSetting = newValue;
        updatePrices();
        updateListeners();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, SalesStandBlockEntity entity) {
        if (entity.level == null || entity.level.isClientSide())
            return;
        
        if (entity.ticks == 0) {
            entity.updatePrices();
            entity.setChanged();
        }

        // Lure villagers every 5 seconds
        if (entity.ticks % 100 == 0) {
            if (entity.canSell()) {
                entity.attractVillager();
            }
        }

        // Move lured villagers every 0.5 second
        if (entity.ticks % 10 == 0){
            entity.luringVillagers.forEach(lure -> {
                if (lure.hasExpired()) {
                    entity.getFrustrated(lure.villager);
                    entity.markedForRemovalVillagers.add(lure);
                } else {
                    if (entity.villagerIsBusy(lure.villager)) {
                        entity.markedForRemovalVillagers.add(lure);
                    } else {
                        entity.moveVillagerTowardBlock(lure.villager);
                        if (lure.villager.blockPosition().closerThan(pos, 3)) {
                            entity.evaluateSale(lure.villager);
                            entity.markedForRemovalVillagers.add(lure);
                        }
                    }
                }
            });
            entity.markedForRemovalVillagers.forEach(lure -> {
                entity.luringVillagers.remove(lure);
            });
            entity.markedForRemovalVillagers.clear();
            
            boolean inventoryChanged = false;
            if (entity.getItem(INPUT_SLOT).getCount() != entity.lastUpdatedInputCount) {
                entity.lastUpdatedInputCount = entity.getItem(INPUT_SLOT).getCount();
                inventoryChanged = true;
            }
            if (Item.getId(entity.getItem(INPUT_SLOT).getItem()) != entity.lastUpdatedInputRawId) {
                entity.lastUpdatedInputRawId = Item.getId(entity.getItem(INPUT_SLOT).getItem());
                inventoryChanged = true;
            }
            if (entity.getItem(OUTPUT_SLOT_NUGGETS).getCount() != entity.lastUpdatedBlockCount ||
            entity.getItem(OUTPUT_SLOT_EMERALDS).getCount() != entity.lastUpdatedEmeraldCount ||
            entity.getItem(OUTPUT_SLOT_BLOCKS).getCount() != entity.lastUpdatedNuggetCount) {
                inventoryChanged = true;
                entity.lastUpdatedNuggetCount = entity.getItem(OUTPUT_SLOT_NUGGETS).getCount();
                entity.lastUpdatedEmeraldCount = entity.getItem(OUTPUT_SLOT_EMERALDS).getCount();
                entity.lastUpdatedBlockCount = entity.getItem(OUTPUT_SLOT_BLOCKS).getCount();
            }
            if (inventoryChanged) {
                entity.updatePrices();
                entity.updateListeners();
            }
        }

        // Find nearby villagers every 20 seconds
        if (entity.ticks >= 400) {
            entity.foundVillagers = world.getEntitiesOfClass(Villager.class,
            new AABB(pos.offset(-RADIUS, -RADIUS, -RADIUS), pos.offset(RADIUS, RADIUS, RADIUS)), 
            villager -> true);
            //VillageBusiness.LOGGER.info("Found "+foundVillagers.size()+" villagers");
            entity.ticks = world.random.nextIntBetweenInclusive(-10, 10);
        }

        entity.ticks++;
    }

    private boolean villagerIsBusy(Villager villager) {
        Brain<Villager> brain = villager.getBrain();
        return (brain.isActive(Activity.REST) ||
        brain.isActive(Activity.HIDE) ||
        brain.isActive(Activity.PANIC) ||
        brain.isActive(Activity.SWIM) ||
        brain.isActive(Activity.PLAY) ||
        brain.isActive(Activity.WORK));
    }

    private void attractVillager() {
        if (!foundVillagers.isEmpty()) {
            foundVillagers.forEach(villager -> {
                if (!villager.isBaby()) {
                    if (level.random.nextInt(100) < ATTRACT_CHANCE) {
                        if(!villagerIsBusy(villager)) {
                            CompoundTag nbt = new CompoundTag();
                            villager.addAdditionalSaveData(nbt);
                            boolean willShop = true;
                            if (nbt.contains("LastLuredByBusiness")) {
                                if (nbt.getLong("LastLuredByBusiness")+LURED_BY_SALES_COOLDOWN*1000 > System.currentTimeMillis()) {
                                    willShop = false;
                                }
                            }
                            if (willShop && nbt.contains("BusinessRecords")) {
                                CompoundTag records = nbt.getCompound("BusinessRecords");
                                String itemId = getSellingItemID();
                                if (records.getAllKeys().contains(itemId)) {
                                    if (records.getLong(itemId) > System.currentTimeMillis()) {
                                        willShop = false;
                                    }
                                }
                            }
                            if (willShop) {
                                luringVillagers.add(new VillagerLure(villager, 180));
                            }
                        }
                    }
                }
            });
        }
    }

    private void moveVillagerTowardBlock(Villager villager) {
        CompoundTag nbt = new CompoundTag();
        villager.addAdditionalSaveData(nbt);
        nbt.putLong("LastLuredByBusiness", System.currentTimeMillis());
        villager.readAdditionalSaveData(nbt);

        Brain<Villager> brain = villager.getBrain();
        brain.setActiveActivityIfPossible(Activity.IDLE);
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(worldPosition));
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(worldPosition), 0.5F, 2));
        //((ServerWorld) world).spawnParticles(ParticleTypes.CRIT, villager.getX(), villager.getY() + 1, villager.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
    }

    private void evaluateSale(Villager villager) {
        if (itemPrice != null && level.random.nextInt(100) < itemPrice.getSaleChance(priceSetting) && canSell()) {
            performSale(villager);
        } else {
            rejectSale(villager);
        }
    }

    private void performSale(Villager villager) {
        this.removeItem(INPUT_SLOT, itemPrice.getSellAmount(priceSetting));

        if (getItem(OUTPUT_SLOT_NUGGETS).isEmpty())
            outputNuggetCount = 0;
        else
            outputNuggetCount = getItem(OUTPUT_SLOT_NUGGETS).getCount();
        
        if (getItem(OUTPUT_SLOT_EMERALDS).isEmpty())
            outputEmeraldCount = 0;
        else 
            outputEmeraldCount = getItem(OUTPUT_SLOT_EMERALDS).getCount();

        if (getItem(OUTPUT_SLOT_BLOCKS).isEmpty())
            outputBlockCount = 0;
        else
            outputBlockCount = getItem(OUTPUT_SLOT_BLOCKS).getCount();

        int resultingNuggets = outputNuggetCount + itemPrice.getPrice(priceSetting);
        int resultingEmeralds = outputEmeraldCount;
        int resultingBlocks = outputBlockCount;
        while (resultingNuggets >= 9) {
            if (resultingEmeralds >= 64 && resultingBlocks >= 64)
                break;
            resultingEmeralds += 1;
            resultingNuggets -= 9;
            if (resultingEmeralds > 64) {
                resultingBlocks += 1;
                resultingEmeralds -= 9;
            }
        }

        if (resultingNuggets != outputNuggetCount) {
            outputNuggetCount = resultingNuggets;
            this.setItem(OUTPUT_SLOT_NUGGETS, new ItemStack(VillagerBusinessItemInit.EMERALD_NUGGET, resultingNuggets));
        }

        if (resultingEmeralds != outputEmeraldCount) {
            outputEmeraldCount = resultingEmeralds;
            this.setItem(OUTPUT_SLOT_EMERALDS, new ItemStack(Items.EMERALD, resultingEmeralds));
        }

        if (resultingBlocks != outputBlockCount) {
            outputBlockCount = resultingBlocks;
            this.setItem(OUTPUT_SLOT_BLOCKS, new ItemStack(Items.EMERALD_BLOCK, outputBlockCount));
        }

        if (villager != null) {
            CompoundTag nbt = new CompoundTag();
            villager.addAdditionalSaveData(nbt);
            CompoundTag business = nbt.getCompound("BusinessRecords");
            business.putLong(getSellingItemID(), System.currentTimeMillis()+(int)(SUCCESSFUL_PURCHASE_COOLDOWN*1000*itemPrice.getCooldown(this.priceSetting)));
            nbt.put("BusinessRecords", business);
            villager.readAdditionalSaveData(nbt);

            level.playSound(null, worldPosition, SoundEvents.VILLAGER_TRADE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1, villager.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
        }

        if (getItem(INPUT_SLOT).isEmpty()) {
            updatePrices();
        }
        updateListeners();
    }

    private void rejectSale(Villager villager) {
        if (itemPrice != null) {
            CompoundTag nbt = new CompoundTag();
            villager.addAdditionalSaveData(nbt);
            CompoundTag business = nbt.getCompound("BusinessRecords");
            business.putLong(getSellingItemID(), System.currentTimeMillis()+REJECTED_PURCHASE_COOLDOWN*1000*itemPrice.getCooldown(this.priceSetting));
            nbt.put("BusinessRecords", business);
            villager.readAdditionalSaveData(nbt);
        }

        level.playSound(null, worldPosition, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
        ((ServerLevel) level).sendParticles(ParticleTypes.ANGRY_VILLAGER, villager.getX(), villager.getY() + 1, villager.getZ(), 2, 0.5, 0.5, 0.5, 0.1);
        villager.setUnhappyCounter(40);
    }

    private void getFrustrated(Villager villager) {
        CompoundTag nbt = new CompoundTag();
        villager.addAdditionalSaveData(nbt);
        CompoundTag business = nbt.getCompound("BusinessRecords");
        business.putLong(getSellingItemID(), System.currentTimeMillis()+REJECTED_PURCHASE_COOLDOWN*300000); // Ignore item for 5 minutes
        nbt.put("BusinessRecords", business);
        villager.readAdditionalSaveData(nbt);

        level.playSound(null, worldPosition, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
        ((ServerLevel) level).sendParticles(ParticleTypes.ANGRY_VILLAGER, villager.getX(), villager.getY() + 1, villager.getZ(), 5, 1, 1, 1, 0.1);
        villager.setUnhappyCounter(40);

        Brain<Villager> brain = villager.getBrain();
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(villager.position()), 0.5F, 2));
        brain.setActiveActivityIfPossible(Activity.IDLE);
    }

    private boolean canSell() {
        if (level.hasNeighborSignal(worldPosition))
            return false;
        if (!hasEnoughProduct())
            return false;
        return canInsertAmountIntoOutputSlot();
    }

    public boolean hasProduct() {
        if (itemPrice == null) return false;
        return !getItem(INPUT_SLOT).isEmpty();
    }

    public boolean hasEnoughProduct() {
        if (itemPrice == null) return false;
        return !getItem(INPUT_SLOT).isEmpty() && getItem(INPUT_SLOT).getCount() >= itemPrice.getSellAmount(priceSetting);
    }

    public boolean canInsertAmountIntoOutputSlot() {
        if (getItem(OUTPUT_SLOT_NUGGETS).isEmpty())
            outputNuggetCount = 0;
        else
            outputNuggetCount = getItem(OUTPUT_SLOT_NUGGETS).getCount();
        
        if (getItem(OUTPUT_SLOT_EMERALDS).isEmpty())
            outputEmeraldCount = 0;
        else 
            outputEmeraldCount = getItem(OUTPUT_SLOT_EMERALDS).getCount();

        if (getItem(OUTPUT_SLOT_BLOCKS).isEmpty())
            outputBlockCount = 0;
        else
            outputBlockCount = getItem(OUTPUT_SLOT_BLOCKS).getCount();

        int resultingNuggets = outputNuggetCount + itemPrice.getPrice(priceSetting);
        int resultingEmeralds = outputEmeraldCount;
        int resultingBlocks = outputBlockCount;
        while (resultingNuggets >= 9) {
            if (resultingEmeralds >= 64 && resultingBlocks >= 64)
                break;
            resultingEmeralds += 1;
            resultingNuggets -= 9;
            if (resultingEmeralds > 64) {
                resultingBlocks += 1;
                resultingEmeralds -= 9;
            }
        }

        return resultingNuggets <= 64 && resultingEmeralds <= 64 && resultingBlocks <= 64;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

        inventory.set(slot, stack);

        if (slot == INPUT_SLOT) {
            updatePrices();
        }

        lastUpdatedInputCount = getItem(INPUT_SLOT).getCount();
        lastUpdatedInputRawId = Item.getId(getItem(INPUT_SLOT).getItem());
        lastUpdatedNuggetCount = getItem(OUTPUT_SLOT_NUGGETS).getCount();
        lastUpdatedEmeraldCount = getItem(OUTPUT_SLOT_EMERALDS).getCount();
        lastUpdatedBlockCount = getItem(OUTPUT_SLOT_BLOCKS).getCount();

        updateListeners();
    }

    private void updatePrices() {
        ItemStack stack = getItem(INPUT_SLOT);
        if (stack.isEmpty()) {
            itemPrice = null;
            return;
        }

        if (ItemPrices.priceList.containsKey(stack.getItem())) {
            itemPrice = ItemPrices.priceList.get(stack.getItem());
        } else {
            itemPrice = null;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (stack.getItem() == Items.EMERALD || stack.getItem() == Items.EMERALD_BLOCK || stack.getItem() == VillagerBusinessItemInit.EMERALD_NUGGET)
            return false;
        return slot == INPUT_SLOT;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (level.hasNeighborSignal(worldPosition))
            return true;
        return slot != INPUT_SLOT;
    }
            
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.village_business.sales_stand");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new SalesStandScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        ContainerHelper.loadAllItems(nbt, inventory);
        if (nbt.contains("InputCount", Tag.TAG_INT)) {
			this.inputCount = nbt.getInt("InputCount");
		}
        if (nbt.contains("OutputNuggetCount", Tag.TAG_INT)) {
			this.outputNuggetCount = nbt.getInt("OutputNuggetCount");
		}
        if (nbt.contains("OutputEmeraldCount", Tag.TAG_INT)) {
			this.outputEmeraldCount = nbt.getInt("OutputEmeraldCount");
		}
        if (nbt.contains("OutputBlockCount", Tag.TAG_INT)) {
			this.outputBlockCount = nbt.getInt("OutputBlockCount");
		}
        if (nbt.contains("PriceSetting", Tag.TAG_INT)) {
            this.priceSetting = nbt.getInt("PriceSetting");
        }

        if (level != null && level.isClientSide()) {
            int sellAmount = 0;
            int price = 0;
            int saleChance = 0;
            int cooldown = 0;
            if (nbt.contains("SellAmount", Tag.TAG_INT)) {
                sellAmount = nbt.getInt("SellAmount");
            }
            if (nbt.contains("Price", Tag.TAG_INT)) {
                price = nbt.getInt("Price");
            }
            if (nbt.contains("SaleChance", Tag.TAG_INT)) {
                saleChance = nbt.getInt("SaleChance");
            }
            if (nbt.contains("Cooldown", Tag.TAG_INT)) {
                cooldown = nbt.getInt("Cooldown");
            }
            itemPrice = new ItemPrice(getItem(INPUT_SLOT).getItem(), price, sellAmount, saleChance, 0, cooldown);
        } else {
            updatePrices();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        ContainerHelper.saveAllItems(nbt, inventory);
        nbt.putInt("PriceSetting", this.priceSetting);
        nbt.putInt("InputCount", this.inputCount);
        nbt.putInt("OutputNuggetCount", this.outputNuggetCount);
        nbt.putInt("OutputEmeraldCount", this.outputEmeraldCount);
        nbt.putInt("OutputBlockCount", this.outputBlockCount);

        if (itemPrice != null) {
            nbt.putInt("SellAmount", itemPrice.getSellAmount(1));
            nbt.putInt("Price", itemPrice.getPrice(1));
            nbt.putInt("SaleChance", itemPrice.getSaleChance(1));
            nbt.putInt("Cooldown", itemPrice.getCooldown(1));
        } else {
            nbt.putInt("SellAmount", 0);
            nbt.putInt("Price", 0);
            nbt.putInt("SaleChance", 0);
            nbt.putInt("Cooldown", 0);
        }
    }

    private String getSellingItemID() {
        return getItem(INPUT_SLOT).getItem().toString();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);
        updatePrices();
        return nbt;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public void updateListeners() {
        this.inputCount = getItem(INPUT_SLOT).getCount();
        this.outputNuggetCount = getItem(OUTPUT_SLOT_NUGGETS).getCount();
        this.outputEmeraldCount = getItem(OUTPUT_SLOT_EMERALDS).getCount();
        this.outputBlockCount = getItem(OUTPUT_SLOT_BLOCKS).getCount();
		this.setChanged();
		this.getLevel().sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
	}

    public int getInputCount() {
        return inputCount;
    }

    public int getOutputNuggetCount() {
        return outputNuggetCount;
    }

    public int getOutputEmeraldCount() {
        return outputEmeraldCount;
    }

    public int getOutputBlockCount() {
        return outputBlockCount;
    }
}
