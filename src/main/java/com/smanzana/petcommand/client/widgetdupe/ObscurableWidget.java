package com.smanzana.petcommand.client.widgetdupe;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public abstract class ObscurableWidget extends FixedWidget {

	protected @Nullable Rect2i bounds;
	protected boolean hidden;
	
	public ObscurableWidget(int x, int y, int width, int height, Component label) {
		super(x, y, width, height, label);
		bounds = null;
		hidden = false;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public boolean isHidden() {
		return this.hidden;
	}
	
	public void setBounds(Rect2i bounds) {
		this.bounds = bounds;
	}
	
	public void setBounds(int x, int y, int width, int height) {
		setBounds(new Rect2i(x, y, width, height));
	}
	
	protected boolean inBounds() {
		if (this.bounds != null) {
			final int x = this.getX();
			final int y = this.getY();
			final int maxX = x + this.width;
			final int maxY = y + this.height;
			final int boundsMaxX = bounds.getX() + bounds.getWidth();
			final int boundsMaxY = bounds.getY() + bounds.getHeight();
			return x < boundsMaxX
					&& maxX > bounds.getX()
					&& y < boundsMaxY
					&& maxY > bounds.getY();
		}
		
		return true;
	}
	
	@Override
	public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		// Check bounds
		this.visible = !isHidden() && inBounds();
		super.render(graphics, mouseX, mouseY, partialTicks);
	}
	
}
