package net.marum.villagebusiness.block.entity;

import net.marum.villagebusiness.VillageBusiness;
import net.marum.villagebusiness.init.VillageBusinessBlockEntityTypeInit;
import net.marum.villagebusiness.init.VillagerBusinessItems;
import net.marum.villagebusiness.network.RequestFilterPayload;
import net.marum.villagebusiness.pricing.ItemPrice;
import net.marum.villagebusiness.pricing.ItemPrices;
import net.marum.villagebusiness.screen.RequestStandScreenHandler;
import net.marum.villagebusiness.util.BVLoaderHelpers;
import net.marum.villagebusiness.util.VillagerLure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RequestStandBlockEntity extends BlockEntity implements MenuProvider, ImplementedInventory {
    private ItemStack filterItem = ItemStack.EMPTY;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    private static final int OUTPUT_SLOT = 0;
    private static final int INPUT_SLOT_NUGGETS = 3;
    private static final int INPUT_SLOT_EMERALDS = 2;
    private static final int INPUT_SLOT_BLOCKS = 1;
    private static final int ATTRACT_CHANCE = 1;

    private List<Villager> foundVillagers = new ArrayList<>();
    private final Set<VillagerLure> luringVillagers = new HashSet<>();
    private final Set<VillagerLure> markedForRemovalVillagers = new HashSet<>();

    private static final int RADIUS = 50;
    private static final int SUCCESSFUL_PURCHASE_COOLDOWN = 1;
    private static final int REJECTED_PURCHASE_COOLDOWN = 1;
    private static final int LURED_BY_SALES_COOLDOWN = 1;

    private double priceMultiplier;
    private double cooldownMultiplier;

    private int ticks = 0;

    private int inputCount = 0;
    private int inputNuggetCount = 0;
    private int inputEmeraldCount = 0;
    private int inputBlockCount = 0;

    private ItemPrice itemPrice;
    private int priceSetting = 1;

    private int lastUpdatedOutputCount = 0;
    private int lastUpdatedOutputRawId = 0;
    private int lastUpdatedNuggetCount = 0;
    private int lastUpdatedEmeraldCount = 0;
    private int lastUpdatedBlockCount = 0;

    public RequestStandBlockEntity(BlockPos pos, BlockState state) {
        super(VillageBusinessBlockEntityTypeInit.REQUEST_STAND_ENTITY.get(), pos, state);
    }

    public ItemPrice getItemPrice() {
        return itemPrice;
    }

    public int getPriceSetting() {
        return priceSetting;
    }

    public void sendRequestToServer(ItemStack itemStack) {
        BVLoaderHelpers.c2sPacket(new RequestFilterPayload(worldPosition, itemStack));
        this.filterItem = itemStack;
        updatePrices();
    }

    public ItemStack getFilterItem() {
        return filterItem;
    }

    public void setFilterItem(ItemStack stack) {
        if (stack.getItem() == Items.EMERALD || stack.getItem() == Items.EMERALD_BLOCK || stack.getItem() == VillagerBusinessItems.EMERALD_NUGGET.get())
            return;
        this.filterItem = stack;
        updatePrices();
        setChanged();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, RequestStandBlockEntity entity) {
        if (world == null || world.isClientSide())
            return;

        if (entity.ticks == 0) {
            entity.updatePrices();
            entity.setChanged();
        }

        // Lure villagers every 5 seconds
        if (entity.ticks % 100 == 0) {
            if (entity.canBuy()) {
                entity.attractVillager();
            }
        }

        // Move lured villagers every 0.5 second
        if (entity.ticks % 10 == 0) {
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
            entity.markedForRemovalVillagers.forEach(entity.luringVillagers::remove);
            entity.markedForRemovalVillagers.clear();

            boolean inventoryChanged = false;
            if (entity.getItem(OUTPUT_SLOT).getCount() != entity.lastUpdatedOutputCount) {
                entity.lastUpdatedOutputCount = entity.getItem(OUTPUT_SLOT).getCount();
                inventoryChanged = true;
            }
            if (Item.getId(entity.getItem(OUTPUT_SLOT).getItem()) != entity.lastUpdatedOutputRawId) {
                entity.lastUpdatedOutputRawId = Item.getId(entity.getItem(OUTPUT_SLOT).getItem());
                inventoryChanged = true;
            }
            if (entity.getItem(INPUT_SLOT_NUGGETS).getCount() != entity.lastUpdatedBlockCount ||
                    entity.getItem(INPUT_SLOT_EMERALDS).getCount() != entity.lastUpdatedEmeraldCount ||
                    entity.getItem(INPUT_SLOT_BLOCKS).getCount() != entity.lastUpdatedNuggetCount) {
                inventoryChanged = true;
                entity.lastUpdatedNuggetCount = entity.getItem(INPUT_SLOT_NUGGETS).getCount();
                entity.lastUpdatedEmeraldCount = entity.getItem(INPUT_SLOT_EMERALDS).getCount();
                entity.lastUpdatedBlockCount = entity.getItem(INPUT_SLOT_BLOCKS).getCount();
            }
            if (inventoryChanged) {
                entity.updatePrices();
                entity.updateListeners();
            }
        }

        // Find nearby villagers every 20 seconds
        if (entity.ticks >= 400) {
            entity.foundVillagers = world.getEntitiesOfClass(Villager.class,
                    AABB.encapsulatingFullBlocks(pos.offset(-RADIUS, -RADIUS, -RADIUS), pos.offset(RADIUS, RADIUS, RADIUS)),
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
                        if (level.random.nextInt(100) < itemPrice.getRequestChance()) {
                            if (!villagerIsBusy(villager)) {
                                CompoundTag nbt = new CompoundTag();
                                villager.addAdditionalSaveData(nbt);
                                boolean willShop = true;
                                if (nbt.contains("LastLuredByBusiness")) {
                                    if (nbt.getLong("LastLuredByBusiness") + LURED_BY_SALES_COOLDOWN * 1000 > System.currentTimeMillis()) {
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
        if (itemPrice != null && canBuy()) {
            performSale(villager);
        }
    }

    private void performSale(Villager villager) {
        int outputCount = getItem(OUTPUT_SLOT).getCount();
        this.setItem(OUTPUT_SLOT, new ItemStack(filterItem.getItem(), outputCount + itemPrice.getSellAmount(priceSetting)));

        if (getItem(INPUT_SLOT_NUGGETS).isEmpty())
            inputNuggetCount = 0;
        else
            inputNuggetCount = getItem(INPUT_SLOT_NUGGETS).getCount();

        if (getItem(INPUT_SLOT_EMERALDS).isEmpty())
            inputEmeraldCount = 0;
        else
            inputEmeraldCount = getItem(INPUT_SLOT_EMERALDS).getCount();

        if (getItem(INPUT_SLOT_BLOCKS).isEmpty())
            inputBlockCount = 0;
        else
            inputBlockCount = getItem(INPUT_SLOT_BLOCKS).getCount();

        int resultingNuggets = inputNuggetCount - getRequestPrice();
        int resultingEmeralds = inputEmeraldCount;
        int resultingBlocks = inputBlockCount;
        while (resultingNuggets < 0) {
            resultingEmeralds -= 1;
            resultingNuggets += 9;
            while (resultingEmeralds < 0) {
                resultingBlocks -= 1;
                resultingEmeralds += 9;
            }
        }

        if (resultingNuggets != inputNuggetCount) {
            inputNuggetCount = resultingNuggets;
            this.setItem(INPUT_SLOT_NUGGETS, new ItemStack(VillagerBusinessItems.EMERALD_NUGGET.get(), resultingNuggets));
        }

        if (resultingEmeralds != inputEmeraldCount) {
            inputEmeraldCount = resultingEmeralds;
            this.setItem(INPUT_SLOT_EMERALDS, new ItemStack(Items.EMERALD, resultingEmeralds));
        }

        if (resultingBlocks != inputBlockCount) {
            inputBlockCount = resultingBlocks;
            this.setItem(INPUT_SLOT_BLOCKS, new ItemStack(Items.EMERALD_BLOCK, inputBlockCount));
        }

        if (villager != null) {
            CompoundTag nbt = new CompoundTag();
            villager.addAdditionalSaveData(nbt);
            CompoundTag business = nbt.getCompound("BusinessRecords");
            var timeMultiplier = VillageBusiness.CONFIG.requestTimeMultiplier.get();
            business.putLong(getSellingItemID(), System.currentTimeMillis() + (int) (SUCCESSFUL_PURCHASE_COOLDOWN * 1000 * itemPrice.getCooldown(this.priceSetting) * timeMultiplier));
            nbt.put("BusinessRecords", business);
            villager.readAdditionalSaveData(nbt);

            level.playSound(null, worldPosition, SoundEvents.VILLAGER_TRADE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1, villager.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
        }

        if (getItem(OUTPUT_SLOT).isEmpty()) {
            updatePrices();
        }
        updateListeners();
    }

    private void getFrustrated(Villager villager) {
        CompoundTag nbt = new CompoundTag();
        villager.addAdditionalSaveData(nbt);
        CompoundTag business = nbt.getCompound("BusinessRecords");
        business.putLong(getSellingItemID(), System.currentTimeMillis() + REJECTED_PURCHASE_COOLDOWN * 300000); // Ignore item for 5 minutes
        nbt.put("BusinessRecords", business);
        villager.readAdditionalSaveData(nbt);

        level.playSound(null, worldPosition, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
        ((ServerLevel) level).sendParticles(ParticleTypes.ANGRY_VILLAGER, villager.getX(), villager.getY() + 1, villager.getZ(), 5, 1, 1, 1, 0.1);
        villager.setUnhappyCounter(40);

        Brain<Villager> brain = villager.getBrain();
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(villager.position()), 0.5F, 2));
        brain.setActiveActivityIfPossible(Activity.IDLE);
    }

    private boolean canBuy() {
        if (level.hasNeighborSignal(worldPosition))
            return false;
        if (!hasEnoughSpace())
            return false;
        return hasEnoughEmeralds();
    }

    public boolean hasFilter() {
        if (filterItem == null) return false;
        return !filterItem.isEmpty();
    }

    public boolean hasProduct() {
        if (itemPrice == null) return false;
        return !getItem(OUTPUT_SLOT).isEmpty();
    }

    public boolean hasEnoughSpace() {
        if (itemPrice == null) return false;
        if (getItem(OUTPUT_SLOT).isEmpty()) return true;
        if (!getItem(OUTPUT_SLOT).is(filterItem.getItem())) return false;
        return getItem(OUTPUT_SLOT).getCount() + itemPrice.getSellAmount(1) <= filterItem.getMaxStackSize();
    }

    public boolean hasEnoughEmeralds() {
        inputNuggetCount = getItem(INPUT_SLOT_NUGGETS).getCount();
        inputEmeraldCount = getItem(INPUT_SLOT_EMERALDS).getCount();
        inputBlockCount = getItem(INPUT_SLOT_BLOCKS).getCount();
        return inputNuggetCount + inputEmeraldCount * 9 + inputBlockCount * 81 >= getRequestPrice();
    }

    public int getRequestPrice() {
        return (int) Math.round(itemPrice.getPrice(1) * priceMultiplier);
    }

    public int getRequestCooldown() {
        return (int) Math.round(itemPrice.getCooldown(1) * cooldownMultiplier);
    }

    public int getRequestChance() {
        return itemPrice.getRequestChance();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

        inventory.set(slot, stack);

        lastUpdatedOutputCount = getItem(OUTPUT_SLOT).getCount();
        lastUpdatedOutputRawId = Item.getId(getItem(OUTPUT_SLOT).getItem());
        lastUpdatedNuggetCount = getItem(INPUT_SLOT_NUGGETS).getCount();
        lastUpdatedEmeraldCount = getItem(INPUT_SLOT_EMERALDS).getCount();
        lastUpdatedBlockCount = getItem(INPUT_SLOT_BLOCKS).getCount();

        updateListeners();
    }

    private void updatePrices() {
        ItemStack stack = getFilterItem();
        if (stack.isEmpty()) {
            itemPrice = null;
            return;
        }

        itemPrice = ItemPrices.getPrices().getOrDefault(stack.getItem(), null);

        priceMultiplier = VillageBusiness.CONFIG.requestPriceMultiplier.get();
        cooldownMultiplier = VillageBusiness.CONFIG.requestCooldownMultiplier.get();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (stack.getItem() == Items.EMERALD_BLOCK && slot == INPUT_SLOT_BLOCKS) return true;
        if (stack.getItem() == Items.EMERALD && slot == INPUT_SLOT_EMERALDS) return true;
        return stack.getItem() == VillagerBusinessItems.EMERALD_NUGGET.get() && slot == INPUT_SLOT_NUGGETS;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (level.hasNeighborSignal(worldPosition))
            return true;
        return slot == OUTPUT_SLOT;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.village_business.request_stand");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return new RequestStandScreenHandler(i, inventory, worldPosition);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(nbt, provider);
        ContainerHelper.loadAllItems(nbt, inventory, provider);
        if (nbt.contains("InputCount", Tag.TAG_INT)) {
            this.inputCount = nbt.getInt("InputCount");
        }
        if (nbt.contains("OutputNuggetCount", Tag.TAG_INT)) {
            this.inputNuggetCount = nbt.getInt("OutputNuggetCount");
        }
        if (nbt.contains("OutputEmeraldCount", Tag.TAG_INT)) {
            this.inputEmeraldCount = nbt.getInt("OutputEmeraldCount");
        }
        if (nbt.contains("OutputBlockCount", Tag.TAG_INT)) {
            this.inputBlockCount = nbt.getInt("OutputBlockCount");
        }
        if (nbt.contains("FilterItem")) {
            filterItem = ItemStack.parse(provider, nbt.getCompound("FilterItem")).orElse(ItemStack.EMPTY);
        } else {
            filterItem = ItemStack.EMPTY;
        }

        if (level != null && level.isClientSide()) {
            int sellAmount = 0;
            int price = 0;
            int requestChance = 0;
            int cooldown = 0;
            if (nbt.contains("SellAmount", Tag.TAG_INT)) {
                sellAmount = nbt.getInt("SellAmount");
            }
            if (nbt.contains("Price", Tag.TAG_INT)) {
                price = nbt.getInt("Price");
            }
            if (nbt.contains("RequestChance", Tag.TAG_INT)) {
                requestChance = nbt.getInt("RequestChance");
            }
            if (nbt.contains("Cooldown", Tag.TAG_INT)) {
                cooldown = nbt.getInt("Cooldown");
            }
            itemPrice = new ItemPrice(getItem(OUTPUT_SLOT).getItem(), price, sellAmount, 0, requestChance, cooldown);
        }
        updatePrices();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(nbt, provider);
        ContainerHelper.saveAllItems(nbt, inventory, provider);
        nbt.putInt("InputCount", this.inputCount);
        nbt.putInt("OutputNuggetCount", this.inputNuggetCount);
        nbt.putInt("OutputEmeraldCount", this.inputEmeraldCount);
        nbt.putInt("OutputBlockCount", this.inputBlockCount);

        if (itemPrice != null) {
            nbt.putInt("SellAmount", itemPrice.getSellAmount(1));
            nbt.putInt("Price", itemPrice.getPrice(1));
            nbt.putInt("RequestChance", itemPrice.getRequestChance());
            nbt.putInt("Cooldown", itemPrice.getCooldown(1));
        } else {
            nbt.putInt("SellAmount", 0);
            nbt.putInt("Price", 0);
            nbt.putInt("RequestChance", 0);
            nbt.putInt("Cooldown", 0);
        }

        if (!filterItem.isEmpty()) {
            nbt.put("FilterItem", filterItem.save(provider));
        }
    }

    private String getSellingItemID() {
        return getItem(OUTPUT_SLOT).getItem().toString();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        CompoundTag nbt = super.getUpdateTag(provider);
        this.saveAdditional(nbt, provider);
        updatePrices();
        return nbt;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void updateListeners() {
        this.inputCount = getItem(OUTPUT_SLOT).getCount();
        this.inputNuggetCount = getItem(INPUT_SLOT_NUGGETS).getCount();
        this.inputEmeraldCount = getItem(INPUT_SLOT_EMERALDS).getCount();
        this.inputBlockCount = getItem(INPUT_SLOT_BLOCKS).getCount();
        this.setChanged();
        this.getLevel().sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    public int getInputCount() {
        return inputCount;
    }

    public int getInputNuggetCount() {
        return inputNuggetCount;
    }

    public int getInputEmeraldCount() {
        return inputEmeraldCount;
    }

    public int getInputBlockCount() {
        return inputBlockCount;
    }
}
