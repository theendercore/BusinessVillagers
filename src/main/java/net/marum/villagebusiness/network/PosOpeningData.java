package net.marum.villagebusiness.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PosOpeningData(BlockPos pos) {
    public static PosOpeningData of(BlockPos pos) {
        return new PosOpeningData(pos);
    }

    public static StreamCodec<FriendlyByteBuf, PosOpeningData> CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, PosOpeningData::pos, PosOpeningData::new);
}
