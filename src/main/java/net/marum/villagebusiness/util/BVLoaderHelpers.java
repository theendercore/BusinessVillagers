package net.marum.villagebusiness.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface BVLoaderHelpers {
    static void c2sPacket(ResourceLocation channelName, FriendlyByteBuf buf) {
        ClientPlayNetworking.send(channelName, buf);
    }
}
