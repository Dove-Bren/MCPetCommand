package com.smanzana.petcommand.api.client.petgui.sheet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIRenderHelper;
import com.smanzana.petcommand.api.entity.IEntityPet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public abstract class PetInventorySheet<T extends LivingEntity & IEntityPet> implements IPetGUISheet<T> {
	
	protected final T pet;
	protected final Container petInv;
	
	public PetInventorySheet(T pet, Container petInventory) {
		this.pet = pet;
		this.petInv = petInventory;
	}
	
	@Override
	public void showSheet(T pet, Player player, IPetContainer<T> container, int width, int height, int offsetX, int offsetY) {
		final int cellWidth = 18;
		final int invRow = 9;
		final int invWidth = cellWidth * invRow;
		final int leftOffset = (width - invWidth) / 2;
		final int dragonTopOffset = 10;
		final int playerInvSize = 27 + 9;
		
		for (int i = 0; i < petInv.getContainerSize(); i++) {
			Slot slotIn = new Slot(petInv, i, leftOffset + offsetX + (cellWidth * (i % invRow)), dragonTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
		
		final int playerTopOffset = 100;
		Container playerInv = player.getInventory();
		for (int i = 0; i < playerInvSize; i++) {
			Slot slotIn = new Slot(playerInv, (i + 9) % 36, leftOffset + offsetX + (cellWidth * (i % invRow)),
					(i < 27 ? 0 : 10) + playerTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
	}

	@Override
	public void hideSheet(T pet, Player player, IPetContainer<T> container) {
		container.clearSlots();
	}

	@Override
	public void draw(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		// Draw sheet
		matrixStackIn.pushPose();
		{
			final int cellWidth = 18;
			final int invRow = 9;
			final int invWidth = cellWidth * invRow;
			final int leftOffset = (width - invWidth) / 2;
			final int dragonTopOffset = 10;
			final int playerInvSize = 27 + 9;
			
			// Pet slots
			matrixStackIn.pushPose();
			matrixStackIn.translate(leftOffset - 1, dragonTopOffset - 1, 0);
			PetGUIRenderHelper.DrawSlots(matrixStackIn, cellWidth, cellWidth, petInv.getContainerSize(), invRow);
			matrixStackIn.popPose();
			
			// Player slots
			final int playerTopOffset = 100;
			matrixStackIn.pushPose();
			matrixStackIn.translate(leftOffset - 1, playerTopOffset - 1, 0);
			// ... First 27
			PetGUIRenderHelper.DrawSlots(matrixStackIn, cellWidth, cellWidth, Math.min(27, playerInvSize), invRow);
			
			// Remaining (toolbar)
			final int yOffset = ((Math.min(27, playerInvSize) / invRow)) * cellWidth;
			matrixStackIn.translate(0, 10 + yOffset, 0);
			PetGUIRenderHelper.DrawSlots(matrixStackIn, cellWidth, cellWidth, Math.max(0, playerInvSize-27), invRow);
			
			matrixStackIn.popPose();
		}
		matrixStackIn.popPose();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return false;
	}

	@Override
	public void handleMessage(CompoundTag data) {
		
	}

	@Override
	public String getButtonText() {
		return "Backpack";
	}

	@Override
	public abstract boolean shouldShow(T dragon, IPetContainer<T> container);

	@Override
	public void overlay(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
