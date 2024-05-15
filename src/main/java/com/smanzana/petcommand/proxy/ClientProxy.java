package com.smanzana.petcommand.proxy;

import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.client.overlay.OverlayRenderer;
import com.smanzana.petcommand.entity.IEntityPet;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.network.message.PetCommandMessage;
import com.smanzana.petcommand.pet.PetPlacementMode;
import com.smanzana.petcommand.pet.PetTargetMode;
import com.smanzana.petcommand.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.petcommand.util.RayTrace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
	
	private KeyBinding bindingPetPlacementModeCycle;
	private KeyBinding bindingPetTargetModeCycle;
	private KeyBinding bindingPetAttackAll;
	private KeyBinding bindingPetAttack;
	private KeyBinding bindingPetAllStop;
	private OverlayRenderer overlayRenderer;
	
	private @Nullable LivingEntity selectedPet; // Used for directing pets to do actions on key releases
	
	public ClientProxy() {
		super();
		this.overlayRenderer = new OverlayRenderer();
		
		MinecraftForge.EVENT_BUS.register(this); // Handle keyboard inputs
	}
	
	public void initKeybinds() {
		bindingPetPlacementModeCycle = new KeyBinding("key.pet.placementmode.desc", GLFW.GLFW_KEY_G, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetPlacementModeCycle);
		bindingPetTargetModeCycle = new KeyBinding("key.pet.targetmode.desc", GLFW.GLFW_KEY_H, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetTargetModeCycle);
		bindingPetAttackAll = new KeyBinding("key.pet.attackall.desc", GLFW.GLFW_KEY_X, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetAttackAll);
		bindingPetAttack = new KeyBinding("key.pet.attack.desc", GLFW.GLFW_KEY_C, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetAttack);
		bindingPetAllStop = new KeyBinding("key.pet.stopall.desc", GLFW.GLFW_KEY_L, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetAllStop);
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public @Nullable PlayerEntity getPlayer() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	@Override
	public void openContainer(PlayerEntity player, IPackedContainerProvider provider) {
		if (!player.world.isRemote) {
			super.openContainer(player, provider);
		}
		; // On client, do nothing
	}
	
	@Override
	public void openPetGUI(PlayerEntity player, IEntityPet pet) {
		// Integrated clients still need to open the gui...
		//if (!player.world.isRemote) {
//			DragonContainer container = dragon.getGUIContainer();
//			DragonGUI gui = new DragonGUI(container);
//			FMLCommonHandler.instance().showGuiScreen(gui);
			super.openPetGUI(player, pet);
		//}
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		if (bindingPetPlacementModeCycle.isPressed()) {
			// Cycle placement mode
			final PetPlacementMode current = PetCommand.GetPetCommandManager().getPlacementMode(this.getPlayer());
			final PetPlacementMode next = PetPlacementMode.values()[(current.ordinal() + 1) % PetPlacementMode.values().length];
			
			// Set up client to have this locally
			PetCommand.GetPetCommandManager().setPlacementMode(getPlayer(), next);
			
			// Update client icon
			this.overlayRenderer.changePetPlacementIcon();
			
			// Send change to server
			NetworkHandler.sendToServer(PetCommandMessage.AllPlacementMode(next));
		} else if (bindingPetTargetModeCycle.isPressed()) {
			// Cycle target mode
			final PetTargetMode current = PetCommand.GetPetCommandManager().getTargetMode(this.getPlayer());
			final PetTargetMode next = PetTargetMode.values()[(current.ordinal() + 1) % PetTargetMode.values().length];
			
			// Update client icon
			this.overlayRenderer.changePetTargetIcon();
			
			// Set up client to have this locally
			PetCommand.GetPetCommandManager().setTargetMode(getPlayer(), next);
			
			// Send change to server
			NetworkHandler.sendToServer(PetCommandMessage.AllTargetMode(next));
		} else if (bindingPetAttackAll.isPressed()) {
			// Raytrace, find tar get, and set all to attack
			final PlayerEntity player = getPlayer();
			if (player != null && player.world != null) {
				final float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
				final List<LivingEntity> tames = PetCommand.GetTamedEntities(player);
				RayTraceResult result = RayTrace.raytraceApprox(
						player.world, player,
						player.getEyePosition(partialTicks),
						player.getLook(partialTicks),
						100, (e) -> { return e != player && e instanceof LivingEntity && !player.isOnSameTeam(e) && !tames.contains(e);},
						1);
				if (result != null && result.getType() == RayTraceResult.Type.ENTITY) {
					NetworkHandler.sendToServer(PetCommandMessage.AllAttack(RayTrace.livingFromRaytrace(result)));
				}
			}
		} else if (bindingPetAttack.isPressed()) {
			// Raytrace, find target, and then make single one attack
			// Probably could be same button but if raytrace is our pet,
			// have them hold it down and release on an enemy? Or 'select' them
			// and have them press again to select enemy?
			final PlayerEntity player = getPlayer();
			if (player != null && player.world != null) {
				final float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
				final List<LivingEntity> tames = PetCommand.GetTamedEntities(player);
				if (selectedPet == null) {
					// Try and select a pet
					RayTraceResult result = RayTrace.raytraceApprox(
							player.world, player,
							player.getEyePosition(partialTicks),
							player.getLook(partialTicks),
							100, (e) -> { return e != player && tames.contains(e);},
							.1);
					if (result != null && result.getType() == RayTraceResult.Type.ENTITY) {
						selectedPet = RayTrace.livingFromRaytrace(result);
						if (selectedPet.world.isRemote) {
							selectedPet.setGlowing(true);
						}
					}
				} else {
					// Find target
					RayTraceResult result = RayTrace.raytraceApprox(
							player.world, player,
							player.getEyePosition(partialTicks),
							player.getLook(partialTicks),
							100, (e) -> { return e != player && e instanceof LivingEntity && !player.isOnSameTeam(e) && !tames.contains(e);},
							1);
					if (result != null && result.getType() == RayTraceResult.Type.ENTITY) {
						NetworkHandler.sendToServer(PetCommandMessage.PetAttack(selectedPet, RayTrace.livingFromRaytrace(result)));
					}
					
					// Clear out pet
					if (selectedPet.world.isRemote) {
						selectedPet.setGlowing(false);
					}
					selectedPet = null;
				}
			}
		} else if (bindingPetAllStop.isPressed()) {
			NetworkHandler.sendToServer(PetCommandMessage.AllStop());
		}
	}
	
	public @Nullable LivingEntity getCurrentPet() {
		return this.selectedPet;
	}

}
