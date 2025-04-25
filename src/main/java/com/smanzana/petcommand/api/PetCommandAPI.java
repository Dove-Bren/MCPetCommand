package com.smanzana.petcommand.api;

import javax.annotation.Nullable;

import com.smanzana.petcommand.api.client.pet.ISelectionManager;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.ITargetManager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class PetCommandAPI {

	public static final void OpenPetGUI(Player player, IEntityPet pet) {
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
	
	/**
	 * Get the selection manager, which is used to track what tamed entities the player has selected.
	 * Note that the selection manager is entirely client-side and will return null for dedicated servers.
	 * @return
	 */
	public static final ISelectionManager GetSelectionManager() {
		if (Impl != null) {
			return Impl.getSelectionManager();
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
	
	protected abstract void openPetGUI(Player player, IEntityPet pet);

	protected abstract ITargetManager getTargetManager(LivingEntity entity);
	
	protected abstract ISelectionManager getSelectionManager();
}
