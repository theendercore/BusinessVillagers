package net.marum.villagebusiness.init;

import net.marum.villagebusiness.block.entity.RequestStandBlockEntity;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.marum.villagebusiness.VillageBusiness.MOD_ID;

public class VillageBusinessBlockEntityTypeInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SalesStandBlockEntity>> SALES_STAND_ENTITY =
            register("sales_stand_entity", SalesStandBlockEntity::new, VillagerBusinessBlocks.SALES_STAND_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RequestStandBlockEntity>> REQUEST_STAND_ENTITY =
            register("request_stand_entity", RequestStandBlockEntity::new, VillagerBusinessBlocks.REQUEST_STAND_BLOCK);


    public static <T extends BlockEntity, B extends Block> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> blockEntitySupplier, DeferredBlock<B> blocks) {
        return BLOCK_ENTITY_TYPES.register(name, () -> BlockEntityType.Builder.of(blockEntitySupplier, blocks.get()).build(null));
    }

    public static void init(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
