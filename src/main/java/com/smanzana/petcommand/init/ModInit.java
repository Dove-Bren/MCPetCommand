package com.smanzana.petcommand.init;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.serializers.PetJobSerializer;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = PetCommand.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModInit {

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		NetworkHandler.getInstance();
	}
	
	@SubscribeEvent
    public static void registerDataSerializers(RegistryEvent.Register<DataSerializerEntry> event) {
		final IForgeRegistry<DataSerializerEntry> registry = event.getRegistry();
		
		registry.register(new DataSerializerEntry(PetJobSerializer.instance).setRegistryName("nostrum.serial.pet_job"));
	}
	
}
