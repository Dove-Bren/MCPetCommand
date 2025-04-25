package com.smanzana.petcommand.api.client;

import com.smanzana.petcommand.api.IPetCommandAPIProvider;
import com.smanzana.petcommand.api.client.pet.ISelectionManager;

/**
 * API available to clients. Includes 
 */
public interface IPetCommandClientAPIProvider extends IPetCommandAPIProvider {

	public ISelectionManager getSelectionManager();
}
