package net.marum.villagebusiness.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static net.marum.villagebusiness.VillageBusiness.id;

public record PriceSetPayload(BlockPos pos, int newPrice) implements CustomPacketPayload {
    @Override
    public @NotNull Type<PriceSetPayload> type() {
        return ID;
    }

    public static CustomPacketPayload.Type<PriceSetPayload> ID = new CustomPacketPayload.Type<>(id("price_set"));

    public static StreamCodec<FriendlyByteBuf, PriceSetPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PriceSetPayload::pos,
            ByteBufCodecs.INT, PriceSetPayload::newPrice,
            PriceSetPayload::new
    );
}
