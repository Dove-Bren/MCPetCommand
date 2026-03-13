package com.smanzana.petcommand.client.widgetdupe;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class Tooltip implements ITooltip {
	
	protected final List<Component> staticTooltip;

	private Tooltip(List<Component> staticTooltip) {
		this.staticTooltip = staticTooltip;
	}
	
	public static Tooltip create(List<Component> tooltip) {
		return new Tooltip(tooltip);
	}
	
	public static Tooltip create(Component tooltip) {
		return new Tooltip(List.of(tooltip));
	}
	
	@Override
	public List<Component> get() {
		return this.staticTooltip;
	}
	
	public static final void RenderTooltip(ITooltip tooltip, Screen parent, GuiGraphics graphics, int mouseX, int mouseY) {
		final var rawTooltip = tooltip.get();
		if (rawTooltip != null && !rawTooltip.isEmpty()) {
			final Minecraft mc = Minecraft.getInstance();
			final Font font = mc.font;
			
			graphics.pose().pushPose();
			graphics.pose().translate(0, 0, 100);
			graphics.renderComponentTooltip(font, rawTooltip, mouseX, mouseY);
			graphics.pose().popPose();
		}
	}
	
}
