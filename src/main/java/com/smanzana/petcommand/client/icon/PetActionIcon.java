package com.smanzana.petcommand.client.icon;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.pet.EPetAction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class PetActionIcon {

	private static Map<EPetAction, PetActionIcon> cache = new EnumMap<>(EPetAction.class);
	
	public static PetActionIcon get(EPetAction action) {
		return cache.computeIfAbsent(action, PetActionIcon::new);
	}
	
	private static final int texLen = 32;
	
	private final ResourceLocation texture;
	
	private PetActionIcon(EPetAction action) {
		texture = ResourceLocation.fromNamespaceAndPath(PetCommand.MODID, "textures/gui/petaction_" + action.name().toLowerCase() + ".png");
	}
	
	protected ResourceLocation getTexture() {
		return texture;
	}
	
	public void draw(GuiGraphics graphics, int xOffset, int yOffset, int width, int height) {
		draw(graphics, xOffset, yOffset, width, height, 1f, 1f, 1f, 1f);
	}
	
	public void draw(GuiGraphics graphics, int xOffset, int yOffset, int width, int height,
			float red, float green, float blue, float alpha) {
		graphics.pose().pushPose();

		{
			RenderSystem.enableDepthTest();
			RenderSystem.setShaderTexture(0, this.getTexture());
			RenderSystem.enableBlend();
			graphics.setColor(red, green, blue, alpha); // idk if this works since blit uses postex
			graphics.blit(getTexture(), xOffset, yOffset, width, height, 0, 0, texLen, texLen, 32, 32);
			//GuiComponent.blit(matrixStackIn, xOffset, yOffset, width, height, 0, 0, texLen, texLen, 32, 32);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			RenderSystem.disableBlend();
		}
		
		graphics.pose().popPose();
	}
	
}
