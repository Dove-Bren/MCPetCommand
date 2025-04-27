package com.smanzana.petcommand.entity.ai;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.pet.EPetPlacementMode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class FollowOwnerAdvancedGoal<T extends Mob> extends Goal {
	
	private final T thePet;
	private LivingEntity theOwner;
	private Level theWorld;
	private final double followSpeed;
	private final PathNavigation petPathfinder;
	private int timeToRecalcPath;
	private float maxDist;
	private float minDist;
	private float oldWaterCost;
	
	private @Nullable Vec3 lastPosition;
	private int timeToRecalcPosition; // measured in existTicks of pet
	
	protected Predicate<? super T> filter;

	public FollowOwnerAdvancedGoal(T thePetIn, double followSpeedIn, float minDistIn, float maxDistIn) {
		this(thePetIn, followSpeedIn, minDistIn, maxDistIn, null);
	}
	
	public FollowOwnerAdvancedGoal(T thePetIn, double followSpeedIn, float minDistIn, float maxDistIn, Predicate<? super T> filter) {
		this.thePet = thePetIn;
		this.theWorld = thePetIn.level;
		this.followSpeed = followSpeedIn;
		this.petPathfinder = thePetIn.getNavigation();
		this.minDist = minDistIn;
		this.maxDist = maxDistIn;
		lastPosition = null;
		timeToRecalcPosition = 0;
		
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		
		this.filter = filter;
	}
	
	protected boolean isPetSitting(T pet) {
		return PetFuncs.IsPetSitting(pet);
	}
	
	/**
	 * Weird function. We want a deterministic 'index' of the pet in some hypothetical sorted
	 * list of all pets for the owner.
	 * As of writing, I'm planning on actually making that list every time we need it and sorting by
	 * UUID
	 * @param pet
	 * @param owner
	 * @return
	 */
	protected int getPetPositionIndex(T pet, LivingEntity owner) {
		List<LivingEntity> pets = PetFuncs.GetTamedEntities(owner);
		pets.removeIf((p) -> {
			return p == null
					|| PetFuncs.IsPetSitting(p)
					|| thePet.isPassenger()
					|| thePet.hasIndirectPassenger(this.theOwner);
		});
		if (!pets.contains(pet)) {
			return 0;
		}
		Collections.sort(pets, (a, b) -> {
			return a.getUUID().compareTo(b.getUUID());
		});
		return pets.indexOf(pet);
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
	protected Vec3 getIdealTargetPosition(T pet, LivingEntity owner, EPetPlacementMode mode) {
		final int index = getPetPositionIndex(pet, owner);
		final Vec3 target;
		
		switch (mode) {
		case HEEL_DEFENSIVE:
		{
			// For defensive mode, be in front and then around the player.
			// 
			//   . .   . .  
			//  . .  +  . .  
			//   . .   . .   
			// 
			// Where each shell is filled in from inside to out, beginning from
			// the front of the player and moving to the back alternating left and
			// right sides as it goes.
			// Shell positioning will be fixed offsets, with outer shells being the same
			// offsets as inner + a x shift left or right depending on which side it's already on.
			final float spacing = 1.5f;
			final float bulge = 1f;
			float xs[] = {-spacing, spacing, -spacing - bulge, spacing + bulge, -spacing, spacing};
			float zs[] = {spacing, spacing, 0, 0, -spacing, -spacing};
			
			final int shell = (index / xs.length);
			final float offsetX = xs[index % xs.length];
			final float offsetZ = zs[index % zs.length];
			final float adjX = Math.signum(offsetX) * spacing * shell;
			
			// Get owner rotation to apply to
			final Vec3 ownerMoveVec = PetCommand.GetMovementListener().getLastMove(owner);
			final float yawOwnerRad = (float) Math.atan2(ownerMoveVec.x, ownerMoveVec.z);
			
			// Get offset first as if owner was facing 
			Vec3 offset = new Vec3(offsetX + adjX, 0, offsetZ);
			offset = offset.yRot(yawOwnerRad + .0f * (float) Math.PI);
			target = owner.position().add(offset);
			
			break;
		}
		case HEEL_FOLLOW:
		{
			// For follow mode, trail behind player in triangle
			// Skip row 1 of triangle
			//      +
			//     . .
			//    . . .
			//   . . . .
			// Gonna do with angles. 90 degree spread with first row being 0 and 90, second being 0 45 90 etc
			// with magnitude determined by row. will be more of a curved triangle than diagramed above.
			int rowCount = index;
			int row = 0;
			while (rowCount >= row+2) {
				rowCount -= row+2;
				row++;
			}
			
			// row is which 'shell' we're in.
			// rowCount is which position in that shell.
			final int rowMaxIndex = row+2;
			final float angle = 90f * ((float)rowCount / (float)(rowMaxIndex-1)); // 0,90 for row==0, 0,45,90 for row==1
			final double angleRad = 2 * Math.PI * (angle/360f);
			final float magnitude = 1.5f + (1.5f * row);
			
			// Get owner rotation to apply to
			final Vec3 ownerMoveVec = PetCommand.GetMovementListener().getLastMove(owner);
			final float yawOwnerRad = (float) Math.atan2(ownerMoveVec.x, ownerMoveVec.z);
			
			// Get offset first as if owner was facing 
			Vec3 offset = new Vec3(magnitude * Math.cos(angleRad), 0, magnitude * Math.sin(angleRad));
			offset = offset.yRot(yawOwnerRad + .75f * (float) Math.PI);
			target = owner.position().add(offset);
			
			break;
		}
		case FREE:
		default:
			// Free shouldn't ever get here, but return owner position just in case
			target = owner.position();
			break;
		}
		
		return target;
	}
	
	protected Vec3 getTargetPosition(T pet, LivingEntity owner, EPetPlacementMode mode) {
		if (timeToRecalcPosition == 0 || timeToRecalcPosition < pet.tickCount) {
			timeToRecalcPosition = pet.tickCount + 20;
			lastPosition = getIdealTargetPosition(pet, owner, mode);
			
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

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean canUse() {
		final LivingEntity entitylivingbase = PetFuncs.GetOwner(thePet);
		
		if (entitylivingbase == null) {
			return false;
		}
		
		if (thePet.getTarget() != null) {
			return false;
		}
		
		final EPetPlacementMode mode = PetCommand.GetPetCommandManager().getPlacementMode(entitylivingbase);
		
		if (mode == EPetPlacementMode.FREE) {
			return false;
		}
		
		final boolean sitting = this.isPetSitting(thePet);
		final Vec3 targetPos = getTargetPosition(thePet, entitylivingbase, mode);

		if (entitylivingbase instanceof Player && ((Player)entitylivingbase).isSpectator()) {
			return false;
		} else if (sitting) {
			return false;
		} else if (this.thePet.distanceToSqr(targetPos.x, targetPos.y, targetPos.z) < (double)(this.minDist * this.minDist)) {
			return false;
		} else if (this.filter != null && !this.filter.apply(this.thePet)) {
			return false;
		} else {
			this.theOwner = entitylivingbase;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean canContinueToUse() {
		if (this.thePet.getTarget() != null) {
			return false;
		}
		
		final EPetPlacementMode mode = PetCommand.GetPetCommandManager().getPlacementMode(this.theOwner);
		if (mode == EPetPlacementMode.FREE) {
			return false;
		}
		final boolean sitting = this.isPetSitting(thePet);
		
		if (sitting || thePet.isPassenger() || thePet.hasIndirectPassenger(this.theOwner)) {
			return false;
		}
		
		final Vec3 targetPos = getTargetPosition(thePet, theOwner, mode);
		return !this.petPathfinder.isDone() && this.thePet.distanceToSqr(targetPos.x, targetPos.y, targetPos.z) > (double)(this.maxDist * this.maxDist);
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.timeToRecalcPath = 0;
		this.timeToRecalcPosition = 0;
		this.oldWaterCost = this.thePet.getPathfindingMalus(BlockPathTypes.WATER);
		this.thePet.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
	}

	/**
	 * Resets the task
	 */
	public void stop() {
		this.theOwner = null;
		this.petPathfinder.stop();
		this.thePet.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
	}

	/**
	 * Updates the task
	 */
	public void tick() {
		//this.thePet.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F, (float)this.thePet.getVerticalFaceSpeed());

		if (!isPetSitting(thePet)) {
//			final Vec3 ownerMoveVec = NostrumMagica.playerListener.getLastMove(theOwner);
//			final float yawOwner = (float) -Math.atan2(ownerMoveVec.x, ownerMoveVec.z) * 180f / (float)Math.PI;
//			this.thePet.getLookHelper().setLookPosition(x, y, z, deltaYaw, deltaPitch);
			
			//System.out.println("Moving");
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;
				final EPetPlacementMode mode = PetCommand.GetPetCommandManager().getPlacementMode(this.theOwner);
				final Vec3 targetPos = this.getTargetPosition(thePet, theOwner, mode);

				//thePet.setLocationAndAngles(targetPos.x, targetPos.y, targetPos.z, this.thePet.rotationYaw, this.thePet.rotationPitch);
				if (!this.petPathfinder.moveTo(targetPos.x, targetPos.y, targetPos.z, this.followSpeed)) {
					if (!this.thePet.isLeashed()) {
						if (this.thePet.distanceToSqr(this.theOwner) >= 144.0D) {
							thePet.moveTo(targetPos.x, targetPos.y, targetPos.z, this.thePet.getYRot(), this.thePet.getXRot());
						}
					}
				}
			}
		}
	}
	
}
