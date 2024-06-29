package com.smanzana.petcommand.client.overlay;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.PetAction;
import com.smanzana.petcommand.api.pet.PetInfo.SecondaryFlavor;
import com.smanzana.petcommand.api.pet.PetPlacementMode;
import com.smanzana.petcommand.api.pet.PetTargetMode;
import com.smanzana.petcommand.client.render.PetCommandRenderTypes;
import com.smanzana.petcommand.config.ModConfig;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends AbstractGui {

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
	private static final int GUI_HEALTHBAR_ORB_ENTITY_VOFFSET = 40;
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
	private static final int GUI_HEALTHBAR_ICON_HOFFSET = 207;
	private static final int GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET = 300;
	private static final int GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET = 50;
	private static final int GUI_HEALTHBAR_ICON_STAY_VOFFSET = 0;
	private static final int GUI_HEALTHBAR_ICON_ATTACK_VOFFSET = GUI_HEALTHBAR_ICON_STAY_VOFFSET + GUI_HEALTHBAR_ICON_LENGTH;
	private static final int GUI_HEALTHBAR_ICON_WORK_VOFFSET = GUI_HEALTHBAR_ICON_ATTACK_VOFFSET + GUI_HEALTHBAR_ICON_LENGTH;
	
	public static final ResourceLocation GUI_PET_ICONS = new ResourceLocation(PetCommand.MODID, "textures/gui/pet_icons.png");
	//private static final int GUI_PET_ICONS_DIMS = 256;
	private static final int GUI_PET_ICON_DIMS = 32;
	private static final int GUI_PET_ICON_TARGET_HOFFSET = 0;
	private static final int GUI_PET_ICON_TARGET_VOFFSET = 0;
	private static final int GUI_PET_ICON_PLACEMENT_HOFFSET = 0;
	private static final int GUI_PET_ICON_PLACEMENT_VOFFSET = GUI_PET_ICON_TARGET_VOFFSET + GUI_PET_ICON_DIMS;
	private static final int GUI_PET_ICON_FLOATTARGET_HOFFSET = 0;
	private static final int GUI_PET_ICON_FLOATTARGET_VOFFSET = GUI_PET_ICON_PLACEMENT_VOFFSET + GUI_PET_ICON_DIMS;
	
	private int petTargetIndex; // Controls displaying pet target icon (fade in/out 50%)
	private int petTargetAnimDur = 80;
	private int petPlacementIndex; // Controls displaying pet placement icon (fade in/out at 50%)
	private int petPlacementAnimDur = 80;
	
	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		petTargetIndex = -1;
		petPlacementIndex = -1;
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		MainWindow window = event.getWindow();
		MatrixStack matrixStackIn = event.getMatrixStack();
		
		if (ModConfig.config.showHealthbars()
				&& event.getType() == ElementType.EXPERIENCE) {
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
					xOffset = window.getScaledWidth() - (-xConfigOffset + healthbarWidth);
				}
				
				List<LivingEntity> bigPets = PetFuncs.GetTamedEntities(player, (ent) -> {
					return ent != null && ent instanceof IEntityPet && ((IEntityPet) ent).isBigPet();
				});
				Collections.sort(bigPets, (left, right) -> {
					return ((LivingEntity) (left)).getUniqueID().compareTo(((LivingEntity) right).getUniqueID());
				});
				for (LivingEntity bigPet: bigPets) {
					renderHealthbarOrb(matrixStackIn, player, window, bigPet, xOffset, y, scale);
					y += healthbarHeight + 2;
				}
			}
			
			// Regulars/all
			healthbarWidth = (int) (GUI_HEALTHBAR_BOX_BACK_WIDTH * scale);
			healthbarHeight = (int) (GUI_HEALTHBAR_BOX_BACK_HEIGHT * scale);
			if (xConfigOffset >= 0) {
				xOffset = xConfigOffset;
			} else {
				xOffset = window.getScaledWidth() - (-xConfigOffset + healthbarWidth);
			}
			final boolean hideBigs = ModConfig.config.showBigHealthbars();
			for (LivingEntity tamed : PetFuncs.GetTamedEntities(player)) {
				if (hideBigs
						&& tamed instanceof IEntityPet
						&& ((IEntityPet) tamed).isBigPet()) {
					continue;
				}
				renderHealthbarBox(matrixStackIn, player, window, tamed, xOffset, y, scale);
				y += healthbarHeight;
			}
		} else if (event.getType() == ElementType.CROSSHAIRS) {
			final float ticks = player.ticksExisted + event.getPartialTicks();
			if (petTargetIndex >= 0) {
				PetTargetMode mode = PetCommand.GetPetCommandManager().getTargetMode(player);
				renderPetActionTargetMode(matrixStackIn, player, window, mode, (ticks - petTargetIndex) / (float) petTargetAnimDur);
				
				if (ticks >= petTargetIndex + petTargetAnimDur) {
					petTargetIndex = -1;
				}
			}
			
			if (petPlacementIndex >= 0) {
				PetPlacementMode mode = PetCommand.GetPetCommandManager().getPlacementMode(player);
				renderPetActionPlacementMode(matrixStackIn, player, window, mode, (ticks - petPlacementIndex) / (float) petPlacementAnimDur);
				
				if (ticks >= petPlacementIndex + petPlacementAnimDur) {
					petPlacementIndex = -1;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onWorldRender(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
		List<MobEntity> targeters = PetCommandAPI.GetTargetManager(event.getEntity()).getEntitiesTargetting(event.getEntity());
		if (!targeters.isEmpty()) {
			int i = 0;
			final MatrixStack matrixStackIn = event.getMatrixStack();
			final Minecraft mc = Minecraft.getInstance();
			final ActiveRenderInfo activeRenderInfo = mc.getRenderManager().info;
			final IVertexBuilder buffer = event.getBuffers().getBuffer(PetCommandRenderTypes.PET_TARGET_ICON); // Could only grab this when rendering at least one?
			matrixStackIn.push();
			matrixStackIn.translate(0, event.getEntity().getHeight() + .15f, 0);
			matrixStackIn.rotate(activeRenderInfo.getRotation());
			matrixStackIn.translate(-event.getEntity().getWidth()/2f, 0, 0);
			for (MobEntity targeter : targeters) {
				@Nullable LivingEntity owner = PetFuncs.GetOwner(targeter);
				if (owner != null && owner == mc.player) {
					matrixStackIn.translate(0, .25f /* *i*/, 0);
					renderPetTargetIcon(matrixStackIn, buffer, targeter, i++, event.getPartialRenderTick());
				}
			}
			matrixStackIn.pop();
		}
	}

	private void renderPetActionTargetMode(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow scaledResolution, PetTargetMode mode, float prog) {
		Minecraft mc = Minecraft.getInstance();
		final float alpha;
		if (prog < .2f) {
			alpha = prog / .2f;
		} else if (prog >= .8f) {
			alpha = (1f-prog) / .2f;
		} else {
			alpha = 1f;
		}
		
		final int u = GUI_PET_ICON_TARGET_HOFFSET + (mode.ordinal() * GUI_PET_ICON_DIMS);
		final int v = GUI_PET_ICON_TARGET_VOFFSET; // + (mode.ordinal() * GUI_PET_ICON_DIMS);
		
		matrixStackIn.push();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		mc.getTextureManager().bindTexture(GUI_PET_ICONS);
		
		matrixStackIn.translate(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.translate(1, 1, 0);
		
		RenderSystem.color4f(1f, 1f, 1f, alpha * .6f);
		blit(matrixStackIn, 0, 0, u, v, GUI_PET_ICON_DIMS, GUI_PET_ICON_DIMS);
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		
		RenderSystem.disableBlend();
		matrixStackIn.pop();
	}
	
	private void renderPetActionPlacementMode(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow scaledResolution, PetPlacementMode mode, float prog) {
		Minecraft mc = Minecraft.getInstance();
		final float alpha;
		if (prog < .2f) {
			alpha = prog / .2f;
		} else if (prog >= .8f) {
			alpha = (1f-prog) / .2f;
		} else {
			alpha = 1f;
		}
		final int u = GUI_PET_ICON_PLACEMENT_HOFFSET + (mode.ordinal() * GUI_PET_ICON_DIMS);
		final int v = GUI_PET_ICON_PLACEMENT_VOFFSET; // + (mode.ordinal() * GUI_PET_ICON_DIMS);
		
		matrixStackIn.push();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		mc.getTextureManager().bindTexture(GUI_PET_ICONS);
		
		matrixStackIn.translate(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.translate(-(GUI_PET_ICON_DIMS + 1), 1, 0);
		
		RenderSystem.color4f(1f, 1f, 1f, alpha * .6f);
		blit(matrixStackIn, 0, 0, u, v, GUI_PET_ICON_DIMS, GUI_PET_ICON_DIMS);
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		
		RenderSystem.disableBlend();
		matrixStackIn.pop();
	}
	
	private void renderHealthbarOrb(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow window, LivingEntity pet, int xoffset, int yoffset, float scale) {
		Minecraft mc = Minecraft.getInstance();
		
		// Render back, scaled bar + middle 'goods', and then foreground. Easy.
		// For center, render:
		// 1) healthbar
		// 2) pet head/icon
		// 3) pet status icon
		FontRenderer fonter = mc.fontRenderer;
//		final boolean sitting = (pet instanceof EntityTameable ? ((EntityTameable) pet).isSitting()
//				: pet instanceof IEntityTameable ? ((IEntityTameable) pet).isSitting()
//				: false);
//		final boolean attacking = (pet instanceof MobEntity ? ((MobEntity) pet).getAttackTarget() != null : false);
//		final float health = (float) (Math.max(0, Math.ceil(pet.getHealth())) / Math.max(0.01, Math.ceil(pet.getMaxHealth())));
//		boolean hasSecondaryBar = false;
//		float secondaryMeter = 0f;
//		
//		if (pet instanceof ITameDragon) {
//			ITameDragon dragon = (ITameDragon) pet;
//			hasSecondaryBar = true;
//			secondaryMeter = (float) dragon.getXP() / (float) dragon.getMaxXP();
//		}
		
		PetInfo info;
		if (pet instanceof IEntityPet) {
			IEntityPet iPet = (IEntityPet) pet;
			info = iPet.getPetSummary();
		} else {
			info = PetInfo.Wrap(pet);
		}
		
		final float health = (float) info.getHpPercent();//(float) (Math.max(0, Math.ceil(pet.getHealth())) / Math.max(0.01, Math.ceil(pet.getMaxHealth())));
		final boolean hasSecondaryBar = info.getMaxSecondary() > 0;
		float secondaryMeter = (float) info.getSecondaryPercent();
		final SecondaryFlavor flavor = info.getSecondaryFlavor();
		final PetAction action = info.getPetAction();
		
		info.release();
		info = null;
		
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		
		matrixStackIn.push();
		
		matrixStackIn.translate(xoffset, yoffset, 0);
		matrixStackIn.scale(scale, scale, 1);
		
		RenderSystem.enableBlend();
		
		// Draw background
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, -100);
		this.fillGradient(matrixStackIn, GUI_HEALTHBAR_ORB_NAME_HOFFSET, GUI_HEALTHBAR_ORB_NAME_VOFFSET,
				GUI_HEALTHBAR_ORB_NAME_WIDTH, GUI_HEALTHBAR_ORB_NAME_HEIGHT,
				0x50000000, 0xA0000000); //nameplate background
		blit(matrixStackIn, 0, 0,
				0, GUI_HEALTHBAR_ORB_BACK_HEIGHT, GUI_HEALTHBAR_ORB_BACK_WIDTH, GUI_HEALTHBAR_ORB_BACK_HEIGHT);
		matrixStackIn.pop();
		
		// Draw middle
		matrixStackIn.push();
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
			
			RenderSystem.color4f(color[0], color[1], color[2], color[3]);
			blit(matrixStackIn, 
					GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_VOFFSET,
					GUI_HEALTHBAR_ORB_SECONDARY_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_ORB_SECONDARY_BAR_VOFFSET,
					GUI_HEALTHBAR_ORB_SECONDARY_WIDTH - Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_ORB_SECONDARY_HEIGHT);
			RenderSystem.color4f(1f, 1f, 1f, 1f);
		}
	
		//	-> Icon
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(matrixStackIn.getLast().getMatrix());
		InventoryScreen.drawEntityOnScreen(GUI_HEALTHBAR_ORB_ENTITY_HOFFSET, GUI_HEALTHBAR_ORB_ENTITY_VOFFSET, GUI_HEALTHBAR_ORB_ENTITY_WIDTH, 0, 0, pet);
		RenderSystem.popMatrix();
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		
		//	-> Status
		matrixStackIn.translate(0, 0, 100);
		matrixStackIn.push();
		matrixStackIn.scale(.6f, .6f, .6f);
		matrixStackIn.translate(0, 0, 0);
		if (action == PetAction.ATTACKING) {
			blit(matrixStackIn, GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_ATTACK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.SITTING) {
			blit(matrixStackIn, GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_STAY_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.WORKING) {
			blit(matrixStackIn, GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_WORK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		}
		matrixStackIn.pop();
		
		//	-> Name
		final String name = pet.hasCustomName() ? pet.getCustomName().getString() : pet.getName().getString();
		final int nameLen = fonter.getStringWidth(name);
		//final float fontScale = (1f/scale) * .6f;
		final float fontScale = scale * 2.4f;
		matrixStackIn.push();
		matrixStackIn.scale(fontScale, fontScale, fontScale);
		fonter.drawString(matrixStackIn, name, 123 - (nameLen), 25 - (fonter.FONT_HEIGHT + 2), 0xFFFFFFFF);
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		matrixStackIn.pop();
		
		matrixStackIn.pop();
		
		// Draw foreground
		RenderSystem.enableBlend();
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, 100);
		blit(matrixStackIn, 0, 0,
				0, 0, GUI_HEALTHBAR_ORB_BACK_WIDTH, GUI_HEALTHBAR_ORB_BACK_HEIGHT);
		matrixStackIn.pop();
		
		RenderSystem.disableBlend();
		matrixStackIn.pop();
	}
	
	private void renderHealthbarBox(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow window, LivingEntity pet, int xoffset, int yoffset, float scale) {
		Minecraft mc = Minecraft.getInstance();
		
		// Render back, scaled bar + middle 'goods', and then foreground. Easy.
		// For center, render:
		// 1) healthbar
		// 2) pet head/icon
		// 3) pet status icon
		FontRenderer fonter = mc.fontRenderer;
		
		PetInfo info;
		if (pet instanceof IEntityPet) {
			IEntityPet iPet = (IEntityPet) pet;
			info = iPet.getPetSummary();
		} else {
			info = PetInfo.Wrap(pet);
		}
		
		final float health = (float) info.getHpPercent();//(float) (Math.max(0, Math.ceil(pet.getHealth())) / Math.max(0.01, Math.ceil(pet.getMaxHealth())));
		final boolean hasSecondaryBar = info.getMaxSecondary() > 0;
		float secondaryMeter = (float) info.getSecondaryPercent();
		final SecondaryFlavor flavor = info.getSecondaryFlavor();
//		final boolean sitting = (pet instanceof EntityTameable ? ((EntityTameable) pet).isSitting()
//				: pet instanceof IEntityTameable ? ((IEntityTameable) pet).isSitting()
//				: false);
//		final boolean attacking = (pet instanceof MobEntity ? ((MobEntity) pet).getAttackTarget() != null : false);
		final PetAction action = info.getPetAction();
		
		info.release();
		info = null;
		
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		
		matrixStackIn.push();
		
		matrixStackIn.translate(xoffset, yoffset, 0);
		matrixStackIn.scale(scale, scale, 1);
		
		RenderSystem.enableBlend();
		
		// Draw background
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, -100);
//		this.drawGradientRect(GUI_HEALTHBAR_ORB_NAME_HOFFSET, GUI_HEALTHBAR_ORB_NAME_VOFFSET,
//				GUI_HEALTHBAR_ORB_NAME_WIDTH, GUI_HEALTHBAR_ORB_NAME_HEIGHT,
//				0x50000000, 0xA0000000); //nameplate background
		blit(matrixStackIn, 0, 0,
				0, GUI_HEALTHBAR_BOX_BACK_VOFFSET + GUI_HEALTHBAR_BOX_BACK_HEIGHT, GUI_HEALTHBAR_BOX_BACK_WIDTH, GUI_HEALTHBAR_BOX_BACK_HEIGHT);
		matrixStackIn.pop();
		
		// Draw middle
		matrixStackIn.push();
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
			
			RenderSystem.color4f(color[0], color[1], color[2], color[3]);
			blit(matrixStackIn, 
					GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_VOFFSET,
					GUI_HEALTHBAR_BOX_SECONDARY_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_BOX_SECONDARY_BAR_VOFFSET,
					GUI_HEALTHBAR_BOX_SECONDARY_WIDTH - Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
					GUI_HEALTHBAR_BOX_SECONDARY_HEIGHT);
			RenderSystem.color4f(1f, 1f, 1f, 1f);
		}
	
		
		//		-> Status
		matrixStackIn.translate(0, 0, 100);
		matrixStackIn.push();
		matrixStackIn.scale(.6f, .6f, .6f);
		matrixStackIn.translate(0, 0, 0);
		if (action == PetAction.ATTACKING) {
			blit(matrixStackIn, 282, 6,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_ATTACK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.SITTING) {
			blit(matrixStackIn, 282, 6,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_STAY_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.WORKING) {
			blit(matrixStackIn, 282, 6,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_WORK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		}
		matrixStackIn.pop();

		//	-> Name
		final String name = pet.hasCustomName() ? pet.getCustomName().getString() : pet.getName().getString();
		final int nameLen = fonter.getStringWidth(name);
		//final float fontScale = (1f/scale) * .6f;
		final float fontScale = scale * 2.4f;
		matrixStackIn.push();
		matrixStackIn.scale(fontScale, fontScale, fontScale);
		fonter.drawStringWithShadow(matrixStackIn, name, 135 - (nameLen), 14 - (fonter.FONT_HEIGHT + 2), 0xFFFFFFFF);
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		matrixStackIn.pop();
		
		matrixStackIn.pop();
		
		// Draw foreground
		RenderSystem.enableBlend();
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, 100);
		blit(matrixStackIn, 0, 0,
				0, GUI_HEALTHBAR_BOX_BACK_VOFFSET, GUI_HEALTHBAR_BOX_BACK_WIDTH, GUI_HEALTHBAR_BOX_BACK_HEIGHT);
		matrixStackIn.pop();
		
		RenderSystem.disableBlend();
		matrixStackIn.pop();
	}
	
	protected float[] getTargetColor(MobEntity targeter) {
		if (targeter instanceof IEntityPet) {
			final int raw = ((IEntityPet) targeter).getPetColor();
			return new float[] {
				(float) ((raw >> 16) & 0xFF) / 256f,
				(float) ((raw >> 8) & 0xFF) / 256f,
				(float) ((raw >> 0) & 0xFF) / 256f,
				(float) ((raw >> 24) & 0xFF) / 256f,
			};
		} else if (targeter instanceof WolfEntity) {
			final float[] rgb = ((WolfEntity) targeter).getCollarColor().getColorComponentValues();
			return new float[] {
					rgb[0], rgb[1], rgb[2], 1f
			};
		} else if (targeter instanceof CatEntity) {
			final float[] rgb = ((CatEntity) targeter).getCollarColor().getColorComponentValues();
			return new float[] {
					rgb[0], rgb[1], rgb[2], 1f
			};
		} else {
			final int raw = IEntityPet.MakeColorFromID(targeter.getUniqueID());
			return new float[] {
				(float) ((raw >> 16) & 0xFF) / 256f,
				(float) ((raw >> 8) & 0xFF) / 256f,
				(float) ((raw >> 0) & 0xFF) / 256f,
				(float) ((raw >> 24) & 0xFF) / 256f,
			};
		}
	}
	
	private void renderPetTargetIcon(MatrixStack matrixStackIn, IVertexBuilder buffer, MobEntity targeter, int i, float partialRenderTick) {
		
		final float scale = 1f / (16f * (256f / (float) GUI_HEALTHBAR_ICON_LENGTH));
		//final Minecraft mc = Minecraft.getInstance();
		//mc.getTextureManager().bindTexture(GUI_PET_ICONS);
		matrixStackIn.push();
		matrixStackIn.scale(scale, scale, 1f);
		matrixStackIn.translate(-(GUI_HEALTHBAR_ICON_LENGTH/2f), 0, 0);
		
		final float[] color = getTargetColor(targeter);
		
		final float width = GUI_HEALTHBAR_ICON_LENGTH;
		final float height = GUI_HEALTHBAR_ICON_LENGTH;
		final float minU = ((float) GUI_PET_ICON_FLOATTARGET_HOFFSET / 256f);
		final float maxU = minU + ((float) GUI_HEALTHBAR_ICON_LENGTH / 256f);
		final float minV = ((float) GUI_PET_ICON_FLOATTARGET_VOFFSET / 256f);
		final float maxV = minV + ((float) GUI_HEALTHBAR_ICON_LENGTH / 256f);
		{
			final Matrix4f transform = matrixStackIn.getLast().getMatrix();
			//blit(matrixStackIn, 0, 0, GUI_PET_ICON_FLOATTARGET_HOFFSET, GUI_PET_ICON_FLOATTARGET_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
			buffer.pos(transform, 0f, height, 0).color(color[0], color[1], color[2], color[3]).tex(minU, maxV).endVertex();
			buffer.pos(transform, width, height, 0).color(color[0], color[1], color[2], color[3]).tex(maxU, maxV).endVertex();
			buffer.pos(transform, width, 0f, 0).color(color[0], color[1], color[2], color[3]).tex(maxU, minV).endVertex();
			buffer.pos(transform, 0f, 0f, 0).color(color[0], color[1], color[2], color[3]).tex(minU, minV).endVertex();
		}
		matrixStackIn.pop();
	}
	
	public void changePetTargetIcon() {
		Minecraft mc = Minecraft.getInstance();
		final ClientPlayerEntity player = mc.player;
		if (petTargetIndex < 0) {
			// Brand new animation
			petTargetIndex = player.ticksExisted;
		} else if (player.ticksExisted - petTargetIndex > petTargetAnimDur/2) {
			// Reset to halfway point
			petTargetIndex = player.ticksExisted - petTargetAnimDur/2;
		} else {
			; // Fading in, leave alone and just swap out the icon
		}
	}
	
	public void changePetPlacementIcon() {
		Minecraft mc = Minecraft.getInstance();
		final ClientPlayerEntity player = mc.player;
		if (petPlacementIndex < 0) {
			// Brand new animation
			petPlacementIndex = player.ticksExisted;
		} else if (player.ticksExisted - petPlacementIndex > petPlacementAnimDur/2) {
			// Reset to halfway point
			petPlacementIndex = player.ticksExisted - petPlacementAnimDur/2;
		} else {
			; // Fading in, leave alone and just swap out the icon
		}
	}
}
