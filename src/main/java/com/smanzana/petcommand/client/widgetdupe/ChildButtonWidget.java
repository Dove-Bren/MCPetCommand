package com.smanzana.petcommand.client.widgetdupe;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ChildButtonWidget<T extends ChildButtonWidget<T>> extends ObscurableChildWidget<T> {
	
	// Copied from AbstractButton in 1.20.6
//	protected static final WidgetSprites SPRITES = new WidgetSprites(
//		ResourceLocation.parse("widget/button"), ResourceLocation.parse("widget/button_disabled"), ResourceLocation.parse("widget/button_highlighted")
//	);
	
	protected static final void DrawVanillaButton(GuiGraphics graphics, ChildButtonWidget<?> widget) {
		DrawVanillaButton(graphics, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), widget.active, widget.isHoveredOrFocused());
	}
	
	// Adapted from AbstractButton in 1.20.1
	protected static final void DrawVanillaButton(GuiGraphics graphics, int x, int y, int width, int height, boolean active, boolean isHoveredOrFocused) {
		// getTextureY()
		int textureY = 1;
		if (!active) {
			textureY = 0;
		} else if (isHoveredOrFocused) {
			textureY = 2;
		}

		textureY = 46 + textureY * 20;
		
		
		graphics.blitNineSliced(WIDGETS_LOCATION, x, y, width, height, 20, 4, 200, 20, 0, textureY);
	}
	
	// Mirror of Button.OnPress
	public static interface OnPress {
		 void onPress(ChildButtonWidget<?> button);
	}
	
	protected final Screen parent;
	
	protected final OnPress onPress;
	
	public ChildButtonWidget(Screen parent, int x, int y, int width, int height, Component label, OnPress onPress) {
		super(x, y, width, height, label);
		this.parent = parent;
		this.onPress = onPress;
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		final int x = getX();
		final int y = getY();
//		//super.renderButton(matrixStackIn, mouseX, mouseY, partialTicks);
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		final float sat = this.active ? 1f : .3f;
		graphics.setColor(sat, sat, sat, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		{
////			this.blit(matrixStackIn, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
////			this.blit(matrixStackIn, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
//			// gonna just render outside border, and then grainy texture. Will scale if button is too large?
//			fill(matrixStackIn, x, y, x + width, y + height, this.active && this.isHoveredOrFocused() ? 0xFFCCCCCC : 0xFF000000);
//			
//			renderButtonIcon(matrixStackIn, x + 1, y + 1, width - 2, height - 2, partialTicks);
//			
//			// highlights
//			fill(matrixStackIn, x + 1, y + 1, x + (width - 1), y + 2, 0x40FFFFFF);
//			fill(matrixStackIn, x + 1, y + 1, x + 2, y + (height - 1), 0x40FFFFFF);
//			fill(matrixStackIn, x + 1, y + (height - 2), x + (width - 1), y + (height - 1), 0x40000000);
//			fill(matrixStackIn, x + (width - 2), y + 1, x + (width - 1), y + (height - 1), 0x40000000);
//			
		
			// 1.20.6
			//graphics.blitSprite(SPRITES.get(active, this.isHoveredOrFocused()), x, y, getWidth(), getHeight());
			
			DrawVanillaButton(graphics, this);
		}
//		//this.renderBg(matrixStackIn, minecraft, mouseX, mouseY);
		graphics.setColor(1f, 1f, 1f, 1f);
		int j = getFGColor();
		
		{
			// possibly scale down if button is small
			graphics.pose().pushPose();
			graphics.pose().translate(x + this.width / 2, y + this.height / 2, 0);
			if (font.lineHeight > this.height - 4) {
				final float scale = (float)(this.height - 4) / (float) (font.lineHeight);
				graphics.pose().scale(scale, scale, 1f);
			}
			graphics.drawCenteredString(font, this.getMessage(), 0, -(font.lineHeight / 2), j | Mth.ceil(this.alpha * 255.0F) << 24);
			graphics.pose().popPose();
		}
		
		
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		return super.isValidClickButton(button); // no click consumption
	}
	
	@Override
	public void onClick(double mouseX, double mouseY) {
		this.onPress.onPress(this);
	}
}
