package com.smanzana.petcommand.client.container;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.client.petgui.PetGUI;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = PetCommand.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(PetCommand.MODID)
public class PetCommandContainers {

	@ObjectHolder(PetGUI.PetContainer.ID) public static ContainerType<PetGUI.PetContainer<?>> PetGui;
	
	@SubscribeEvent
	public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
		final IForgeRegistry<ContainerType<?>> registry = event.getRegistry();
		
		registry.register(IForgeContainerType.create(PetGUI.PetContainer::FromNetwork).setRegistryName(PetGUI.PetContainer.ID));
	}
}
