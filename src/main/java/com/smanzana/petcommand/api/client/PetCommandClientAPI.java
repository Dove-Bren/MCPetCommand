package com.smanzana.petcommand.api.client;

import javax.annotation.Nullable;

import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.client.pet.ISelectionManager;

/**
 * API available to clients. Includes common API funcs too, like getting the targetting manager.
 */
public abstract class PetCommandClientAPI extends PetCommandAPI {

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
	protected static @Nullable IPetCommandClientAPIProvider Impl;
	
	protected static final void ProvideImpl(IPetCommandClientAPIProvider api) {
		Impl = api;
		PetCommandAPI.ProvideImpl(api);
	}
}
