package com.smanzana.petcommand.proxy;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.client.pet.ISelectionManager;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.ITargetManager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PetCommandAPIImpl extends PetCommandAPI {
	
	public static void Register() {
		new PetCommandAPIImpl();
	}
	
	private PetCommandAPIImpl() {
		PetCommandAPI.ProvideImpl(this);
	}

	@Override
	protected void openPetGUI(Player player, IEntityPet pet) {
		PetCommand.GetProxy().openPetGUI(player, pet);
	}

	@Override
	protected ITargetManager getTargetManager(LivingEntity entity) {
		return PetCommand.GetProxy().getTargetManager(entity);
	}

	@Override
	protected ISelectionManager getSelectionManager() {
		return PetCommand.GetProxy().getSelectionManager();
	}
}
