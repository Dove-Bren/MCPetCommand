package com.smanzana.petcommand.api.entity;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Like Vanilla's EntityTameable, but an interface instead
 * @author Skyler
 *
 */
public interface ITameableEntity {

	public @Nullable Entity getOwner();
	
	/**
	 * Returns whether this entity has been tamed and, thus, has an owner.
	 * @return
	 */
	public boolean isEntityTamed();
	
	/**
	 * Updated getOwner call. We must be owned by an LivingEntity
	 */
	default public LivingEntity getLivingOwner() {
		Entity owner = this.getOwner();
		if (owner instanceof LivingEntity) {
			return (LivingEntity) owner;
		}
		return null;
	}

	/**
	 * Returns whether the entity is sitting.
	 * Not all entities must implement sitting. Simply always return false.
	 * @return
	 */
	public boolean isEntitySitting();
	
	/**
	 * Attempt to command the pet to sit.
	 * @param sitting
	 * @return whether the pet is sitting as a result of this call
	 */
	public boolean setEntitySitting(boolean sitting);
	
}
