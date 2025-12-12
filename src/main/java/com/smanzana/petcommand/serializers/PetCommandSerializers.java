package com.smanzana.petcommand.serializers;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PetCommandSerializers {

	public static final DeferredRegister<EntityDataSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, PetCommand.MODID);
	
	public static final RegistryObject<PetJobSerializer> PetJob = REGISTRY.register("petcommand.serial.pet_job", () -> PetJobSerializer.GetInstance());
	
}
