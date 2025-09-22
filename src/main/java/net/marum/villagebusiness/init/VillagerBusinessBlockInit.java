package net.marum.villagebusiness.init;

import net.marum.villagebusiness.VillageBusiness;
import net.marum.villagebusiness.block.RequestStandBlock;
import net.marum.villagebusiness.block.SalesStandBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class VillagerBusinessBlockInit {
    public static final SalesStandBlock SALES_STAND_BLOCK = registerWithItem("sales_stand",
        new SalesStandBlock(BlockBehaviour.Properties.copy(Blocks.BARREL).isValidSpawn((state, world, pos, entityType) -> false)), new Item.Properties());
    public static final RequestStandBlock REQUEST_STAND_BLOCK = registerWithItem("request_stand",
        new RequestStandBlock(BlockBehaviour.Properties.copy(Blocks.BARREL).isValidSpawn((state, world, pos, entityType) -> false)), new Item.Properties());

    public static <T extends Block> T register(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, VillageBusiness.id(name), block);
    }

    public static <T extends Block> T registerWithItem(String name, T block, Item.Properties settings) {
        T registered = register(name, block);
        VillagerBusinessItemInit.register(name, new BlockItem(block, settings));
        return registered;
    }

    public static void load() {
        
    }
}
