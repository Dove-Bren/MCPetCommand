package com.smanzana.petcommand.client.container;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Just adds the default render that used to be in GuiContainer
 * @author Skyler
 *
 */
public abstract class AutoGuiContainer<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	protected final Minecraft mc;
	
	public AutoGuiContainer(T inventorySlotsIn, Inventory playerInv, Component name) {
		super(inventorySlotsIn, playerInv, name);
		mc = Minecraft.getInstance();
	}
	
	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);
	}
}
