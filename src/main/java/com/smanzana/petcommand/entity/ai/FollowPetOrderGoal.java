package com.smanzana.petcommand.entity.ai;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.pet.EPetOrderType;
import com.smanzana.petcommand.pet.PetOrder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Overrides most behaviors to follow the current pet behavior
 * @param <T>
 */
public class FollowPetOrderGoal<T extends Mob> extends Goal {
	
	private final T thePet;
	private LivingEntity theOwner;
	private Level theWorld;
	private final double followSpeed;
	private final PathNavigation petPathfinder;
	private int timeToRecalcPath;
	private float maxDist;
	private float oldWaterCost;
	
	private @Nullable Vec3 lastPosition;
	private int timeToRecalcPosition; // measured in existTicks of pet
	protected boolean shouldMoveToTarget;
	
	protected @Nullable PetOrder lastOrder;
	
	protected Predicate<? super T> filter;

	public FollowPetOrderGoal(T thePetIn, double followSpeedIn, float maxDistIn) {
		this(thePetIn, followSpeedIn, maxDistIn, null);
	}
	
	public FollowPetOrderGoal(T thePetIn, double followSpeedIn, float maxDistIn, Predicate<? super T> filter) {
		this.thePet = thePetIn;
		this.theWorld = thePetIn.level;
		this.followSpeed = followSpeedIn;
		this.petPathfinder = thePetIn.getNavigation();
		this.maxDist = maxDistIn;
		lastPosition = null;
		timeToRecalcPosition = 0;
		shouldMoveToTarget = true;
		
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.TARGET));
		
		this.filter = filter;
	}
	
	protected @Nullable PetOrder getCurrentOrder() {
		final LivingEntity owner = PetFuncs.GetOwner(thePet);
		return owner != null ? PetCommand.GetPetCommandManager().getPetOrder(owner, thePet) : null;
	}
	
	/**
	 * Calculate the position this entity should want to be at.
	 * Note this method is expected to have no caching on it and find the theoretical ideal
	 * location.
	 * Other layers take care of caching and adjusting the position to one that the pet can
	 * actually stand at.
	 * @param pet
	 * @param owner
	 * @param mode
	 * @return
	 */
	protected Vec3 getIdealTargetPosition(T pet, LivingEntity owner, @Nonnull PetOrder order) {
		final Vec3 target;
		
		switch (order.type()) {
		case STAY:
			target = pet.position(); // don't move
			break;
		case MOVE_TO_ME:
		default:
			target = owner.position();
			break;
		case GUARD_POS:
		case MOVE_TO_POS:
			target = Vec3.atBottomCenterOf(order.pos());
			break;
		}
		
		return target;
	}
	
	protected Vec3 getTargetPosition(T pet, LivingEntity owner, @Nonnull PetOrder order) {
		if (timeToRecalcPosition == 0 || timeToRecalcPosition < pet.tickCount) {
			timeToRecalcPosition = pet.tickCount + 20;
			lastPosition = getIdealTargetPosition(pet, owner, order);
			
			MutableBlockPos cursor = new MutableBlockPos();
			cursor.set(lastPosition.x, lastPosition.y, lastPosition.z);
			if (!isEmptyBlock(cursor) || isEmptyBlock(cursor.below())) {
				if (isEmptyBlock(cursor.below()) && !isEmptyBlock(cursor.below().below())) {
					lastPosition = lastPosition.add(0, -1, 0);
				} else if (isEmptyBlock(cursor.above()) && !isEmptyBlock(cursor)) {
					lastPosition = lastPosition.add(0, 1, 0);
				}
			}
		}
		
		return lastPosition; 
	}

	private boolean isEmptyBlock(BlockPos pos) {
		return this.theWorld.isEmptyBlock(pos);
	}
	
	protected @Nullable LivingEntity getGuardTarget(T pet, LivingEntity owner, @Nonnull PetOrder order) {
		List<LivingEntity> tamed = (owner == null ? Lists.newArrayList() : PetFuncs.GetTamedEntities(owner));
		List<Entity> entities = pet.getLevel().getEntities(pet, AABB.ofSize(Vec3.atBottomCenterOf(order.pos()), 12, 6, 12), (e) -> {
			return e instanceof LivingEntity
					&& e != pet
					&& e != owner
					&& !tamed.contains(e)
					&& !PetFuncs.IsSameTeam(pet, (LivingEntity) e);
		});
		Collections.sort(entities, (a, b) -> {
			return (int) (a.distanceToSqr(pet) - b.distanceToSqr(pet));
		});
		return entities.isEmpty() ? null : (LivingEntity)entities.get(0);
	}
	
	protected void finishOrder(T pet, LivingEntity owner) {
		PetCommand.GetPetCommandManager().setPetOrder(owner, pet, null);
	}
	
	protected void onOrderStart(T pet, LivingEntity owner, @Nonnull PetOrder order) {
		// Only one that really has a startup thing is the STAY, which may get away with being a one-time thing
		if (order.type() == EPetOrderType.STAY) {
			if (PetFuncs.TryToSitPet(pet, true)) {
				// It worked, so can clear out this order
				finishOrder(pet, owner);
			}
		} else {
			// Try not to be sitting if we are so we can do what we need to do
			PetFuncs.TryToSitPet(pet, false);
		}
	}
	
	protected void onOrderTick(T pet, LivingEntity owner, @Nonnull PetOrder order, boolean inPosition) {
		switch (order.type()) {
		// Some things want to be done when they are in position
		case MOVE_TO_ME:
		case MOVE_TO_POS:
			if (inPosition) {
				finishOrder(pet, owner);
			}
			break;
		case GUARD_POS:
			if (inPosition && (pet.getTarget() == null || pet.getTarget().isDeadOrDying() || pet.distanceTo(pet.getTarget()) > 10)) {
				final LivingEntity newTarg = getGuardTarget(pet, owner, order);
				if (newTarg != null) {
					pet.setTarget(newTarg);
				}
			}
			
			break;
		// Some things tick until removed
		case STAY:
		default:
			break;
		}
	}

	protected boolean shouldMoveToTarget(T pet, LivingEntity owner, PetOrder order, Vec3 targetPos) {
		switch (order.type()) {
		case MOVE_TO_ME:
		case MOVE_TO_POS:
		default:
			return true;
		

		case STAY:
			return false;

		case GUARD_POS:
			return pet.distanceToSqr(targetPos) > 100 || pet.getTarget() == null;
		}
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	@Override
	public boolean canUse() {
		final LivingEntity entitylivingbase = PetFuncs.GetOwner(thePet);
		
		if (entitylivingbase == null) {
			return false;
		}
		
		final PetOrder order = this.getCurrentOrder();
		if (order == null) {
			return false;
		}
		
		if (order.type() == EPetOrderType.GUARD_POS) {
			final Vec3 targetPos = getTargetPosition(thePet, theOwner, order);
			if (!this.shouldMoveToTarget(thePet, theOwner, order, targetPos)
					&& (thePet.getTarget() != null && !thePet.getTarget().isDeadOrDying() && thePet.distanceTo(thePet.getTarget()) < 10)) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean canContinueToUse() {
		final PetOrder order = this.getCurrentOrder();
		if (order == null) {
			return false;
		}
		
		if (thePet.isPassenger()) {
			return false;
		}
		
		if (order.type() == EPetOrderType.GUARD_POS) {
			final Vec3 targetPos = getTargetPosition(thePet, theOwner, order);
			if (!this.shouldMoveToTarget(thePet, theOwner, order, targetPos)
					&& (thePet.getTarget() != null && !thePet.getTarget().isDeadOrDying() && thePet.distanceTo(thePet.getTarget()) < 10)) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.timeToRecalcPath = 0;
		this.timeToRecalcPosition = 0;
		this.oldWaterCost = this.thePet.getPathfindingMalus(BlockPathTypes.WATER);
		this.thePet.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
		this.theOwner = PetFuncs.GetOwner(thePet);
		
		this.onOrderStart(thePet, theOwner, getCurrentOrder());
	}

	/**
	 * Resets the task
	 */
	public void stop() {
		this.theOwner = null;
		this.petPathfinder.stop();
		this.thePet.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
		this.lastOrder = null;
	}

	/**
	 * Updates the task
	 */
	public void tick() {
		final PetOrder order = this.getCurrentOrder();
		if (order == null) {
			return;
		}
		
		if (theOwner == null) {
			return;
		}
		
		if (lastOrder != null && !lastOrder.equals(order)) {
			// Order changed without ever stopping this AI task, so fake a start
			this.start();
		}
		lastOrder = order;
		
		// Calculate how far from where we want to be we are
		final Vec3 targetPos = getTargetPosition(thePet, theOwner, order);
		final float reqDist = (order.type() == EPetOrderType.MOVE_TO_ME ? 2 :
			order.type() == EPetOrderType.GUARD_POS ? 10
					: this.maxDist);
		final boolean inPosition = this.thePet.distanceToSqr(targetPos.x, targetPos.y, targetPos.z) <= (double)(reqDist * reqDist);
		this.onOrderTick(thePet, theOwner, order, inPosition);
		
		if (shouldMoveToTarget(thePet, theOwner, order, targetPos)) {
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;
				if (!this.petPathfinder.moveTo(targetPos.x, targetPos.y, targetPos.z, this.followSpeed)) {
					// Teleport if too far away
					if (!this.thePet.isLeashed()) {
						if (this.thePet.distanceToSqr(this.theOwner) >= 144.0D) {
							thePet.moveTo(targetPos.x, targetPos.y, targetPos.z, this.thePet.getYRot(), this.thePet.getXRot());
						}
					}
				}
			}
		} else if (this.petPathfinder.isInProgress()) {
			this.petPathfinder.stop();
		}
	}
	
}
