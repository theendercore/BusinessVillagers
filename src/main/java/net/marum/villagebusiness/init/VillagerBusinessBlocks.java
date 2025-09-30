package net.marum.villagebusiness.init;

import net.marum.villagebusiness.block.RequestStandBlock;
import net.marum.villagebusiness.block.SalesStandBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static net.marum.villagebusiness.VillageBusiness.id;

public class VillagerBusinessBlocks {
    public static final BlockBehaviour.Properties STAND_PROPS = BlockBehaviour.Properties.copy(Blocks.BARREL).isValidSpawn(Blocks::never);

    public static final SalesStandBlock SALES_STAND_BLOCK = registerWithItem("sales_stand", new SalesStandBlock(STAND_PROPS));
    public static final RequestStandBlock REQUEST_STAND_BLOCK = registerWithItem("request_stand", new RequestStandBlock(STAND_PROPS));


    public static <T extends Block> T register(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, id(name), block);
    }

    public static <T extends Block> T registerWithItem(String name, T block) {
        T registered = register(name, block);
        VillagerBusinessItems.register(name, new BlockItem(block, new Item.Properties()));
        return registered;
    }

    public static void init() { }
}
