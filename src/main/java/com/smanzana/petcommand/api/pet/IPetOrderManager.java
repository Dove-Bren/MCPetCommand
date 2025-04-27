package com.smanzana.petcommand.api.pet;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;

/**
 * Organizes orders given to pets, which pets act out before following general placement and targeting rules.
 */
public interface IPetOrderManager {
	
	public @Nullable EPetOrderType getCurrentOrder(LivingEntity owner, LivingEntity pet);
	
	public default boolean hasCurrentOrder(LivingEntity owner, LivingEntity pet) {
		return getCurrentOrder(owner, pet) != null;
	}
	
	public boolean clearOrder(LivingEntity owner, LivingEntity pet);

}
