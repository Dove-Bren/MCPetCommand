package com.smanzana.petcommand.serializers;

import com.smanzana.petcommand.api.pet.EPetAction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class PetJobSerializer implements EntityDataSerializer<EPetAction> {
	
	private static PetJobSerializer instance = new PetJobSerializer();
	public static PetJobSerializer GetInstance() {
		return instance;
	}
	
	private PetJobSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, EPetAction value) {
		buf.writeEnum(value);
	}

	@Override
	public EPetAction read(FriendlyByteBuf buf)  {
		return buf.readEnum(EPetAction.class);
	}

	@Override
	public EPetAction copy(EPetAction value) {
		return value;
	}
}