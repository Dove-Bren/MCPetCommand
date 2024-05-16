package com.smanzana.petcommand.api;

import javax.annotation.Nullable;

import com.smanzana.petcommand.api.entity.IEntityPet;

import net.minecraft.entity.player.PlayerEntity;

public abstract class PetCommandAPI {

	public static final void OpenPetGUI(PlayerEntity player, IEntityPet pet) {
		if (Impl != null) {
			Impl.openPetGUI(player, pet);
		}
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
}
