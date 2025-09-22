package net.marum.villagebusiness.mixin;

import net.marum.villagebusiness.villager.BusinessRecordList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(Villager.class)
public class VillageBusinessMixin {

    @Nullable
	public BusinessRecordList businessRecords;
	public Long lastLuredByBusiness = 0L;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addCustomNBTToVillager(CallbackInfo ci, @Local CompoundTag nbt) {
        if (this.businessRecords == null) {
            nbt.put("BusinessRecords", new CompoundTag());
        } else {
            nbt.put("BusinessRecords", this.businessRecords.toNbt());
        }

        nbt.putLong("LastLuredByBusiness", this.lastLuredByBusiness);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void loadCustomNBTFromVillager(CallbackInfo ci, @Local CompoundTag nbt) {
        if (nbt.contains("BusinessRecords")) {
            this.businessRecords = new BusinessRecordList(nbt.getCompound("BusinessRecords"));
        }

        if (nbt.contains("LastLuredByBusiness")) {
            this.lastLuredByBusiness = nbt.getLong("LastLuredByBusiness");
        }
    }
}