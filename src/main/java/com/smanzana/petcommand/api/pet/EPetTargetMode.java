package com.smanzana.petcommand.api.pet;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum EPetTargetMode {
	FREE, // Follow regular target rules for mob
	DEFENSIVE, // Attack only when we or owner is attacked
	AGGRESSIVE, // Attack anything that might turn aggressive
	PASSIVE, // Wait for command
	;
	
	private final Component name;
	private final Component desc;
	
	private EPetTargetMode() {
		this.name = new TranslatableComponent("pettarget." + this.name().toLowerCase() + ".name");
		this.desc = new TranslatableComponent("pettarget." + this.name().toLowerCase() + ".desc");
	}
	
	public Component getName() {
		return this.name;
	}
	
	public Component getDescription() {
		return this.desc;
	}
}
