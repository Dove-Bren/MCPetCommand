package com.smanzana.petcommand.api.client.container;

import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.entity.IEntityPet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public interface IPetContainer<T extends IEntityPet> {

	public AbstractContainerMenu getContainer();
	
	public T getPet();
	
	public IPetGUISheet<T> getCurrentSheet();
	
	public void setSheet(int index);
	
	public int getSheetIndex();
	
	public void addSheetSlot(Slot slotIn);

	public void clearSlots();
	
	public int getSheetCount();
	
	// Sheets can call on their handle to the container to sync with the server.
	// This call doesn't check if it's on the server. It'll just 'send' it. Know what you're doing!
	public void sendSheetMessageToServer(CompoundTag data);
	
	public void sendSheetMessageToClient(CompoundTag data);
	
	public void dropContainerInventory(Container inventory);
	
}
