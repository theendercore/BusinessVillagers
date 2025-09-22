package net.marum.villagebusiness.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.marum.villagebusiness.VillageBusiness;
import net.marum.villagebusiness.block.entity.RequestStandBlockEntity;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class VillageBusinessNetworking {
    public static final ResourceLocation PRICE_SETTING_PACKET = VillageBusiness.id("price_setting");
    public static final ResourceLocation REQUEST_PACKET = VillageBusiness.id("request");

    public static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(PRICE_SETTING_PACKET, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int value = buf.readInt();

            server.execute(() -> {
                if (player.level().getBlockEntity(pos) instanceof SalesStandBlockEntity blockEntity) {
                    blockEntity.serverSetPriceSetting(value);
                    blockEntity.updateListeners();
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(REQUEST_PACKET, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            ItemStack value = buf.readItem();

            server.execute(() -> {
                if (player.level().getBlockEntity(pos) instanceof RequestStandBlockEntity blockEntity) {
                    blockEntity.setFilterItem(value);
                    blockEntity.updateListeners();
                }
            });
        });
    }
}
