package com.smanzana.petcommand.client.input;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

public final class PetCommandKeyBindings {
	
	public static final KeyMapping bindingPetPlacementModeCycle = new KeyMapping("key.pet.placementmode.desc", InputConstants.UNKNOWN.getValue(), "key.PetCommand.desc");
	public static final KeyMapping bindingPetTargetModeCycle = new KeyMapping("key.pet.targetmode.desc", InputConstants.UNKNOWN.getValue(), "key.PetCommand.desc");
	public static final KeyMapping bindingPetAttackAll = new KeyMapping("key.pet.attackall.desc", GLFW.GLFW_KEY_X, "key.PetCommand.desc");
	public static final KeyMapping bindingPetAttack = new KeyMapping("key.pet.attack.desc", GLFW.GLFW_KEY_C, "key.PetCommand.desc");
	public static final KeyMapping bindingPetAllStop = new KeyMapping("key.pet.stopall.desc", GLFW.GLFW_KEY_L, "key.PetCommand.desc");
	public static final KeyMapping bindingPetScreen = new KeyMapping("key.pet.screen.desc", GLFW.GLFW_KEY_P, "key.PetCommand.desc");
	public static final KeyMapping bindingPetOrderModifier = new KeyMapping("key.pet.order.desc", GLFW.GLFW_KEY_LEFT_CONTROL, "key.PetCommand.desc");
}
