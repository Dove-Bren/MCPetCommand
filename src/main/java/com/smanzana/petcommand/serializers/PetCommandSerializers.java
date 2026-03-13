package com.smanzana.petcommand.serializers;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PetCommandSerializers {

	public static final DeferredRegister<EntityDataSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, PetCommand.MODID);
	public static final RegistryObject<PetJobSerializer> PetJob = REGISTRY.register("petcommand.serial.pet_job", () -> PetJobSerializer.GetInstance());
	
	// 1.20.6+
//	public static final RegistryObject<EntityDataSerializer<EPetAction>> PetJob = REGISTRY.register("petcommand.serial.pet_job", () -> forEnum(EPetAction.class));
//	
//	private static final <T extends Enum<T>> EntityDataSerializer<T> forEnum(Class<T> clazz) {
//		return EntityDataSerializer.forValueType(serializeEnum(clazz));
//	}
//	
//	private static final <T extends Enum<T>> StreamCodec<? super RegistryFriendlyByteBuf, T> serializeEnum(Class<T> clazz) {
//		return StreamCodec.of((s, v) -> s.writeEnum(v), (s) -> s.readEnum(clazz));
//	}
	
}
