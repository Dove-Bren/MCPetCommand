package com.smanzana.petcommand.client.widgetdupe;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;

public class TextWidget extends ObscurableChildWidget {
	
	protected final Screen parent;
	protected final Component text;
	
	protected @Nullable List<Component> tooltip;
	protected int color = 0xFFDDDDDD;
	protected float scale = 1f;
	protected boolean centered = false;
	protected boolean truncate = false;
	
	public TextWidget(Screen parent, Component text, int x, int y, int width, int height) {
		super(x, y, width, height, TextComponent.EMPTY);
		this.parent = parent;
		this.text = text;
	}
	
	public TextWidget tooltip(List<Component> tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	
	public TextWidget tooltip(Component tooltip) {
		return tooltip(Lists.newArrayList(tooltip));
	}
	
	public TextWidget color(int color) {
		this.color = color;
		return this;
	}
	
	public TextWidget scale(float scale) {
		this.scale = scale;
		return this;
	}
	
	public TextWidget center() {
		this.centered = true;
		return this;
	}
	
	public TextWidget truncate() {
		this.truncate = true;
		return this;
	}
	
	@Override
	public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = this.parent.getMinecraft();
		final Font font = mc.font;
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(this.x, this.y, 0);
		matrixStackIn.scale(scale, scale, 1f);
		final FormattedCharSequence textToDraw;
		if (truncate) {
			textToDraw = Language.getInstance().getVisualOrder(font.substrByWidth(this.text, (int) (this.width / scale)));
		} else {
			textToDraw = this.text.getVisualOrderText();
		}
		final int textWidth = font.width(textToDraw);
		font.draw(matrixStackIn, textToDraw, centered ? -(textWidth/2) : 0, 0, color);
		matrixStackIn.popPose();
		
		final int actingWidth = (int) (textWidth * scale);
		final int actingHeight = (int) (font.lineHeight * scale);
		final int actingX = centered ? (this.x - (textWidth/2)) : this.x;
		this.isHovered = mouseX >= actingX && mouseY >= this.y && mouseX < actingX + actingWidth && mouseY < this.y + actingHeight;
	}
	
	@Override
	public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
		if (this.isHoveredOrFocused() && this.tooltip != null) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, 100);
			parent.renderComponentTooltip(matrixStackIn, tooltip, mouseX, mouseY);
			matrixStackIn.popPose();
		}
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		return false; // no click consumption
	}
}
