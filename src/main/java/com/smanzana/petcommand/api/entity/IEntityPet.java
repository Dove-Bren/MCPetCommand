package com.smanzana.petcommand.api.entity;

import java.awt.Color;
import java.util.UUID;

import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIStatAdapter;
import com.smanzana.petcommand.api.pet.PetInfo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public interface IEntityPet extends ITameableEntity {

	public PetInfo getPetSummary();
	
	default public void onAttackCommand(LivingEntity target) { if (this instanceof Mob) ((Mob) this).setTarget(target); };
	
	default public void onStopCommand() { if (this instanceof Mob) ((Mob) this).setTarget(null); };
	
	public UUID getPetID();
	
	public boolean isBigPet();
	
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(Player player);
	
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
