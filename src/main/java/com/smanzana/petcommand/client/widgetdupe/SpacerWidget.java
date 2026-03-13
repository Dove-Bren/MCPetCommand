package com.smanzana.petcommand.client.widgetdupe;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SpacerWidget extends ObscurableChildWidget<SpacerWidget> {

	public SpacerWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.empty());
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		return false; // no click consumption
	}
	
	@Override
	public void renderWidget(GuiGraphics matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		; // render nothing
	}

}
