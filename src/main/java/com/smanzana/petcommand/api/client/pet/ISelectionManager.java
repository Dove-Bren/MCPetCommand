package com.smanzana.petcommand.api.client.pet;

import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.LivingEntity;

public interface ISelectionManager {
	
	public boolean isSelected(@Nonnull LivingEntity pet);

	public @Nonnull Set<LivingEntity> getSelectedPets();
	
	public void setPet(@Nonnull LivingEntity pet, boolean selected);
	
	default public void addPet(@Nonnull LivingEntity pet) {
		setPet(pet, true);
	}
	
	default public void removePet(@Nonnull LivingEntity pet) {
		setPet(pet, true);
	}
	
	public void clearSelection();
	
}
