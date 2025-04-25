package com.smanzana.petcommand.api;

import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.ITargetManager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface IPetCommandAPIProvider {

	public void openPetGUI(Player player, IEntityPet pet);

	public ITargetManager getTargetManager(LivingEntity entity);
}
