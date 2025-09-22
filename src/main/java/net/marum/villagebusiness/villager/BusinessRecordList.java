package net.marum.villagebusiness.villager;

import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;

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
			BusinessRecord record = (BusinessRecord)this.get(i);
			nbtCompound.putLong(record.getItemID(), record.getTimestamp());
		}

		return nbtCompound;
	}
}
