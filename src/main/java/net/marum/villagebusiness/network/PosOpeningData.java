package net.marum.villagebusiness.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PosOpeningData(BlockPos pos) {
    public static PosOpeningData of(BlockPos pos) {
        return new PosOpeningData(pos);
    }

    public <T extends FriendlyByteBuf> void send(T buf) {
        CODEC.encode(buf, this);
    }

    public static StreamCodec<FriendlyByteBuf, PosOpeningData> CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, PosOpeningData::pos, PosOpeningData::new);

    public static PosOpeningData get(FriendlyByteBuf buf) {
        return CODEC.decode(buf);
    }
}
