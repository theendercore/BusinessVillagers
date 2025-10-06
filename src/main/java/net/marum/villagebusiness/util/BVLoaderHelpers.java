package net.marum.villagebusiness.util;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public interface BVLoaderHelpers {
    static void c2sPacket(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
