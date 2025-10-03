package net.marum.villagebusiness.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.marum.villagebusiness.block.entity.RequestStandBlockEntity;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntity;

public class VillageBusinessNetworking {
    @SuppressWarnings("resource")
    public static void init() {
        PayloadTypeRegistry.playC2S().register(PriceSetPayload.ID, PriceSetPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestFilterPayload.ID, RequestFilterPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PriceSetPayload.ID, (payload, ctx) -> ctx.server().execute(() -> {
            if (ctx.player().level().getBlockEntity(payload.pos()) instanceof SalesStandBlockEntity blockEntity) {
                blockEntity.serverSetPriceSetting(payload.newPrice());
                blockEntity.updateListeners();
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(RequestFilterPayload.ID, (payload, ctx) -> ctx.server().execute(() -> {
            if (ctx.player().level().getBlockEntity(payload.pos()) instanceof RequestStandBlockEntity blockEntity) {
                blockEntity.setFilterItem(payload.filter());
                blockEntity.updateListeners();
            }
        }));
    }
}
