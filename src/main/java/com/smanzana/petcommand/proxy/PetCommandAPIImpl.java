package com.smanzana.petcommand.proxy;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.IPetCommandAPIProvider;
import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.pet.IPetOrderManager;
import com.smanzana.petcommand.api.pet.ITargetManager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PetCommandAPIImpl implements IPetCommandAPIProvider {
	
	public static boolean Register() {
		new PetCommandAPIImpl();
		return true;
	}
	
	protected PetCommandAPIImpl() {
		PetCommandAPI.ProvideImpl(this);
	}

	@Override
	public void openPetGUI(Player player, LivingEntity pet) {
		PetCommand.GetProxy().openPetGUI(player, pet);
	}

	@Override
	public ITargetManager getTargetManager(LivingEntity entity) {
		return PetCommand.GetProxy().getTargetManager(entity);
	}

	@Override
	public IPetOrderManager getPetOrderManager() {
		return PetCommand.GetPetCommandManager();
	}
}
