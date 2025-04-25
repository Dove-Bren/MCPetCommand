package com.smanzana.petcommand.client.icon;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.pet.PetInfo.PetAction;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

public class PetActionIcon {

	private static Map<PetAction, PetActionIcon> cache = new EnumMap<>(PetAction.class);
	
	public static PetActionIcon get(PetAction action) {
		return cache.computeIfAbsent(action, PetActionIcon::new);
	}
	
	private static final int texLen = 32;
	
	private final ResourceLocation texture;
	
	private PetActionIcon(PetAction action) {
		texture = new ResourceLocation(PetCommand.MODID, "textures/gui/petaction_" + action.name().toLowerCase() + ".png");
	}
	
	protected ResourceLocation getTexture() {
		return texture;
	}
	
	public void draw(PoseStack matrixStackIn, int xOffset, int yOffset, int width, int height) {
		draw(matrixStackIn, xOffset, yOffset, width, height, 1f, 1f, 1f, 1f);
	}
	
	public void draw(PoseStack matrixStackIn, int xOffset, int yOffset, int width, int height,
			float red, float green, float blue, float alpha) {
		matrixStackIn.pushPose();

		{
			RenderSystem.setShaderTexture(0, this.getTexture());
			RenderSystem.enableBlend();
			RenderSystem.setShaderColor(red, green, blue, alpha); // idk if this works since blit uses postex
			GuiComponent.blit(matrixStackIn, xOffset, yOffset, 0, 0, width, height, texLen, texLen);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			RenderSystem.disableBlend();
		}
		
		matrixStackIn.popPose();
	}
	
}
