package com.smanzana.petcommand.client.widgetdupe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public abstract class ParentWidget<W extends ParentWidget<W, T>, T extends ObscurableChildWidget<?>> extends ObscurableChildWidget<W> {
	
	protected final List<T> children;
	
	public ParentWidget(int x, int y, int width, int height, Component title) {
		super(x, y, width, height, title);
		children = new ArrayList<>();
	}
	
	public void addChild(T child) {
		children.add(child);
		child.setParent(this);
	}
	
	protected void addChildren(Collection<T> children) {
		this.children.addAll(children);
		for (T child : children) {
			child.setParent(this);
		}
	}
	
	protected void clearChildren() {
		for (T child : children) {
			child.setParent(null);
		}
		children.clear();
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		//super.renderWidget(graphics, mouseX, mouseY, partialTicks);
		
		if (this.visible) {
			for (T widget : children) {
				widget.render(graphics, mouseX, mouseY, partialTicks);
			}
		}
		
		renderForeground(graphics, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void renderToolTip(GuiGraphics matrixStack, int mouseX, int mouseY) {
		for (T widget : children) {
			widget.renderToolTip(matrixStack, mouseX, mouseY);
		}
	}
	
	/**
	 * Render foreground media. Notably, this is called after children have been rendered.
	 * @param graphics
	 * @param mouseX
	 * @param mouseY
	 * @param partialTicks
	 */
	protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Try children first since they're probably on TOP of the widget
		if (this.visible) {
			for (T widget : children) {
				if (widget.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	// By default, don't do any mouse processing ourselves
	@Override
	protected boolean isValidClickButton(int button) {
		return false; // no click consumption
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		// Try children first since they're probably on TOP of the widget
		if (this.visible) {
			for (T widget : children) {
				if (widget.mouseReleased(mouseX, mouseY, button)) {
					// Vanilla is dumb and has 'am I under it' checks in mouse click but not for mouse released. That means
					// things blindly return 'true' to whether they've handled it or not.
					// So instead of caring, call it on every child and ignore return.
					; //return true;
				}
			}
		}
		
		//return super.mouseReleased(mouseX, mouseY, button);
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		// Try children first since they're probably on TOP of the widget
		if (this.visible) {
			for (T widget : children) {
				if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
					// Like 'mouseReleased', vanilla classes blindly 'handle' thi s. So do all children even if one returns true.
					//return true;
				}
			}
		}
		
		//return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		return false;
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		// Try children first since they're probably on TOP of the widget
		if (this.visible) {
			for (T widget : children) {
				widget.mouseMoved(mouseX, mouseY);
			}
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		// Try children first since they're probably on TOP of the widget
		if (this.visible) {
			for (T widget : children) {
				if (widget.mouseScrolled(mouseX, mouseY, delta)) {
					return true;
				}
			}
		}
		
		//return super.mouseScrolled(mouseX, mouseY, delta);
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// Try children first since they're probably on TOP of the widget
		if (this.visible) {
			for (T widget : children) {
				if (widget.keyPressed(keyCode, scanCode, modifiers)) {
					return true;
				}
			}
		}
		
		//return super.keyPressed(keyCode, scanCode, modifiers);
		return false;
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		// Try children first since they're probably on TOP of the widget
		if (this.visible) {
			for (T widget : children) {
				if (widget.keyReleased(keyCode, scanCode, modifiers)) {
					return true;
				}
			}
		}
		
		//return super.keyReleased(keyCode, scanCode, modifiers);
		return false;
	}
	
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		// Try children first since they're probably on TOP of the widget
		if (this.visible) {
			for (T widget : children) {
				if (widget.charTyped(codePoint, modifiers)) {
					return true;
				}
			}
		}
		
		//return super.charTyped(codePoint, modifiers);
		return false;
	}
	
	protected Rect2i getBounds() {
		return new Rect2i(getX(), getY(), this.width, this.height);
	}
	
	protected void updateChildPosition(T widget, Rect2i bounds) {
		widget.setBounds(bounds);
		widget.snapToParent(getX(), getY());
	}
	
	/**
	 * We've just been repositioned and need to update child positions and bounds
	 */
	protected void updateChildPositions() {
		final Rect2i bounds = getBounds();
		
		for (T widget : children) {
			updateChildPosition(widget, bounds);
		}
	}
	
	@Override
	public void setPosition(int x, int y) {
		super.setPosition(x, y);
		
		this.updateChildPositions();
	}

}
