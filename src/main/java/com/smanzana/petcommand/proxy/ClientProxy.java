package com.smanzana.petcommand.proxy;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.pet.EPetOrderType;
import com.smanzana.petcommand.api.pet.EPetPlacementMode;
import com.smanzana.petcommand.api.pet.EPetTargetMode;
import com.smanzana.petcommand.api.pet.ITargetManager;
import com.smanzana.petcommand.client.overlay.OverlayRenderer;
import com.smanzana.petcommand.client.pet.SelectionManager;
import com.smanzana.petcommand.client.render.OutlineRenderer;
import com.smanzana.petcommand.client.render.PetOrderRenderer;
import com.smanzana.petcommand.client.screen.PetListScreen;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.network.message.PetCommandMessage;
import com.smanzana.petcommand.pet.PetOrder;
import com.smanzana.petcommand.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.petcommand.util.RayTrace;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.RawMouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientProxy extends CommonProxy {
	
	private KeyMapping bindingPetPlacementModeCycle;
	private KeyMapping bindingPetTargetModeCycle;
	private KeyMapping bindingPetAttackAll;
	private KeyMapping bindingPetAttack;
	private KeyMapping bindingPetAllStop;
	private KeyMapping bindingPetScreen;
	private KeyMapping bindingPetOrderModifier;
	private OverlayRenderer overlayRenderer;
	private OutlineRenderer outlineRenderer;
	private PetOrderRenderer petOrderRenderer;
	private SelectionManager selectionManager;
	
	public ClientProxy() {
		super();
		this.overlayRenderer = new OverlayRenderer();
		this.outlineRenderer = new OutlineRenderer();
		this.selectionManager = new SelectionManager(this.getOutlineRenderer());
		this.petOrderRenderer = new PetOrderRenderer();
		
		MinecraftForge.EVENT_BUS.register(this); // Handle keyboard inputs
	}
	
	public void initKeybinds() {
		bindingPetPlacementModeCycle = new KeyMapping("key.pet.placementmode.desc", InputConstants.UNKNOWN.getValue(), "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetPlacementModeCycle);
		bindingPetTargetModeCycle = new KeyMapping("key.pet.targetmode.desc", InputConstants.UNKNOWN.getValue(), "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetTargetModeCycle);
		bindingPetAttackAll = new KeyMapping("key.pet.attackall.desc", GLFW.GLFW_KEY_X, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetAttackAll);
		bindingPetAttack = new KeyMapping("key.pet.attack.desc", GLFW.GLFW_KEY_C, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetAttack);
		bindingPetAllStop = new KeyMapping("key.pet.stopall.desc", GLFW.GLFW_KEY_L, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetAllStop);
		bindingPetScreen = new KeyMapping("key.pet.screen.desc", GLFW.GLFW_KEY_P, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetScreen);
		bindingPetOrderModifier = new KeyMapping("key.pet.order.desc", GLFW.GLFW_KEY_LEFT_CONTROL, "key.PetCommand.desc");
		ClientRegistry.registerKeyBinding(bindingPetOrderModifier);
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
	public void openPetGUI(Player player, LivingEntity pet) {
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
	
	public void cyclePlacementMode() {
		final EPetPlacementMode current = PetCommand.GetPetCommandManager().getPlacementMode(this.getPlayer());
		final EPetPlacementMode next = EPetPlacementMode.values()[(current.ordinal() + 1) % EPetPlacementMode.values().length];
		
		// Set up client to have this locally
		PetCommand.GetPetCommandManager().setPlacementMode(getPlayer(), next);
		
		// Send change to server
		NetworkHandler.sendToServer(PetCommandMessage.AllPlacementMode(next));
	}
	
	public void cycleTargetMode() {
		// Cycle target mode
		final EPetTargetMode current = PetCommand.GetPetCommandManager().getTargetMode(this.getPlayer());
		final EPetTargetMode next = EPetTargetMode.values()[(current.ordinal() + 1) % EPetTargetMode.values().length];
		
		// Set up client to have this locally
		PetCommand.GetPetCommandManager().setTargetMode(getPlayer(), next);
		
		// Send change to server
		NetworkHandler.sendToServer(PetCommandMessage.AllTargetMode(next));
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		if (bindingPetPlacementModeCycle.consumeClick()) {
			// Cycle placement mode
			cyclePlacementMode();
			
			// Update client icon
			this.overlayRenderer.changePetPlacementIcon();
		} else if (bindingPetTargetModeCycle.consumeClick()) {
			cycleTargetMode();
			
			// Update client icon
			this.overlayRenderer.changePetTargetIcon();
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
				final Set<LivingEntity> selectedPets = this.getSelectionManager().getSelectedPets();
				if (selectedPets.isEmpty()) {
					// Try and select a pet
					HitResult result = RayTrace.raytraceApprox(
							player.level, player,
							player.getEyePosition(partialTicks),
							player.getViewVector(partialTicks),
							100, (e) -> { return e != player && tames.contains(e);},
							.1);
					if (result != null && result.getType() == HitResult.Type.ENTITY) {
						LivingEntity selectedPet = RayTrace.livingFromRaytrace(result);
						this.getSelectionManager().addPet(selectedPet);
					} else {
						// didn't hit any specific pet, so select all
						tames.forEach(p -> this.getSelectionManager().addPet(p));
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
						for (LivingEntity selectedPet : selectedPets) {
							NetworkHandler.sendToServer(PetCommandMessage.PetAttack(selectedPet, RayTrace.livingFromRaytrace(result)));
						}
					}
					
					// Clear out pet
					this.getSelectionManager().clearSelection();
				}
				
				// Readjust order renderer based on selection
				this.getPetOrderRenderer().setEnabled(!this.getSelectionManager().getSelectedPets().isEmpty());
			}
		} else if (bindingPetAllStop.consumeClick()) {
			NetworkHandler.sendToServer(PetCommandMessage.AllStop());
		} else if (bindingPetScreen.consumeClick()) {
			togglePetScreen();
		}
	}
	
	@SubscribeEvent
	public void onMouse(RawMouseEvent event) {
		if (event.isCanceled() || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_2) {
			return;
		}
		
		if (!this.isPetOrderPressed()) {
			return;
		}
		
		if (this.getSelectionManager().getSelectedPets().isEmpty()) {
			return;
		}
		
		// Process command
		event.setCanceled(true);
		this.handlePetOrder();
	}
	
	public boolean isPetOrderPressed() {
		return this.bindingPetOrderModifier.isDown();
	}
	
	public boolean isPetOrderGuard() {
		return this.getPlayer().isCrouching();
	}
	
	public OutlineRenderer getOutlineRenderer() {
		return this.outlineRenderer;
	}
	
	public PetOrderRenderer getPetOrderRenderer() {
		return this.petOrderRenderer;
	}
	
	public SelectionManager getSelectionManager() {
		return this.selectionManager;
	}
	
	protected void togglePetScreen() {
		final Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null && mc.screen instanceof PetListScreen) {
			// Close it
			mc.setScreen(null);
		} else if (mc.screen == null) {
			// Open it
			mc.setScreen(new PetListScreen(TextComponent.EMPTY, this.getPlayer()));
		}
	}
	
	protected void handlePetOrder() {
		// We have issued an order. Figure out which, where, and do it
		
		final Player player = this.getPlayer();
		final float partialTicks = Minecraft.getInstance().getFrameTime();
		HitResult result = RayTrace.raytrace(
				player.level, player,
				player.getEyePosition(partialTicks),
				player.getViewVector(partialTicks),
				100, (e) -> false);
		BlockPos pos = RayTrace.outsideBlockPosFromResult(result);
		if (pos != null) {
			final boolean isGuard = this.isPetOrderGuard();
			
			final PetOrder order;
			if (isGuard) {
				order = new PetOrder(EPetOrderType.GUARD_POS, pos);
			} else {
				order = new PetOrder(EPetOrderType.MOVE_TO_POS, pos);
			}
			
			for (LivingEntity pet : this.getSelectionManager().getSelectedPets()) {
				PetCommand.GetPetCommandManager().setPetOrder(player, pet, order);
			}
			
		}
		
		player.playSound(SoundEvents.NOTE_BLOCK_XYLOPHONE, 1f, .5f);
		player.playSound(SoundEvents.NOTE_BLOCK_XYLOPHONE, 1f, .65f);
		
		this.getSelectionManager().clearSelection();
		this.getPetOrderRenderer().setEnabled(false);
	}

}
