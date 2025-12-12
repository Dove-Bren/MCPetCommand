package com.smanzana.petcommand.client.container;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.client.petgui.PetGUI;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PetCommandContainers {
	
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, PetCommand.MODID);

	public static final RegistryObject<MenuType<?>> PetGui = REGISTRY.register(PetGUI.PetContainer.ID, () -> IForgeMenuType.create(PetGUI.PetContainer::FromNetwork));
}
