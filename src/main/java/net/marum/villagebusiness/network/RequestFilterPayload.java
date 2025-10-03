package net.marum.villagebusiness.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.marum.villagebusiness.VillageBusiness.id;

public record RequestFilterPayload(BlockPos pos, ItemStack filter) implements CustomPacketPayload {
    @Override
    public @NotNull Type<RequestFilterPayload> type() {
        return ID;
    }

    public static Type<RequestFilterPayload> ID = new Type<>(id("request_filter"));
    public static StreamCodec<RegistryFriendlyByteBuf, RequestFilterPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RequestFilterPayload::pos,
            ItemStack.OPTIONAL_STREAM_CODEC, RequestFilterPayload::filter,
            RequestFilterPayload::new
    );
}
