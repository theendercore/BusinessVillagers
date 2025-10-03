package net.marum.villagebusiness.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface BVLoaderHelpers {
    static void c2sPacket(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
