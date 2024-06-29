package com.smanzana.petcommand.api.pet;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;

public interface ITargetManager {

	public @Nullable LivingEntity getTarget(MobEntity mob);
	
	public void updateTarget(MobEntity mob, @Nullable LivingEntity target);
	
	public @Nonnull List<MobEntity> getEntitiesTargetting(LivingEntity target);
	
}
