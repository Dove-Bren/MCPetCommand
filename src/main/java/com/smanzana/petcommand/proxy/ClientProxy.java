package com.smanzana.petcommand.proxy;

import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.ITargetManager;
import com.smanzana.petcommand.api.pet.PetPlacementMode;
import com.smanzana.petcommand.api.pet.PetTargetMode;
import com.smanzana.petcommand.client.overlay.OverlayRenderer;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.network.message.PetCommandMessage;
import com.smanzana.petcommand.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.petcommand.util.RayTrace;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
	
	private KeyMapping bindingPetPlacementModeCycle;
	private KeyMapping bindingPetTargetModeCycle;
	private KeyMapping bindingPetAttackAll;
	private KeyMapping bindingPetAttack;
	private KeyMapping bindingPetAllStop;
	private OverlayRenderer overlayRenderer;
	
	private @Nullable LivingEntity selectedPet; // Used for directing pets to do actions on key releases
	
	public ClientProxy() {
		super();
		this.overlayRenderer = new OverlayRenderer();
		
		MinecraftForge.EVENT_BUS.register(this); // Handle keyboard inputs
	}
	
	public void initKeybinds() {
		bindingPetPlacementModeCycle = new KeyMapping("key.pet.placementmode.desc", GLFW.GLFW_KEY_G, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetPlacementModeCycle);
		bindingPetTargetModeCycle = new KeyMapping("key.pet.targetmode.desc", GLFW.GLFW_KEY_H, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetTargetModeCycle);
		bindingPetAttackAll = new KeyMapping("key.pet.attackall.desc", GLFW.GLFW_KEY_X, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetAttackAll);
		bindingPetAttack = new KeyMapping("key.pet.attack.desc", GLFW.GLFW_KEY_C, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetAttack);
		bindingPetAllStop = new KeyMapping("key.pet.stopall.desc", GLFW.GLFW_KEY_L, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetAllStop);
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public @Nullable Player getPlayer() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	@Override
	public void openContainer(Player player, IPackedContainerProvider provider) {
		if (!player.level.isClientSide) {
			super.openContainer(player, provider);
		}
		; // On client, do nothing
	}
	
	@Override
	public void openPetGUI(Player player, IEntityPet pet) {
		// Integrated clients still need to open the gui...
		//if (!player.world.isRemote) {
//			DragonContainer container = dragon.getGUIContainer();
//			DragonGUI gui = new DragonGUI(container);
//			FMLCommonHandler.instance().showGuiScreen(gui);
			super.openPetGUI(player, pet);
		//}
	}
	
	@Override
	public ITargetManager getTargetManager(LivingEntity entity) {
		if (entity.getCommandSenderWorld().isClientSide()) {
			return PetCommand.GetClientTargetManager();
		}
		return super.getTargetManager(entity);
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		if (bindingPetPlacementModeCycle.consumeClick()) {
			// Cycle placement mode
			final PetPlacementMode current = PetCommand.GetPetCommandManager().getPlacementMode(this.getPlayer());
			final PetPlacementMode next = PetPlacementMode.values()[(current.ordinal() + 1) % PetPlacementMode.values().length];
			
			// Set up client to have this locally
			PetCommand.GetPetCommandManager().setPlacementMode(getPlayer(), next);
			
			// Update client icon
			this.overlayRenderer.changePetPlacementIcon();
			
			// Send change to server
			NetworkHandler.sendToServer(PetCommandMessage.AllPlacementMode(next));
		} else if (bindingPetTargetModeCycle.consumeClick()) {
			// Cycle target mode
			final PetTargetMode current = PetCommand.GetPetCommandManager().getTargetMode(this.getPlayer());
			final PetTargetMode next = PetTargetMode.values()[(current.ordinal() + 1) % PetTargetMode.values().length];
			
			// Update client icon
			this.overlayRenderer.changePetTargetIcon();
			
			// Set up client to have this locally
			PetCommand.GetPetCommandManager().setTargetMode(getPlayer(), next);
			
			// Send change to server
			NetworkHandler.sendToServer(PetCommandMessage.AllTargetMode(next));
		} else if (bindingPetAttackAll.consumeClick()) {
			// Raytrace, find tar get, and set all to attack
			final Player player = getPlayer();
			if (player != null && player.level != null) {
				final float partialTicks = Minecraft.getInstance().getFrameTime();
				final List<LivingEntity> tames = PetFuncs.GetTamedEntities(player);
				HitResult result = RayTrace.raytraceApprox(
						player.level, player,
						player.getEyePosition(partialTicks),
						player.getViewVector(partialTicks),
						100, (e) -> { return e != player && e instanceof LivingEntity && !player.isAlliedTo(e) && !tames.contains(e);},
						1);
				if (result != null && result.getType() == HitResult.Type.ENTITY) {
					NetworkHandler.sendToServer(PetCommandMessage.AllAttack(RayTrace.livingFromRaytrace(result)));
				}
			}
		} else if (bindingPetAttack.consumeClick()) {
			// Raytrace, find target, and then make single one attack
			// Probably could be same button but if raytrace is our pet,
			// have them hold it down and release on an enemy? Or 'select' them
			// and have them press again to select enemy?
			final Player player = getPlayer();
			if (player != null && player.level != null) {
				final float partialTicks = Minecraft.getInstance().getFrameTime();
				final List<LivingEntity> tames = PetFuncs.GetTamedEntities(player);
				if (selectedPet == null) {
					// Try and select a pet
					HitResult result = RayTrace.raytraceApprox(
							player.level, player,
							player.getEyePosition(partialTicks),
							player.getViewVector(partialTicks),
							100, (e) -> { return e != player && tames.contains(e);},
							.1);
					if (result != null && result.getType() == HitResult.Type.ENTITY) {
						selectedPet = RayTrace.livingFromRaytrace(result);
						if (selectedPet.level.isClientSide) {
							selectedPet.setGlowingTag(true);
						}
					}
				} else {
					// Find target
					HitResult result = RayTrace.raytraceApprox(
							player.level, player,
							player.getEyePosition(partialTicks),
							player.getViewVector(partialTicks),
							100, (e) -> { return e != player && e instanceof LivingEntity && !player.isAlliedTo(e) && !tames.contains(e);},
							1);
					if (result != null && result.getType() == HitResult.Type.ENTITY) {
						NetworkHandler.sendToServer(PetCommandMessage.PetAttack(selectedPet, RayTrace.livingFromRaytrace(result)));
					}
					
					// Clear out pet
					if (selectedPet.level.isClientSide) {
						selectedPet.setGlowingTag(false);
					}
					selectedPet = null;
				}
			}
		} else if (bindingPetAllStop.consumeClick()) {
			NetworkHandler.sendToServer(PetCommandMessage.AllStop());
		}
	}
	
	public @Nullable LivingEntity getCurrentPet() {
		return this.selectedPet;
	}

}
