package com.smanzana.petcommand.api.client.petgui;

import net.minecraft.entity.LivingEntity;

public interface PetGUIStatAdapter<T> {
	
	// Health/first bar
	default public float getHealth(T pet) { return ((LivingEntity) pet).getHealth(); }
	default public float getMaxHealth(T pet) { return ((LivingEntity) pet).getMaxHealth(); }
	default public String getHealthLabel(T pet) { return "Health"; }
	
	// Second bar (mana?)
	default public boolean supportsSecondaryAmt(T pet) { return true; }
	public float getSecondaryAmt(T pet);
	public float getMaxSecondaryAmt(T pet);
	public String getSecondaryLabel(T pet);
	
	// Third bar (bond?)
	default public boolean supportsTertiaryAmt(T pet) { return true; };
	public float getTertiaryAmt(T pet);
	public float getMaxTertiaryAmt(T pet);
	public String getTertiaryLabel(T pet);
	
	// Fourth bar (xp?)
	default public boolean supportsQuaternaryAmt(T pet) { return true; };
	public float getQuaternaryAmt(T pet);
	public float getMaxQuaternaryAmt(T pet);
	public String getQuaternaryLabel(T pet);
}