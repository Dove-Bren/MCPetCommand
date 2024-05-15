package com.smanzana.petcommand.entity;

import java.util.UUID;

import com.smanzana.petcommand.client.container.IPetGUISheet;
import com.smanzana.petcommand.client.container.PetGUI;
import com.smanzana.petcommand.pet.PetInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface IEntityPet extends ITameableEntity {

	public PetInfo getPetSummary();
	
	default public void onAttackCommand(LivingEntity target) { if (this instanceof MobEntity) ((MobEntity) this).setAttackTarget(target); };
	
	default public void onStopCommand() { if (this instanceof MobEntity) ((MobEntity) this).setAttackTarget(null); };
	
	public UUID getPetID();
	
	public boolean isBigPet();
	
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(PlayerEntity player);
	
	public PetGUI.PetGUIStatAdapter<? extends IEntityPet> getGUIAdapter();
	
}
