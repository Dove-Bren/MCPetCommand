package com.smanzana.petcommand.entity.ai;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.pet.PetTargetMode;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;

public class PetTargetGoal<T extends CreatureEntity> extends TargetGoal {
	
	public static final EntityPredicate Filter = new EntityPredicate().allowUnseeable();
	
	protected T thePet;
	protected LivingEntity theOwner;
	protected int targetTicks;
	
	public PetTargetGoal(T petIn) {
		super(petIn, false);
		this.thePet = petIn;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean canUse() {
		final LivingEntity entitylivingbase = PetFuncs.GetOwner(thePet);
		
		if (entitylivingbase == null) {
			return false;
		}
		
		final PetTargetMode mode = PetCommand.GetPetCommandManager().getTargetMode(entitylivingbase);
		
		if (mode == PetTargetMode.FREE) {
			return false;
		}
		
		theOwner = entitylivingbase;
		return true;
	}
	
	@Override
	public boolean canContinueToUse() {
		return this.canUse();
	}
	
	protected static @Nullable LivingEntity FindAggressiveTarget(MobEntity attacker, double range) {
		LivingEntity owner = PetFuncs.GetOwner(attacker);
		List<LivingEntity> tamed = (owner == null ? Lists.newArrayList() : PetFuncs.GetTamedEntities(owner));
		List<Entity> entities = attacker.level.getEntities(attacker, attacker.getBoundingBox().inflate(range), (e) -> {
			return e instanceof LivingEntity
					&& e != attacker
					&& e != owner
					&& !tamed.contains(e)
					&& Filter.test(attacker, (LivingEntity) e)
					&& !PetFuncs.IsSameTeam(attacker, (LivingEntity) e);
		});
		Collections.sort(entities, (a, b) -> {
			return (int) (a.distanceToSqr(attacker) - b.distanceToSqr(attacker));
		});
		return entities.isEmpty() ? null : (LivingEntity)entities.get(0);
	}
	
	protected @Nullable LivingEntity findAggressiveTarget(T thePet) {
		return FindAggressiveTarget(thePet, 10);
	}
	
	@Override
	public void tick() {
		if (targetTicks > 0) {
			targetTicks--;
		}
		
		if (thePet.getTarget() != null) {
			if (!thePet.getTarget().isAlive()) {
				thePet.setTarget(null);
			}
		}
		
		final PetTargetMode mode = PetCommand.GetPetCommandManager().getTargetMode(theOwner);
		switch (mode) {
		case AGGRESSIVE:
			if (thePet.getTarget() == null && targetTicks <= 0) {
				targetTicks = 20;
				thePet.setTarget(findAggressiveTarget(thePet));
			}
			break;
		case DEFENSIVE:
			if (theOwner.getLastHurtByMob() != null) {
				thePet.setTarget(theOwner.getLastHurtByMob());
			}
			break;
		case FREE:
			; // Shouldn't ever get here
			break;
		case PASSIVE:
			; // Don't automatically set target at all
			break;
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void start() {
		tick();

		super.start();
	}
}
