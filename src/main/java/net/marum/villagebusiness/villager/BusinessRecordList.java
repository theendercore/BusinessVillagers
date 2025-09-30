package net.marum.villagebusiness.villager;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;

public class BusinessRecordList extends ArrayList<BusinessRecord> {
    public BusinessRecordList() {
    }

    public BusinessRecordList(CompoundTag nbt) {
        nbt.getAllKeys().forEach(key -> {
            Long value = nbt.getLong(key);
            this.add(new BusinessRecord(key, value));
        });
    }

    public CompoundTag toNbt() {
        CompoundTag nbtCompound = new CompoundTag();

        for (int i = 0; i < this.size(); i++) {
            BusinessRecord record = (BusinessRecord) this.get(i);
            nbtCompound.putLong(record.getItemID(), record.getTimestamp());
        }

        return nbtCompound;
    }
}
