package net.marum.villagebusiness.init;

import net.marum.villagebusiness.block.entity.RequestStandBlockEntity;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static net.marum.villagebusiness.VillageBusiness.id;

public class VillageBusinessBlockEntityTypeInit {
    public static final BlockEntityType<SalesStandBlockEntity> SALES_STAND_ENTITY =
            register("sales_stand_entity", SalesStandBlockEntity::new, VillagerBusinessBlocks.SALES_STAND_BLOCK);
    public static final BlockEntityType<RequestStandBlockEntity> REQUEST_STAND_ENTITY =
            register("request_stand_entity", RequestStandBlockEntity::new, VillagerBusinessBlocks.REQUEST_STAND_BLOCK);

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.BlockEntitySupplier<T> blockEntitySupplier, Block... blocks) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id(name), BlockEntityType.Builder.of(blockEntitySupplier, blocks).build(null));
    }

    public static void init() { }
}
