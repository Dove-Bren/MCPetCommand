package com.smanzana.petcommand.api.entity;

import java.awt.Color;
import java.util.UUID;

import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIStatAdapter;
import com.smanzana.petcommand.api.pet.PetInfo;

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
	
	public PetGUIStatAdapter<? extends IEntityPet> getGUIAdapter();
	
	public default int getPetColor() {
		return MakeColorFromID(this.getPetID());
	}
	
	public static int MakeColorFromID(UUID id) {
		// By default, return random color based on UUID (so it's consistent)
		final float hue = (float) (id.hashCode() % 1000) / 1000f;
		return 0xFF000000 | Color.HSBtoRGB(hue, .7f, 1f);
	}
	
}
