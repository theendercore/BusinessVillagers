package net.marum.villagebusiness.network;

import net.marum.villagebusiness.block.entity.RequestStandBlockEntity;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class VillageBusinessNetworking {
    public static void init(IEventBus bus) {
        bus.addListener(VillageBusinessNetworking::register);
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(PriceSetPayload.ID, PriceSetPayload.CODEC, VillageBusinessNetworking::priceSetPayloadHandler);
        registrar.playToServer(RequestFilterPayload.ID, RequestFilterPayload.CODEC, VillageBusinessNetworking::requestFilterPayloadHandler);
    }

    @SuppressWarnings("resource")
    public static void priceSetPayloadHandler(PriceSetPayload payload, IPayloadContext ctx) {
        ctx.player().getServer().execute(() -> {
            if (ctx.player().level().getBlockEntity(payload.pos()) instanceof SalesStandBlockEntity blockEntity) {
                blockEntity.serverSetPriceSetting(payload.newPrice());
                blockEntity.updateListeners();
            }
        });

    }

    @SuppressWarnings("resource")
    public static void requestFilterPayloadHandler(RequestFilterPayload payload, IPayloadContext ctx) {
        ctx.player().getServer().execute(() -> {
            if (ctx.player().level().getBlockEntity(payload.pos()) instanceof RequestStandBlockEntity blockEntity) {
                blockEntity.setFilterItem(payload.filter());
                blockEntity.updateListeners();
            }
        });
    }
}
