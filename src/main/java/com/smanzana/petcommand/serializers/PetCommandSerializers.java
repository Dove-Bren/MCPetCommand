package com.smanzana.petcommand.serializers;

import com.smanzana.petcommand.PetCommand;

import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PetCommandSerializers {

	public static final DeferredRegister<DataSerializerEntry> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.DATA_SERIALIZERS, PetCommand.MODID);
	
	public static final RegistryObject<DataSerializerEntry> PetJob = REGISTRY.register("petcommand.serial.pet_job", () -> new DataSerializerEntry(PetJobSerializer.GetInstance()));
	
}
