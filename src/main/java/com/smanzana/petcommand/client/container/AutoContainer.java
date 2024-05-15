package com.smanzana.petcommand.client.container;

import javax.annotation.Nullable;

import com.smanzana.petcommand.util.ContainerUtil.IAutoContainerInventory;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Container that automatically detects changes in backing inventory's fields and syncs them.
 * @author Skyler
 *
 */
public abstract class AutoContainer extends Container {

	private final @Nullable IAutoContainerInventory inventory;
	
	public AutoContainer(ContainerType<? extends AutoContainer> type, int windowId, @Nullable IAutoContainerInventory inventory) {
		super(type, windowId);
		this.inventory = inventory;
		if (inventory != null) {
			this.trackIntArray(inventory);
		}
	}
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void updateProgressBar(int id, int data) {
		super.updateProgressBar(id, data);
	}
}
