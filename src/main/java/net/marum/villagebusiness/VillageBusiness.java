package net.marum.villagebusiness;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.marum.villagebusiness.config.SimpleConfig;
import net.marum.villagebusiness.init.VillageBusinessBlockEntityTypeInit;
import net.marum.villagebusiness.init.VillagerBusinessBlockInit;
import net.marum.villagebusiness.init.VillagerBusinessItemInit;
import net.marum.villagebusiness.network.VillageBusinessNetworking;
import net.marum.villagebusiness.screen.VillageBusinessScreenHandlers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.CreativeModeTabs;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;

public class VillageBusiness implements ModInitializer {
	public static final String MOD_ID = "village_business";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final MemoryModuleType<Long> WONT_PURCHASE_UNTIL = Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE, id("last_purchased"), new MemoryModuleType<>(Optional.of(Codec.LONG)));

	public static final SimpleConfig CONFIG = SimpleConfig.of( "villagebusiness" ).request();
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(s -> SERVER = s);
		VillagerBusinessItemInit.load();
		VillagerBusinessBlockInit.load();
		VillageBusinessBlockEntityTypeInit.load();
		VillageBusinessScreenHandlers.load();
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> entries.accept(VillagerBusinessBlockInit.SALES_STAND_BLOCK));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> entries.accept(VillagerBusinessBlockInit.REQUEST_STAND_BLOCK));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> entries.accept(VillagerBusinessItemInit.EMERALD_NUGGET));
		VillageBusinessNetworking.registerServerHandlers();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.tryBuild(MOD_ID, path);
	}
}