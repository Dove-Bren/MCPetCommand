package com.smanzana.petcommand.proxy;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.client.IPetCommandClientAPIProvider;
import com.smanzana.petcommand.api.client.PetCommandClientAPI;
import com.smanzana.petcommand.api.client.pet.ISelectionManager;

public class PetCommandClientAPIImpl extends PetCommandAPIImpl implements IPetCommandClientAPIProvider {
	
	public static boolean Register() {
		new PetCommandClientAPIImpl();
		return true;
	}
	
	protected PetCommandClientAPIImpl() {
		PetCommandClientAPI.ProvideImpl(this);
	}

	@Override
	public ISelectionManager getSelectionManager() {
		return PetCommand.GetProxy().getSelectionManager();
	}
}
