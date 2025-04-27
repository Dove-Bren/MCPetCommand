package com.smanzana.petcommand.api;

import javax.annotation.Nullable;

import com.smanzana.petcommand.api.pet.IPetOrderManager;
import com.smanzana.petcommand.api.pet.ITargetManager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class PetCommandAPI {

	public static final void OpenPetGUI(Player player, LivingEntity pet) {
		if (Impl != null) {
			Impl.openPetGUI(player, pet);
		}
	}
	
	public static final ITargetManager GetTargetManager(LivingEntity entity) {
		if (Impl != null) {
			return Impl.getTargetManager(entity);
		}
		return null;
	}
	
	public static final IPetOrderManager GetPetOrderManager() {
		if (Impl != null) {
			return Impl.getPetOrderManager();
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////
	//   
	//                         Implementation                   //
	//
	//////////////////////////////////////////////////////////////
	protected static @Nullable IPetCommandAPIProvider Impl;
	
	public static final void ProvideImpl(IPetCommandAPIProvider api) {
		Impl = api;
	}
}
