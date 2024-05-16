package com.smanzana.petcommand.api.client.petgui.sheet;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIRenderHelper;
import com.smanzana.petcommand.api.entity.IEntityPet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;

public abstract class PetInventorySheet<T extends IEntityPet> implements IPetGUISheet<T> {
	
	protected final T pet;
	protected final IInventory petInv;
	
	public PetInventorySheet(T pet, IInventory petInventory) {
		this.pet = pet;
		this.petInv = petInventory;
	}
	
	@Override
	public void showSheet(T pet, PlayerEntity player, IPetContainer<T> container, int width, int height, int offsetX, int offsetY) {
		final int cellWidth = 18;
		final int invRow = 9;
		final int invWidth = cellWidth * invRow;
		final int leftOffset = (width - invWidth) / 2;
		final int dragonTopOffset = 10;
		final int playerInvSize = 27 + 9;
		
		for (int i = 0; i < petInv.getSizeInventory(); i++) {
			Slot slotIn = new Slot(petInv, i, leftOffset + offsetX + (cellWidth * (i % invRow)), dragonTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
		
		final int playerTopOffset = 100;
		IInventory playerInv = player.inventory;
		for (int i = 0; i < playerInvSize; i++) {
			Slot slotIn = new Slot(playerInv, (i + 9) % 36, leftOffset + offsetX + (cellWidth * (i % invRow)),
					(i < 27 ? 0 : 10) + playerTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
	}

	@Override
	public void hideSheet(T pet, PlayerEntity player, IPetContainer<T> container) {
		container.clearSlots();
	}

	@Override
	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		// Draw sheet
		matrixStackIn.push();
		{
			final int cellWidth = 18;
			final int invRow = 9;
			final int invWidth = cellWidth * invRow;
			final int leftOffset = (width - invWidth) / 2;
			final int dragonTopOffset = 10;
			final int playerInvSize = 27 + 9;
			
			// Pet slots
			matrixStackIn.push();
			matrixStackIn.translate(leftOffset - 1, dragonTopOffset - 1, 0);
			PetGUIRenderHelper.DrawSlots(matrixStackIn, cellWidth, cellWidth, petInv.getSizeInventory(), invRow);
			matrixStackIn.pop();
			
			// Player slots
			final int playerTopOffset = 100;
			matrixStackIn.push();
			matrixStackIn.translate(leftOffset - 1, playerTopOffset - 1, 0);
			// ... First 27
			PetGUIRenderHelper.DrawSlots(matrixStackIn, cellWidth, cellWidth, Math.min(27, playerInvSize), invRow);
			
			// Remaining (toolbar)
			final int yOffset = ((Math.min(27, playerInvSize) / invRow)) * cellWidth;
			matrixStackIn.translate(0, 10 + yOffset, 0);
			PetGUIRenderHelper.DrawSlots(matrixStackIn, cellWidth, cellWidth, Math.max(0, playerInvSize-27), invRow);
			
			matrixStackIn.pop();
		}
		matrixStackIn.pop();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return false;
	}

	@Override
	public void handleMessage(CompoundNBT data) {
		
	}

	@Override
	public String getButtonText() {
		return "Backpack";
	}

	@Override
	public abstract boolean shouldShow(T dragon, IPetContainer<T> container);

	@Override
	public void overlay(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
