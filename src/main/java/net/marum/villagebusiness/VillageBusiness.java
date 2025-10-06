package net.marum.villagebusiness;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.fabricmc.api.ModInitializer;
import net.marum.villagebusiness.config.VillageBusinessConfig;
import net.marum.villagebusiness.init.*;
import net.marum.villagebusiness.network.VillageBusinessNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillageBusiness implements ModInitializer {
    public static final String MOD_ID = "village_business";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final VillageBusinessConfig CONFIG = ConfigApiJava.registerAndLoadConfig(VillageBusinessConfig::new);
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