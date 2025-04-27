package com.smanzana.petcommand.client.overlay;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.EPetAction;
import com.smanzana.petcommand.api.pet.EPetPlacementMode;
import com.smanzana.petcommand.api.pet.EPetTargetMode;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.PetValue;
import com.smanzana.petcommand.api.pet.PetInfo.ValueFlavor;
import com.smanzana.petcommand.client.icon.PetActionIcon;
import com.smanzana.petcommand.client.icon.PetPlacementModeIcon;
import com.smanzana.petcommand.client.icon.PetTargetModeIcon;
import com.smanzana.petcommand.client.render.PetCommandRenderTypes;
import com.smanzana.petcommand.config.ModConfig;
import com.smanzana.petcommand.proxy.ClientProxy;
import com.smanzana.petcommand.util.ColorUtil;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends GuiComponent {

	private static final ResourceLocation GUI_HEALTHBARS = new ResourceLocation(PetCommand.MODID, "textures/gui/healthbars.png");
	private static final int GUI_HEALTHBAR_ORB_BACK_WIDTH = 205;
	private static final int GUI_HEALTHBAR_ORB_BACK_HEIGHT = 56;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_BAR_HOFFSET = 2;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_BAR_VOFFSET = 112;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_HOFFSET = 2;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_VOFFSET = 29;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_WIDTH = 152;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_HEIGHT = 18;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_BAR_HOFFSET = 61;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_BAR_VOFFSET = 129;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_HOFFSET = 61;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_VOFFSET = 45;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_WIDTH = 105;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_HEIGHT = 8;
	private static final int GUI_HEALTHBAR_ORB_ENTITY_HOFFSET = 177;
	private static final int GUI_HEALTHBAR_ORB_ENTITY_VOFFSET = 45;
	private static final int GUI_HEALTHBAR_ORB_ENTITY_WIDTH = 12;
	private static final int GUI_HEALTHBAR_ORB_NAME_WIDTH = 160;
	private static final int GUI_HEALTHBAR_ORB_NAME_HEIGHT = 30;
	private static final int GUI_HEALTHBAR_ORB_NAME_HOFFSET = 20;
	private static final int GUI_HEALTHBAR_ORB_NAME_VOFFSET = 12;
	
	private static final int GUI_HEALTHBAR_BOX_BACK_WIDTH = 191;
	private static final int GUI_HEALTHBAR_BOX_BACK_HEIGHT = 25;
	private static final int GUI_HEALTHBAR_BOX_BACK_VOFFSET = 140;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_BAR_HOFFSET = 2;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_BAR_VOFFSET = 191;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_HOFFSET = 2;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_VOFFSET = 1;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_WIDTH = 165;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_HEIGHT = 18;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_BAR_HOFFSET = 61;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_BAR_VOFFSET = 211;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_HOFFSET = 62;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_VOFFSET = 17;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_WIDTH = 104;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_HEIGHT = 8;
	
	private static final int GUI_HEALTHBAR_ICON_LENGTH = 32;
	private static final int GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET = 300;
	private static final int GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET = 50;
	
	public static final ResourceLocation GUI_PET_ICONS = new ResourceLocation(PetCommand.MODID, "textures/gui/pet_icons.png");
	//private static final int GUI_PET_ICONS_DIMS = 256;
	private static final int GUI_PET_ICON_DIMS = 32;
	private static final int GUI_PET_ICON_TARGET_VOFFSET = 0;
	private static final int GUI_PET_ICON_PLACEMENT_VOFFSET = GUI_PET_ICON_TARGET_VOFFSET + GUI_PET_ICON_DIMS;
	private static final int GUI_PET_ICON_FLOATTARGET_HOFFSET = 0;
	private static final int GUI_PET_ICON_FLOATTARGET_VOFFSET = GUI_PET_ICON_PLACEMENT_VOFFSET + GUI_PET_ICON_DIMS;
	
	private int petTargetIndex; // Controls displaying pet target icon (fade in/out 50%)
	private int petTargetAnimDur = 80;
	private int petPlacementIndex; // Controls displaying pet placement icon (fade in/out at 50%)
	private int petPlacementAnimDur = 80;
	
	protected final IIngameOverlay healthbarOverlay;
	protected final IIngameOverlay modeIconOverlay;
	
	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		petTargetIndex = -1;
		petPlacementIndex = -1;
		
		healthbarOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "PetCommand::healthbarOverlay", this::renderHealthbarOverlay);
		modeIconOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CROSSHAIR_ELEMENT, "PetCommand::modeOverlay", this::renderModeOverlay);
	}
	
	@SubscribeEvent
	public void onWorldRender(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
		List<Mob> targeters = PetCommandAPI.GetTargetManager(event.getEntity()).getEntitiesTargetting(event.getEntity());
		if (!targeters.isEmpty()) {
			int i = 0;
			final PoseStack matrixStackIn = event.getPoseStack();
			final Minecraft mc = Minecraft.getInstance();
			final Camera activeRenderInfo = mc.getEntityRenderDispatcher().camera;
			final VertexConsumer buffer = event.getMultiBufferSource().getBuffer(PetCommandRenderTypes.getPetTargetIcon()); // Could only grab this when rendering at least one?
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, event.getEntity().getBbHeight() + .15f, 0);
			matrixStackIn.mulPose(activeRenderInfo.rotation());
			matrixStackIn.translate(-event.getEntity().getBbWidth()/2f, 0, 0);
			for (Mob targeter : targeters) {
				@Nullable LivingEntity owner = PetFuncs.GetOwner(targeter);
				if (owner != null && owner == mc.player) {
					matrixStackIn.translate(0, .25f /* *i*/, 0);
					renderPetTargetIcon(matrixStackIn, buffer, targeter, i++, event.getPartialTick());
				}
			}
			matrixStackIn.popPose();
		}
	}
	
	private void renderHealthbarOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		if (ModConfig.config.showHealthbars()) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			final float scale = 0.5f;
			int y = ModConfig.config.getHealthbarAnchorY();
			int healthbarWidth;
			int healthbarHeight;
			int xOffset;
			int xConfigOffset = ModConfig.config.getHealthbarAnchorX();
			
			// Bigs
			if (ModConfig.config.showBigHealthbars()) {
				healthbarWidth = (int) (GUI_HEALTHBAR_ORB_BACK_WIDTH * scale);
				healthbarHeight = (int) (GUI_HEALTHBAR_ORB_BACK_HEIGHT * scale);
				if (xConfigOffset >= 0) {
					xOffset = xConfigOffset;
				} else {
					xOffset = width - (-xConfigOffset + healthbarWidth);
				}
				
				List<LivingEntity> bigPets = PetFuncs.GetTamedEntities(player, (ent) -> {
					return ent != null && ent instanceof IEntityPet big && big.isBigPet() && !IsHiddenFromHUD(ent);
				});
				Collections.sort(bigPets, (left, right) -> {
					return ((LivingEntity) (left)).getUUID().compareTo(((LivingEntity) right).getUUID());
				});
				for (LivingEntity bigPet: bigPets) {
					final boolean isSelected = ((ClientProxy) PetCommand.GetProxy()).getSelectionManager().isSelected(bigPet);
					renderHealthbarOrb(matrixStackIn, player, width, height, bigPet, isSelected, xOffset, y, scale);
					y += healthbarHeight + 2;
				}
			}
			
			// Regulars/all
			healthbarWidth = (int) (GUI_HEALTHBAR_BOX_BACK_WIDTH * scale);
			healthbarHeight = (int) (GUI_HEALTHBAR_BOX_BACK_HEIGHT * scale);
			if (xConfigOffset >= 0) {
				xOffset = xConfigOffset;
			} else {
				xOffset = width - (-xConfigOffset + healthbarWidth);
			}
			final boolean hideBigs = ModConfig.config.showBigHealthbars();
			for (LivingEntity tamed : PetFuncs.GetTamedEntities(player)) {
				if (hideBigs
						&& tamed instanceof IEntityPet
						&& ((IEntityPet) tamed).isBigPet()) {
					continue;
				}
				if (IsHiddenFromHUD(tamed)) {
					continue;
				}
				final boolean isSelected = ((ClientProxy) PetCommand.GetProxy()).getSelectionManager().isSelected(tamed);
				renderHealthbarBox(matrixStackIn, player, width, height, tamed, isSelected, xOffset, y, scale);
				y += healthbarHeight;
			}
		}
	}
	
	private void renderModeOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		final float ticks = player.tickCount + partialTicks;
		if (petTargetIndex >= 0) {
			EPetTargetMode mode = PetCommand.GetPetCommandManager().getTargetMode(player);
			renderPetActionTargetMode(matrixStackIn, player, width, height, mode, (ticks - petTargetIndex) / (float) petTargetAnimDur);
			
			if (ticks >= petTargetIndex + petTargetAnimDur) {
				petTargetIndex = -1;
			}
		}
		
		if (petPlacementIndex >= 0) {
			EPetPlacementMode mode = PetCommand.GetPetCommandManager().getPlacementMode(player);
			renderPetActionPlacementMode(matrixStackIn, player, width, height, mode, (ticks - petPlacementIndex) / (float) petPlacementAnimDur);
			
			if (ticks >= petPlacementIndex + petPlacementAnimDur) {
				petPlacementIndex = -1;
			}
		}
	}

	private void renderPetActionTargetMode(PoseStack matrixStackIn, LocalPlayer player, int width, int height, EPetTargetMode mode, float prog) {
		final float alpha;
		if (prog < .2f) {
			alpha = prog / .2f;
		} else if (prog >= .8f) {
			alpha = (1f-prog) / .2f;
		} else {
			alpha = 1f;
		}
		
		matrixStackIn.pushPose();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		matrixStackIn.translate(width / 2, height / 2, 0);
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.translate(1, 1, 0);
		
		PetTargetModeIcon.get(mode).draw(matrixStackIn, 0, 0, GUI_PET_ICON_DIMS, GUI_PET_ICON_DIMS, 1f, 1f, 1f, alpha * .6f);
		
		RenderSystem.disableBlend();
		matrixStackIn.popPose();
	}
	
	private void renderPetActionPlacementMode(PoseStack matrixStackIn, LocalPlayer player, int width, int height, EPetPlacementMode mode, float prog) {
		final float alpha;
		if (prog < .2f) {
			alpha = prog / .2f;
		} else if (prog >= .8f) {
			alpha = (1f-prog) / .2f;
		} else {
			alpha = 1f;
		}
		
		matrixStackIn.pushPose();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		matrixStackIn.translate(width / 2, height / 2, 0);
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.translate(-(GUI_PET_ICON_DIMS + 1), 1, 0);
		PetPlacementModeIcon.get(mode).draw(matrixStackIn, 0, 0, GUI_PET_ICON_DIMS, GUI_PET_ICON_DIMS, 1f, 1f, 1f, alpha * .6f);
		
		RenderSystem.disableBlend();
		matrixStackIn.popPose();
	}
	
	private void renderHealthbarOrb(PoseStack matrixStackIn, LocalPlayer player, int width, int height, LivingEntity pet, boolean isSelected, int xoffset, int yoffset, float scale) {
		Minecraft mc = Minecraft.getInstance();
		
		// Render back, scaled bar + middle 'goods', and then foreground. Easy.
		// For center, render:
		// 1) healthbar
		// 2) pet head/icon
		// 3) pet status icon
		Font fonter = mc.font;
//		final boolean sitting = (pet instanceof EntityTameable ? ((EntityTameable) pet).isSitting()
//				: pet instanceof IEntityTameable ? ((IEntityTameable) pet).isSitting()
//				: false);
//		final boolean attacking = (pet instanceof Mob ? ((Mob) pet).getAttackTarget() != null : false);
//		final float health = (float) (Math.max(0, Math.ceil(pet.getHealth())) / Math.max(0.01, Math.ceil(pet.getMaxHealth())));
//		boolean hasSecondaryBar = false;
//		float secondaryMeter = 0f;
//		
//		if (pet instanceof ITameDragon) {
//			ITameDragon dragon = (ITameDragon) pet;
//			hasSecondaryBar = true;
//			secondaryMeter = (float) dragon.getXP() / (float) dragon.getMaxXP();
//		}
		
		PetInfo info = PetInfo.Wrap(pet);
		
		final PetValue valueToDisplay = GetPetValue(pet);
		
		final float health = (float) info.getHpPercent();//(float) (Math.max(0, Math.ceil(pet.getHealth())) / Math.max(0.01, Math.ceil(pet.getMaxHealth())));
		final boolean hasSecondaryBar = valueToDisplay.max() > 0;
		float secondaryMeter = (float) (valueToDisplay.current() / valueToDisplay.max());
		final ValueFlavor flavor = valueToDisplay.flavor();
		final EPetAction action = info.getPetAction();
		final float[] petColor = GetPetColor((Mob) pet);
		
		info.release();
		info = null;
		
		RenderSystem.setShaderTexture(0, GUI_HEALTHBARS);
		
		matrixStackIn.pushPose();
		
		matrixStackIn.translate(xoffset, yoffset, 0);
		matrixStackIn.scale(scale, scale, 1);
		
		RenderSystem.enableBlend();
		
		// Draw selected outline
		if (isSelected) {
			final double period = 1000;
			final float wiggleRadius = 3;
			final double wiggleProg = ((double) (System.currentTimeMillis() % (long) period) / period);
			final float wiggleMod = (float) Math.sin(wiggleProg * Math.PI * 2) * wiggleRadius;
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(-20 + (int)(wiggleMod), 0, -101);
			RenderSystem.setShaderColor(1f, 1f, 1f, .5f);
			
			this.fillGradient(matrixStackIn, GUI_HEALTHBAR_ORB_NAME_HOFFSET, GUI_HEALTHBAR_ORB_NAME_VOFFSET,
					GUI_HEALTHBAR_ORB_NAME_WIDTH, GUI_HEALTHBAR_ORB_NAME_HEIGHT,
					0x50000000, 0xA0000000); //nameplate background
			RenderSystem.setShaderColor(1f, 1f, 1f, .5f);
			blit(matrixStackIn, 0, 0,
					0, GUI_HEALTHBAR_ORB_BACK_HEIGHT, GUI_HEALTHBAR_ORB_BACK_WIDTH, GUI_HEALTHBAR_ORB_BACK_HEIGHT);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			matrixStackIn.popPose();
		}
		
		// Draw background
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 0, -100);
		this.fillGradient(matrixStackIn, GUI_HEALTHBAR_ORB_NAME_HOFFSET, GUI_HEALTHBAR_ORB_NAME_VOFFSET,
				GUI_HEALTHBAR_ORB_NAME_WIDTH, GUI_HEALTHBAR_ORB_NAME_HEIGHT,
				0x50000000, 0xA0000000); //nameplate background
		RenderSystem.setShaderColor(petColor[0], petColor[1], petColor[2], petColor[3]);
		blit(matrixStackIn, 0, 0,
				0, GUI_HEALTHBAR_ORB_BACK_HEIGHT, GUI_HEALTHBAR_ORB_BACK_WIDTH, GUI_HEALTHBAR_ORB_BACK_HEIGHT);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		matrixStackIn.popPose();
		
		// Draw middle
		matrixStackIn.pushPose();
		// 	-> Health bar
		blit(matrixStackIn,
				GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_ORB_HEALTH_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_ORB_HEALTH_BAR_VOFFSET,
				GUI_HEALTHBAR_ORB_HEALTH_WIDTH - Math.round(GUI_HEALTHBAR_ORB_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_ORB_HEALTH_HEIGHT);
		//	-> Secondary bar
		{
			final float[] color;
			if (!hasSecondaryBar) {
				color = new float[] {.7f, .9f, .7f, 1f};
				secondaryMeter = 1f;
			} else {
				color = new float[] {flavor.colorR(secondaryMeter),
						flavor.colorG(secondaryMeter),
						flavor.colorB(secondaryMeter),
						flavor.colorA(secondaryMeter)};
			}
			
			RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
			blit(matrixStackIn, 
					GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_VOFFSET,
					GUI_HEALTHBAR_ORB_SECONDARY_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_ORB_SECONDARY_BAR_VOFFSET,
					GUI_HEALTHBAR_ORB_SECONDARY_WIDTH - Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_ORB_SECONDARY_HEIGHT);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	
		//	-> Icon
		matrixStackIn.pushPose();
		matrixStackIn.translate(GUI_HEALTHBAR_ORB_ENTITY_HOFFSET, GUI_HEALTHBAR_ORB_ENTITY_VOFFSET, 0);
		//matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-30f));
		RenderSystem.getModelViewStack().pushPose();
		RenderSystem.getModelViewStack().mulPoseMatrix(matrixStackIn.last().pose());
		InventoryScreen.renderEntityInInventory(0, 0, GUI_HEALTHBAR_ORB_ENTITY_WIDTH, width/2, -20, pet);
		RenderSystem.getModelViewStack().popPose();
		RenderSystem.applyModelViewMatrix();
		matrixStackIn.popPose();
		RenderSystem.setShaderTexture(0, GUI_HEALTHBARS);
		
		//	-> Status
		matrixStackIn.translate(0, 0, 100);
		matrixStackIn.pushPose();
		matrixStackIn.scale(.6f, .6f, .6f);
		matrixStackIn.translate(0, 0, 0);
//		if (action == PetAction.ATTACK) {
//			blit(matrixStackIn, GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
//					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_ATTACK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
//		} else if (action == PetAction.STAY) {
//			blit(matrixStackIn, GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
//					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_STAY_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
//		} else if (action == PetAction.WORK) {
//			blit(matrixStackIn, GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
//					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_WORK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
//		}
		PetActionIcon.get(action).draw(matrixStackIn, GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
				GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		matrixStackIn.popPose();
		
		//	-> Name
		final String name = pet.hasCustomName() ? pet.getCustomName().getString() : pet.getName().getString();
		final int nameLen = fonter.width(name);
		//final float fontScale = (1f/scale) * .6f;
		final float fontScale = scale * 2.4f;
		matrixStackIn.pushPose();
		matrixStackIn.scale(fontScale, fontScale, fontScale);
		fonter.draw(matrixStackIn, name, 123 - (nameLen), 25 - (fonter.lineHeight + 2), 0xFFFFFFFF);
		RenderSystem.setShaderTexture(0, GUI_HEALTHBARS);
		matrixStackIn.popPose();
		
		matrixStackIn.popPose();
		
		// Draw foreground
		RenderSystem.enableBlend();
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 0, 100);
		blit(matrixStackIn, 0, 0,
				0, 0, GUI_HEALTHBAR_ORB_BACK_WIDTH, GUI_HEALTHBAR_ORB_BACK_HEIGHT);
		matrixStackIn.popPose();
		
		RenderSystem.disableBlend();
		matrixStackIn.popPose();
	}
	
	private void renderHealthbarBox(PoseStack matrixStackIn, LocalPlayer player, int width, int height, LivingEntity pet, boolean isSelected, int xoffset, int yoffset, float scale) {
		Minecraft mc = Minecraft.getInstance();
		
		// Render back, scaled bar + middle 'goods', and then foreground. Easy.
		// For center, render:
		// 1) healthbar
		// 2) pet head/icon
		// 3) pet status icon
		Font fonter = mc.font;
		
		PetInfo info = PetInfo.Wrap(pet);
		
		final PetValue valueToDisplay = GetPetValue(pet);
		
		final float health = (float) info.getHpPercent();//(float) (Math.max(0, Math.ceil(pet.getHealth())) / Math.max(0.01, Math.ceil(pet.getMaxHealth())));
		final boolean hasSecondaryBar = valueToDisplay != null && valueToDisplay.max() > 0;
		float secondaryMeter = hasSecondaryBar ? ((float) (valueToDisplay.current() / valueToDisplay.max())) : 0;
		final ValueFlavor flavor = hasSecondaryBar ? valueToDisplay.flavor() : null;
//		final boolean sitting = (pet instanceof EntityTameable ? ((EntityTameable) pet).isSitting()
//				: pet instanceof IEntityTameable ? ((IEntityTameable) pet).isSitting()
//				: false);
//		final boolean attacking = (pet instanceof Mob ? ((Mob) pet).getAttackTarget() != null : false);
		final EPetAction action = info.getPetAction();
		final float[] petColor = GetPetColor((Mob) pet);
		
		info.release();
		info = null;
		
		RenderSystem.setShaderTexture(0, GUI_HEALTHBARS);
		
		matrixStackIn.pushPose();
		
		matrixStackIn.translate(xoffset, yoffset, 0);
		matrixStackIn.scale(scale, scale, 1);
		
		RenderSystem.enableBlend();
		
		// Draw selected outline
		if (isSelected) {
			final double period = 1000;
			final float wiggleRadius = 3;
			final double wiggleProg = ((double) (System.currentTimeMillis() % (long) period) / period);
			final float wiggleMod = (float) Math.sin(wiggleProg * Math.PI * 2) * wiggleRadius;
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(-20 + (int)(wiggleMod), 0, -101);
			RenderSystem.setShaderColor(1f, 1f, 1f, .5f);
			
			blit(matrixStackIn, 0, 0,
					0, GUI_HEALTHBAR_BOX_BACK_VOFFSET + GUI_HEALTHBAR_BOX_BACK_HEIGHT, GUI_HEALTHBAR_BOX_BACK_WIDTH, GUI_HEALTHBAR_BOX_BACK_HEIGHT);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			matrixStackIn.popPose();
		}
		
		// Draw background
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 0, -100);
		RenderSystem.setShaderColor(petColor[0], petColor[1], petColor[2], petColor[3]);
		blit(matrixStackIn, 0, 0,
				0, GUI_HEALTHBAR_BOX_BACK_VOFFSET + GUI_HEALTHBAR_BOX_BACK_HEIGHT, GUI_HEALTHBAR_BOX_BACK_WIDTH, GUI_HEALTHBAR_BOX_BACK_HEIGHT);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		matrixStackIn.popPose();
		
		// Draw middle
		matrixStackIn.pushPose();
		// 	-> Health bar
		blit(matrixStackIn, 
				GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_BOX_HEALTH_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_BOX_HEALTH_BAR_VOFFSET,
				GUI_HEALTHBAR_BOX_HEALTH_WIDTH - Math.round(GUI_HEALTHBAR_BOX_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_BOX_HEALTH_HEIGHT);
		//	-> Secondary bar
		{
			final float[] color;
			if (!hasSecondaryBar) {
				color = new float[] {.7f, .9f, .7f, 1f};
				secondaryMeter = 1f;
			} else {
				color = new float[] {flavor.colorR(secondaryMeter),
						flavor.colorG(secondaryMeter),
						flavor.colorB(secondaryMeter),
						flavor.colorA(secondaryMeter)};
			}
			
			RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
			blit(matrixStackIn, 
					GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_VOFFSET,
					GUI_HEALTHBAR_BOX_SECONDARY_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_BOX_SECONDARY_BAR_VOFFSET,
					GUI_HEALTHBAR_BOX_SECONDARY_WIDTH - Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_BOX_SECONDARY_HEIGHT);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	
		
		//		-> Status
		matrixStackIn.translate(0, 0, 100);
		matrixStackIn.pushPose();
		matrixStackIn.scale(.6f, .6f, .6f);
		matrixStackIn.translate(0, 0, 0);
		PetActionIcon.get(action).draw(matrixStackIn, 282, 6,
				GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		matrixStackIn.popPose();

		//	-> Name
		final String name = pet.hasCustomName() ? pet.getCustomName().getString() : pet.getName().getString();
		final int nameLen = fonter.width(name);
		//final float fontScale = (1f/scale) * .6f;
		final float fontScale = scale * 2.4f;
		matrixStackIn.pushPose();
		matrixStackIn.scale(fontScale, fontScale, fontScale);
		fonter.drawShadow(matrixStackIn, name, 135 - (nameLen), 14 - (fonter.lineHeight + 2), 0xFFFFFFFF);
		RenderSystem.setShaderTexture(0, GUI_HEALTHBARS);
		matrixStackIn.popPose();
		
		matrixStackIn.popPose();
		
		// Draw foreground
		RenderSystem.enableBlend();
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 0, 100);
		blit(matrixStackIn, 0, 0,
				0, GUI_HEALTHBAR_BOX_BACK_VOFFSET, GUI_HEALTHBAR_BOX_BACK_WIDTH, GUI_HEALTHBAR_BOX_BACK_HEIGHT);
		matrixStackIn.popPose();
		
		RenderSystem.disableBlend();
		matrixStackIn.popPose();
	}
	
	// TODO move these out
	public static final float[] GetPetColor(LivingEntity pet) {
		return ColorUtil.ARGBToColor(GetPetColorRaw(pet));
	}
	public static final int GetPetColorRaw(LivingEntity pet) {
		Integer override = PetCommand.GetClientPetOverrides().getColor(pet.getUUID());
		if (override != null) {
			return override;
		} else {
			return GetDefaultPetColorRaw(pet); 
		}
	}
	
	protected static final int GetDefaultPetColorRaw(LivingEntity pet) {
		if (pet instanceof IEntityPet) {
			return ((IEntityPet) pet).getPetColor();
		} else if (pet instanceof Wolf wolf) {
//			final float[] rgb = ((Wolf) pet).getCollarColor().getTextureDiffuseColors();
//			return 0xFF000000
//					| ((byte)(rgb[0] * 255) << 16)
//					| ((byte)(rgb[1] * 255) << 8)
//					| ((byte)(rgb[2] * 255) << 0)
//					;
			final DyeColor color = wolf.getCollarColor();
			return ColorUtil.dyeToARGB(color);
		} else if (pet instanceof Cat cat) {
//			final float[] rgb = ((Cat) pet).getCollarColor().getTextureDiffuseColors();
//			return 0xFF000000
//					| ((byte)(rgb[0] * 255) << 16)
//					| ((byte)(rgb[1] * 255) << 8)
//					| ((byte)(rgb[2] * 255) << 0)
//					;
			final DyeColor color = cat.getCollarColor();
			return ColorUtil.dyeToARGB(color);
		} else {
			return IEntityPet.MakeColorFromID(pet.getUUID());
		}
	}
	
	public static final boolean IsHiddenFromHUD(LivingEntity pet) {
		Boolean override = PetCommand.GetClientPetOverrides().getHide(pet.getUUID());
		if (override != null) {
			return override.booleanValue();
		} else {
			return false;
		}
	}
	
	public static final int GetPetValueIndex(LivingEntity pet) {
		Integer override = PetCommand.GetClientPetOverrides().getValueIdx(pet.getUUID());
		int index = 0;
		if (override != null) {
			index = override.intValue();
		}
		
		return index;
	}
	
	public static final @Nullable PetValue GetPetValue(LivingEntity pet) {
		PetInfo info = PetInfo.Wrap(pet);
		
		List<PetValue> values = info.getPetValues();
		if (values == null || values.isEmpty()) {
			return null;
		}

		// Make sure index indicates a good value
		int index = GetPetValueIndex(pet);
		if (index >= values.size()) {
			index = 0;
		}
		while (index < values.size()) {
			PetValue value = values.get(index);
			if (value == null || value.label() == null) {
				index++;
			} else {
				break;
			}
		}
		
		if (index < values.size()) {
			return values.get(index);
		} else {
			return null;
		}
	}
	
	private void renderPetTargetIcon(PoseStack matrixStackIn, VertexConsumer buffer, Mob targeter, int i, float partialRenderTick) {
		
		final float scale = 1f / (16f * (256f / (float) GUI_HEALTHBAR_ICON_LENGTH));
		//final Minecraft mc = Minecraft.getInstance();
		matrixStackIn.pushPose();
		matrixStackIn.scale(scale, scale, 1f);
		matrixStackIn.translate(-(GUI_HEALTHBAR_ICON_LENGTH/2f), 0, 0);
		
		final float[] color = GetPetColor(targeter);
		
		final float width = GUI_HEALTHBAR_ICON_LENGTH;
		final float height = GUI_HEALTHBAR_ICON_LENGTH;
		final float minU = ((float) GUI_PET_ICON_FLOATTARGET_HOFFSET / 256f);
		final float maxU = minU + ((float) GUI_HEALTHBAR_ICON_LENGTH / 256f);
		final float minV = ((float) GUI_PET_ICON_FLOATTARGET_VOFFSET / 256f);
		final float maxV = minV + ((float) GUI_HEALTHBAR_ICON_LENGTH / 256f);
		{
			final Matrix4f transform = matrixStackIn.last().pose();
			//blit(matrixStackIn, 0, 0, GUI_PET_ICON_FLOATTARGET_HOFFSET, GUI_PET_ICON_FLOATTARGET_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
			buffer.vertex(transform, 0f, height, 0).color(color[0], color[1], color[2], color[3]).uv(minU, maxV).endVertex();
			buffer.vertex(transform, width, height, 0).color(color[0], color[1], color[2], color[3]).uv(maxU, maxV).endVertex();
			buffer.vertex(transform, width, 0f, 0).color(color[0], color[1], color[2], color[3]).uv(maxU, minV).endVertex();
			buffer.vertex(transform, 0f, 0f, 0).color(color[0], color[1], color[2], color[3]).uv(minU, minV).endVertex();
		}
		matrixStackIn.popPose();
	}
	
	public void changePetTargetIcon() {
		Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		if (petTargetIndex < 0) {
			// Brand new animation
			petTargetIndex = player.tickCount;
		} else if (player.tickCount - petTargetIndex > petTargetAnimDur/2) {
			// Reset to halfway point
			petTargetIndex = player.tickCount - petTargetAnimDur/2;
		} else {
			; // Fading in, leave alone and just swap out the icon
		}
	}
	
	public void changePetPlacementIcon() {
		Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		if (petPlacementIndex < 0) {
			// Brand new animation
			petPlacementIndex = player.tickCount;
		} else if (player.tickCount - petPlacementIndex > petPlacementAnimDur/2) {
			// Reset to halfway point
			petPlacementIndex = player.tickCount - petPlacementAnimDur/2;
		} else {
			; // Fading in, leave alone and just swap out the icon
		}
	}
}
