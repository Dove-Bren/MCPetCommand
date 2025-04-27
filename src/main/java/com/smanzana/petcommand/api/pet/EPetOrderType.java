package com.smanzana.petcommand.api.pet;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * A command given to pets to influence what they're doing
 */
public enum EPetOrderType {
	STAY,
	MOVE_TO_POS,
	MOVE_TO_ME,
	GUARD_POS,
	;
	
	private final Component name;
	private final Component desc;
	
	private EPetOrderType() {
		this.name = new TranslatableComponent("petcommand." + this.name().toLowerCase() + ".name");
		this.desc = new TranslatableComponent("petcommand." + this.name().toLowerCase() + ".desc");
	}
	
	public Component getName() {
		return this.name;
	}
	
	public Component getDescription() {
		return this.desc;
	}
}