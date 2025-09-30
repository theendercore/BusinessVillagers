package net.marum.villagebusiness.util;

import net.minecraft.world.entity.npc.Villager;

public class VillagerLure {
    public Villager villager;
    public double expirationTimestamp;

    public VillagerLure(Villager newVillager, int expiresInSeconds) {
        villager = newVillager;
        expirationTimestamp = System.currentTimeMillis() + expiresInSeconds * 1000;
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() > expirationTimestamp;
    }
}
