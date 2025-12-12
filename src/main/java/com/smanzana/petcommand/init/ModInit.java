package com.smanzana.petcommand.init;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.client.container.PetCommandContainers;
import com.smanzana.petcommand.entity.PetCommandEntities;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.proxy.PetCommandAPIImpl;
import com.smanzana.petcommand.proxy.PetCommandClientAPIImpl;
import com.smanzana.petcommand.serializers.PetCommandSerializers;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = PetCommand.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModInit {

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		NetworkHandler.getInstance();
		DistExecutor.safeRunForDist(() -> PetCommandClientAPIImpl::Register, () -> PetCommandAPIImpl::Register);
	}
	
	public static final void addRegistries(FMLJavaModLoadingContext context) {
		// Registries
		final IEventBus modBus = context.getModEventBus();
		PetCommandContainers.REGISTRY.register(modBus);
		PetCommandEntities.REGISTRY.register(modBus);
		PetCommandSerializers.REGISTRY.register(modBus);
	}
}
