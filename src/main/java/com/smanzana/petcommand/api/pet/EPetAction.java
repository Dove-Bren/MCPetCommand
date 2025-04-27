package com.smanzana.petcommand.api.pet;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * A summary of what the pet is currently doing
 */
public enum EPetAction {
	STAY,
	ATTACK,
	IDLE,
	WORK,
	WAIT,
	GUARD,
	MOVE,
	;
	
	private final Component name;
	private final Component desc;
	
	private EPetAction() {
		this.name = new TranslatableComponent("petaction." + this.name().toLowerCase() + ".name");
		this.desc = new TranslatableComponent("petaction." + this.name().toLowerCase() + ".desc");
	}
	
	public Component getName() {
		return this.name;
	}
	
	public Component getDescription() {
		return this.desc;
	}
}