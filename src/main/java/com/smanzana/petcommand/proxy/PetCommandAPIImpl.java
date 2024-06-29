package com.smanzana.petcommand.proxy;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.ITargetManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class PetCommandAPIImpl extends PetCommandAPI {
	
	public static void Register() {
		new PetCommandAPIImpl();
	}
	
	private PetCommandAPIImpl() {
		PetCommandAPI.ProvideImpl(this);
	}

	@Override
	protected void openPetGUI(PlayerEntity player, IEntityPet pet) {
		PetCommand.GetProxy().openPetGUI(player, pet);
	}

	@Override
	protected ITargetManager getTargetManager(LivingEntity entity) {
		return PetCommand.GetProxy().getTargetManager(entity);
	}
}
