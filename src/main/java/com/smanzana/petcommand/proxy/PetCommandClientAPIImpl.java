package com.smanzana.petcommand.proxy;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.client.IPetCommandClientAPIProvider;
import com.smanzana.petcommand.api.client.PetCommandClientAPI;
import com.smanzana.petcommand.api.client.pet.ISelectionManager;
import com.smanzana.petcommand.api.client.render.IEntityOutliner;

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
		return ((ClientProxy) PetCommand.GetProxy()).getSelectionManager();
	}

	@Override
	public IEntityOutliner getEntityOutliner() {
		return ((ClientProxy) PetCommand.GetProxy()).getOutlineRenderer();
	}
}
