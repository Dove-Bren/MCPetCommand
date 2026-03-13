package com.smanzana.petcommand.client.widget;

import java.util.function.Supplier;

import com.smanzana.petcommand.api.pet.EPetAction;
import com.smanzana.petcommand.client.icon.PetActionIcon;
import com.smanzana.petcommand.client.widgetdupe.LabeledWidget.IValue;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

public class PetActionValue implements IValue {

	protected final Supplier<EPetAction> valueGetter;
	protected final int width;
	protected final int height;
	
	public PetActionValue(Supplier<EPetAction> value, int width, int height) {
		this.valueGetter = value;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public Rect2i render(GuiGraphics graphics, int x, int y, float partialTicks, int color, Rect2i labelArea) {
		final EPetAction value = valueGetter.get();
		
		// Try to center vertically with the label
		final int labelCenter = (labelArea.getY() + (labelArea.getHeight() / 2));
		final int yAdjust = labelCenter - (height / 2);
		
		if (value != null) {
			PetActionIcon.get(value).draw(graphics, x, yAdjust, width, height);
		}
		
		return new Rect2i(x, yAdjust, width, height);
	}

	
}