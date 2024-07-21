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

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = PetCommand.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		MenuScreens.register(PetCommandContainers.PetGui, new PetGUIFactory());
		
		ClientProxy proxy = (ClientProxy) PetCommand.GetProxy();
		proxy.initKeybinds();
		
		PetGUIRenderHelperImpl.Register();
	}
	
	@SubscribeEvent
	public static void registerEntityRenderers(RegisterRenderers event) {
		event.registerEntityRenderer(PetCommandEntities.BOUND_IRON_GOLEM, (manager) -> new BoundIronGolemRenderer(manager));
	}
	
	// To get around bounds matching. D:
	protected static class PetGUIFactory<T extends IEntityPet> implements ScreenConstructor<PetGUI.PetContainer<T>, PetGUI.PetGUIContainer<T>> {

			@Override
			public PetGUIContainer<T> create(PetContainer<T> c, Inventory p,
					Component n) {
				return new PetGUI.PetGUIContainer<T>(c, p, n);
			}
	}
}
