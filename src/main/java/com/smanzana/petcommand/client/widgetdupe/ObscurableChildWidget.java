package com.smanzana.petcommand.client.widgetdupe;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class ObscurableChildWidget<T extends ObscurableChildWidget<?>> extends MoveableObscurableWidget implements IChildWidget {

	protected int parentOffsetX;
	protected int parentOffsetY;
	
	// this probably shouldn't be here but this is a pretty common child class
	protected @Nullable ITooltip tooltip;
	
	public ObscurableChildWidget(int x, int y, int width, int height, Component label) {
		super(x, y, width, height, label);
		this.setParentOffset(x, y);
	}
	
	@SuppressWarnings("unchecked")
	protected final T self() {
		return (T) this;
	}
	
	public T setParentOffset(int x, int y) {
		this.parentOffsetX = x;
		this.parentOffsetY = y;
		return self();
	}

	@Override
	public int getParentOffsetX() {
		return parentOffsetX;
	}

	@Override
	public int getParentOffsetY() {
		return parentOffsetY;
	}
	
	public final T tooltip(ITooltip tooltip) {
		this.tooltip = tooltip;
		return self();
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		super.render(pose, mouseX, mouseY, partialTicks);
	}
	
	//@Override CAn't override parent since it's private
	public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
		if (this.isHoveredOrFocused() && this.tooltip != null) {
			Tooltip.RenderTooltip(this.tooltip, getScreen(), matrixStackIn, mouseX, mouseY);
		}
	}
	
	protected Screen getScreen() {
		return GetScreen();
	}
	
	protected static final Screen GetScreen() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.screen;
	}

}
