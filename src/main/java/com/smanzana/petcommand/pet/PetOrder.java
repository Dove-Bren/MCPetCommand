package com.smanzana.petcommand.pet;

import javax.annotation.Nullable;

import com.smanzana.petcommand.api.pet.EPetOrderType;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

/**
 * Coupling of order type + its data
 */
public final record PetOrder(EPetOrderType type, @Nullable BlockPos pos) {
	
	private static final String NBT_TYPE = "type";
	private static final String NBT_POS = "pos";
	
	public PetOrder {
		
	}
	
	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		
		tag.putString(NBT_TYPE, this.type().name().toLowerCase());
		if (pos != null) {
			tag.put(NBT_POS, NbtUtils.writeBlockPos(pos));
		}
		
		return tag;
	}
	
	public static PetOrder FromNBT(CompoundTag nbt) {
		EPetOrderType type;
		BlockPos pos = null;
		
		try {
			type = EPetOrderType.valueOf(nbt.getString(NBT_TYPE).toUpperCase());
		} catch (Exception e) {
			type = EPetOrderType.MOVE_TO_ME;
		}
		
		if (nbt.contains(NBT_POS)) {
			pos = NbtUtils.readBlockPos(nbt.getCompound(NBT_POS));
		}
		
		return new PetOrder(type, pos);
	}
	
}
