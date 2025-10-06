package net.marum.villagebusiness;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import net.marum.villagebusiness.config.VillageBusinessConfig;
import net.marum.villagebusiness.init.*;
import net.marum.villagebusiness.network.VillageBusinessNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.marum.villagebusiness.VillageBusiness.MOD_ID;

@Mod(value = MOD_ID)
public class VillageBusiness {
    public static final String MOD_ID = "village_business";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final VillageBusinessConfig CONFIG = ConfigApiJava.registerAndLoadConfig(VillageBusinessConfig::new, RegisterType.SERVER);
    public static MinecraftServer SERVER;

    public VillageBusiness(IEventBus bus, ModContainer modContainer) {
        VillagerBusinessItems.init(bus);
        VillagerBusinessBlocks.init(bus);
        VillageBusinessBlockEntityTypeInit.init(bus);
        VillageBusinessScreenHandlers.init(bus);
        VillageBusinessEvents.init(bus);
        VillageBusinessNetworking.init(bus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }
}