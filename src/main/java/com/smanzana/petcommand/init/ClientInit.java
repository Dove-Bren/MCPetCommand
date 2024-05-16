package com.smanzana.petcommand.init;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.client.container.PetCommandContainers;
import com.smanzana.petcommand.client.container.PetGUI;
import com.smanzana.petcommand.client.container.PetGUI.PetContainer;
import com.smanzana.petcommand.client.container.PetGUI.PetGUIContainer;
import com.smanzana.petcommand.client.container.PetGUI.PetGUIContainer.PetGUIRenderHelperImpl;
import com.smanzana.petcommand.proxy.ClientProxy;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = PetCommand.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientInit {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ScreenManager.registerFactory(PetCommandContainers.PetGui, new PetGUIFactory());
		
		ClientProxy proxy = (ClientProxy) PetCommand.GetProxy();
		proxy.initKeybinds();
		
		PetGUIRenderHelperImpl.Register();
	}
	
	// To get around bounds matching. D:
	protected static class PetGUIFactory<T extends IEntityPet> implements ScreenManager.IScreenFactory<PetGUI.PetContainer<T>, PetGUI.PetGUIContainer<T>> {

			@Override
			public PetGUIContainer<T> create(PetContainer<T> c, PlayerInventory p,
					ITextComponent n) {
				return new PetGUI.PetGUIContainer<T>(c, p, n);
			}
	}
	
}
