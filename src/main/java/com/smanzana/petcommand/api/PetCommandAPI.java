package com.smanzana.petcommand.api;

import javax.annotation.Nullable;

import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.ITargetManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public abstract class PetCommandAPI {

	public static final void OpenPetGUI(PlayerEntity player, IEntityPet pet) {
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
	
	
	
	
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////
	//   
	//                         Implementation                   //
	//
	//////////////////////////////////////////////////////////////
	protected static @Nullable PetCommandAPI Impl;
	
	protected static final void ProvideImpl(PetCommandAPI api) {
		Impl = api;
	}
	
	protected abstract void openPetGUI(PlayerEntity player, IEntityPet pet);

	protected abstract ITargetManager getTargetManager(LivingEntity entity);
}
