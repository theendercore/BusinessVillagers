package net.marum.villagebusiness.init;

import net.marum.villagebusiness.block.RequestStandBlock;
import net.marum.villagebusiness.block.SalesStandBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.marum.villagebusiness.VillageBusiness.MOD_ID;

public class VillagerBusinessBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);

    public static final BlockBehaviour.Properties STAND_PROPS = BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).isValidSpawn(Blocks::never);

    public static final DeferredBlock<SalesStandBlock> SALES_STAND_BLOCK =
            registerWithItem("sales_stand", () -> new SalesStandBlock(STAND_PROPS));
    public static final DeferredBlock<RequestStandBlock> REQUEST_STAND_BLOCK =
            registerWithItem("request_stand", () -> new RequestStandBlock(STAND_PROPS));


    public static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static <T extends Block> DeferredBlock<T> registerWithItem(String name, Supplier<T> block) {
        DeferredBlock<T> registered = register(name, block);
        VillagerBusinessItems.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
