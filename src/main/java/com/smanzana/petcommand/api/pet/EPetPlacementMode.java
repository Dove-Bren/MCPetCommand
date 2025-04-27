package com.smanzana.petcommand.api.pet;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum EPetPlacementMode {
	FREE, // No constraint; move as desired
	HEEL_FOLLOW, // Follow owner closely, but behind them
	HEEL_DEFENSIVE, // Follow owner, guarding them
;
	
	private final Component name;
	private final Component desc;
	
	private EPetPlacementMode() {
		this.name = new TranslatableComponent("petplacement." + this.name().toLowerCase() + ".name");
		this.desc = new TranslatableComponent("petplacement." + this.name().toLowerCase() + ".desc");
	}
	
	public Component getName() {
		return this.name;
	}
	
	public Component getDescription() {
		return this.desc;
	}
	
}
