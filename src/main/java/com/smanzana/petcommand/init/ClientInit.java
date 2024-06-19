package com.smanzana.petcommand.init;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.client.container.PetCommandContainers;
import com.smanzana.petcommand.client.petgui.PetGUI;
import com.smanzana.petcommand.client.petgui.PetGUI.PetContainer;
import com.smanzana.petcommand.client.petgui.PetGUI.PetGUIContainer;
import com.smanzana.petcommand.client.petgui.PetGUI.PetGUIContainer.PetGUIRenderHelperImpl;
import com.smanzana.petcommand.client.render.BoundIronGolemRenderer;
import com.smanzana.petcommand.entity.PetCommandEntities;
import com.smanzana.petcommand.proxy.ClientProxy;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = PetCommand.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ScreenManager.registerFactory(PetCommandContainers.PetGui, new PetGUIFactory());
		
		registerEntityRenderers();
		
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
	
	private static final void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(PetCommandEntities.BOUND_IRON_GOLEM, (manager) -> new BoundIronGolemRenderer(manager));
	}
	
}
