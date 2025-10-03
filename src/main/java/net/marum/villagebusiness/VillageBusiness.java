package net.marum.villagebusiness;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.marum.villagebusiness.config.SimpleConfig;
import net.marum.villagebusiness.init.*;
import net.marum.villagebusiness.network.VillageBusinessNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class VillageBusiness implements ModInitializer {
    public static final String MOD_ID = "village_business";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MemoryModuleType<Long> WONT_PURCHASE_UNTIL = Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE, id("last_purchased"), new MemoryModuleType<>(Optional.of(Codec.LONG)));

    public static final SimpleConfig CONFIG = SimpleConfig.of("villagebusiness").request();
    public static MinecraftServer SERVER;

    @Override
    public void onInitialize() {
        VillagerBusinessItems.init();
        VillagerBusinessBlocks.init();
        VillageBusinessBlockEntityTypeInit.init();
        VillageBusinessScreenHandlers.init();
        VillageBusinessEvents.init();
        VillageBusinessNetworking.init();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }
}