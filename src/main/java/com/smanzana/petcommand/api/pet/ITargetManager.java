package com.smanzana.petcommand.api.pet;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public interface ITargetManager {

	public @Nullable LivingEntity getTarget(Mob mob);
	
	public void updateTarget(Mob mob, @Nullable LivingEntity target);
	
	public @Nonnull List<Mob> getEntitiesTargetting(LivingEntity target);
	
}
