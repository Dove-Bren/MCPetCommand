package com.smanzana.petcommand.api.client.container;

import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.entity.IEntityPet;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;

public interface IPetContainer<T extends IEntityPet> {

	public Container getContainer();
	
	public T getPet();
	
	public IPetGUISheet<T> getCurrentSheet();
	
	public void setSheet(int index);
	
	public int getSheetIndex();
	
	public void addSheetSlot(Slot slotIn);

	public void clearSlots();
	
	public int getSheetCount();
	
	// Sheets can call on their handle to the container to sync with the server.
	// This call doesn't check if it's on the server. It'll just 'send' it. Know what you're doing!
	public void sendSheetMessageToServer(CompoundNBT data);
	
	public void sendSheetMessageToClient(CompoundNBT data);
	
	public void dropContainerInventory(IInventory inventory);
	
}
