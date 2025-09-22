package net.marum.villagebusiness.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.marum.villagebusiness.VillageBusiness;
import net.marum.villagebusiness.block.entity.RequestStandBlockEntity;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VillageBusinessBlockEntityTypeInit {
    public static final BlockEntityType<SalesStandBlockEntity> SALES_STAND_ENTITY = register("sales_stand_entity", 
        FabricBlockEntityTypeBuilder.create(SalesStandBlockEntity::new, VillagerBusinessBlockInit.SALES_STAND_BLOCK)
            .build());
    public static final BlockEntityType<RequestStandBlockEntity> REQUEST_STAND_ENTITY = register("request_stand_entity", 
        FabricBlockEntityTypeBuilder.create(RequestStandBlockEntity::new, VillagerBusinessBlockInit.REQUEST_STAND_BLOCK)
            .build());

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, VillageBusiness.id(name), type);
    }

    public static void load() {}
}
