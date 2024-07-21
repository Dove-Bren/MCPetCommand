package com.smanzana.petcommand.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

public class RayTrace {
	
	private static final @Nullable LivingEntity resolveLivingEntity(@Nullable Entity entityOrSubEntity) {
		if (entityOrSubEntity == null) {
			return null;
		}

		if (entityOrSubEntity instanceof LivingEntity) {
			return (LivingEntity) entityOrSubEntity;
		}
		
		// Multiparts aren't living but may have living parents!
		if (entityOrSubEntity instanceof PartEntity) {
			if (((PartEntity<?>) entityOrSubEntity).getParent() instanceof LivingEntity) {
				return (LivingEntity) ((PartEntity<?>) entityOrSubEntity).getParent();
			}
		}
		
//		// EnderDragons are multipart but with no interface anymore
//		if (entityOrSubEntity instanceof EnderDragonPart) {
//			if (((EnderDragonPart) entityOrSubEntity).parentMob instanceof LivingEntity) {
//				return (LivingEntity) ((EnderDragonPart) entityOrSubEntity).parentMob;
//			}
//		}

		return null;
	}
	
	public static class NotEntity implements Predicate<LivingEntity> {
		
		private LivingEntity self;
		
		public NotEntity(LivingEntity self) {
			this.self = self;
		}
		
		@Override
		public boolean test(LivingEntity input) {
			return (!input.is(self) && !input.is(self.getVehicle()));
		}
	}
	
	public static class LivingOnly implements Predicate<Entity> {
		@Override
		public boolean test(Entity input) {
			LivingEntity living = resolveLivingEntity(input);
			return living != null;
		}
	}
	
	public static class OtherLiving implements Predicate<Entity> {
		
		private NotEntity filterMe;
		private LivingOnly filterLiving;
		
		public OtherLiving(LivingEntity self) {
			this.filterMe = new NotEntity(self);
			this.filterLiving = new LivingOnly();
		}
		
		@Override
		public boolean test(Entity input) {
			if (filterLiving.test(input)) {
				// is LivingEntity
				return filterMe.test(resolveLivingEntity(input));
			}
			
			return false;
		}
	}
	
	public static Vec3 directionFromAngles(float pitch, float yaw) {
		float f = Mth.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
	}
	
	public static HitResult miss(Vec3 fromPos, Vec3 toPos) {
		Vec3 rayVec = toPos.subtract(fromPos);
    	return BlockHitResult.miss(fromPos, Direction.getNearest(rayVec.x, rayVec.y, rayVec.z), new BlockPos(fromPos));
	}
	
	public static @Nullable Entity entFromRaytrace(HitResult result) {
		if (result == null
				|| result.getType() != HitResult.Type.ENTITY) {
			return null;
		}
		
		return ((EntityHitResult) result).getEntity();
	}
	
	public static @Nullable LivingEntity livingFromRaytrace(HitResult result) {
		@Nullable Entity ent = entFromRaytrace(result);
		if (ent != null && ent instanceof LivingEntity) {
			return (LivingEntity) ent;
		}
		return null;
	}

	public static @Nullable BlockPos blockPosFromResult(HitResult result) {
		if (result == null || result.getType() != HitResult.Type.BLOCK) {
			return null;
		}
		
		return ((BlockHitResult) result).getBlockPos();
	}
	
	public static HitResult raytrace(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector) {
		if (world == null || fromPos == null)
			return null;
		
		return raytrace(world, tracingEntity, fromPos, directionFromAngles(pitch, yaw), maxDistance, selector);
	}
	
	public static HitResult raytraceApprox(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector, double nearbyRadius) {
		if (world == null || fromPos == null)
			return null;
		
		HitResult result = raytrace(world, tracingEntity, fromPos, pitch, yaw, maxDistance, selector);
		if (nearbyRadius > 0) {
			result = nearbyRayTrace(world, result, nearbyRadius, selector);
		}
		return result;
	}
	
	public static HitResult raytrace(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos,
			Vec3 direction, float maxDistance, Predicate<? super Entity> selector) {
		Vec3 toPos;
		
		if (world == null || fromPos == null || direction == null)
			return null;
		
		double x = direction.x * maxDistance;
		double y = direction.y * maxDistance;
		double z = direction.z * maxDistance;
		toPos = new Vec3(fromPos.x + x, fromPos.y + y, fromPos.z + z);
		
		
		return raytrace(world, tracingEntity, fromPos, toPos, selector);
	}
	

	
	public static HitResult raytraceApprox(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos,
			Vec3 direction, float maxDistance, Predicate<? super Entity> selector, double nearbyRadius) {
		if (world == null || fromPos == null)
			return null;
		
		HitResult result = raytrace(world, tracingEntity, fromPos, direction, maxDistance, selector);
		if (nearbyRadius > 0) {
			result = nearbyRayTrace(world, result, nearbyRadius, selector);
		}
		return result;
	}

	public static HitResult raytrace(Level world, Entity tracingEntity, Vec3 fromPos, Vec3 toPos,
			Predicate<? super Entity> selector) {
		
        if (world == null) {
        	return miss(fromPos, toPos);
        }
        
        HitResult trace;
        
        // First, raytrace against blocks.
        // First we hit also will help us lower the range of our raytrace
        trace = world.clip(new ClipContext(fromPos, toPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, tracingEntity));
        
        if (trace != null && trace.getType() != HitResult.Type.MISS) {
        	// limit toPos to position of block hit
        	toPos = trace.getLocation();
        }
        
        List<Entity> list = world.getEntities((Entity) null,
        		new AABB(fromPos.x, fromPos.y, fromPos.z, toPos.x, toPos.y, toPos.z),
        		EntitySelector.NO_SPECTATORS.and(new Predicate<Entity>()
        {
            public boolean test(Entity p_apply_1_)
            {
                return !p_apply_1_.noPhysics && (selector == null || selector.test(p_apply_1_));
            }
        }));
        // d2 is current closest distance
        double minDist = fromPos.distanceTo(toPos);
        Entity curEntity = null;
        Vec3 curEntityVec = null;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);
            
            float f1 = entity1.getPickRadius();
            AABB AABB = entity1.getBoundingBox().inflate((double)f1, (double)f1, (double)f1);
            Optional<Vec3> entHit = AABB.clip(fromPos, toPos);

            if (AABB.contains(fromPos))
            {
                if (minDist >= 0.0D)
                {
                    curEntity = entity1;
                    minDist = 0.0D;
                }
            }
            else if (entHit.isPresent())
            {
                double d3 = fromPos.distanceTo(entHit.get());

                if (d3 < minDist || minDist == 0.0D)
                {
                    curEntity = entity1;
                    curEntityVec = entHit.get();
                    minDist = d3;
                }
            }
        }

        // If we hit a block, trace is that MOP
        // If we hit an entity between that block, though, we want that
        if (curEntity != null) {
        	trace = new EntityHitResult(curEntity, curEntityVec);
        }
        
        return trace;

	}
	
	
	public static Collection<HitResult> allInPath(Level world, @Nonnull Entity tracingEntity,  Vec3 fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector) {
		if (world == null || fromPos == null)
			return null;
		
		return allInPath(world, tracingEntity, fromPos, directionFromAngles(pitch, yaw), maxDistance, selector);
	}
	
	public static Collection<HitResult> allInPath(Level world, @Nonnull Entity tracingEntity,  Vec3 fromPos,
			Vec3 direction, float maxDistance, Predicate<? super Entity> selector) {
		Vec3 toPos;
		
		if (world == null || fromPos == null || direction == null)
			return new LinkedList<>();
		
		double x = direction.x * maxDistance;
		double y = direction.y * maxDistance;
		double z = direction.z * maxDistance;
		toPos = new Vec3(fromPos.x + x, fromPos.y + y, fromPos.z + z);
		
		
		return allInPath(world, tracingEntity, fromPos, toPos, selector);
	}
	
	/**
	 * Like a raytrace but returns multiple.
	 * @param world
	 * @param fromPos
	 * @param toPos
	 * @param onlyLiving
	 * @return
	 */
	public static Collection<HitResult> allInPath(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos, Vec3 toPos,
			Predicate<? super Entity> selector) {
		
		List<HitResult> ret = new LinkedList<>();
			
        if (world == null || fromPos == null || toPos == null) {
        	return ret;
        }
        
        HitResult trace;
        
        trace = world.clip(new ClipContext(fromPos, toPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, tracingEntity));
        
        if (trace != null && trace.getType() != HitResult.Type.MISS) {
        	// limit toPos to position of block hit
        	toPos = trace.getLocation();
        	ret.add(cloneTrace(trace));
        }
        
        List<Entity> list = world.getEntities((Entity) null,
        		new AABB(fromPos.x, fromPos.y, fromPos.z, toPos.x, toPos.y, toPos.z),
        		EntitySelector.NO_SPECTATORS.and(new Predicate<Entity>()
        {
            public boolean test(Entity p_apply_1_)
            {
                return p_apply_1_.isPickable() && selector.test(p_apply_1_);
            }
        }));
        
        double maxDist = fromPos.distanceTo(toPos);

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);
            
            float f1 = entity1.getPickRadius();
            AABB AABB = entity1.getBoundingBox().inflate((double)f1, (double)f1, (double)f1);
            Optional<Vec3> entHit = AABB.clip(fromPos, toPos);

            if (AABB.contains(fromPos))
            {
                ret.add(new EntityHitResult(entity1, fromPos));
            }
            else if (entHit.isPresent())
            {
                double d3 = fromPos.distanceTo(entHit.get());

                if (d3 < maxDist)
                {
                    ret.add(new EntityHitResult(entity1, entHit.get()));
                }
            }
        }

        return ret;
	}
	
	private static HitResult cloneTrace(HitResult in) {
		if (in.getType() == HitResult.Type.ENTITY) {
			EntityHitResult orig = (EntityHitResult) in;
			return new EntityHitResult(orig.getEntity(), orig.getLocation());
		}
		
		BlockHitResult blockResult = (BlockHitResult) in;
		
		if (in.getType() == HitResult.Type.MISS)
			return BlockHitResult.miss(in.getLocation(), blockResult.getDirection(), blockResult.getBlockPos());
		
		
		return new BlockHitResult(blockResult.getLocation(), blockResult.getDirection(), blockResult.getBlockPos(), blockResult.isInside());
	}
	
	public static HitResult forwardsRaycast(Entity projectile, boolean includeEntities, boolean ignoreCollideFlag, boolean shouldExclude, Entity maybeExcludedEntity) {
		return forwardsRaycast(projectile, ClipContext.Block.COLLIDER, includeEntities, ignoreCollideFlag, shouldExclude, maybeExcludedEntity);
	}
	
	// Copy of ProjectileUtil method but with ability to collide with other misc entities
	public static HitResult forwardsRaycast(Entity projectile, ClipContext.Block Block, boolean includeEntities, boolean ignoreCollideFlag, boolean shouldExclude, Entity maybeExcludedEntity){
		double d0 = projectile.getX();
		double d1 = projectile.getY();
		double d2 = projectile.getZ();
		double d3 = projectile.getDeltaMovement().x;
		double d4 = projectile.getDeltaMovement().y;
		double d5 = projectile.getDeltaMovement().z;
		Level world = projectile.level;
		Vec3 Vec3 = new Vec3(d0, d1, d2);
		Vec3 Vector3d1 = new Vec3(d0 + d3, d1 + d4, d2 + d5);
		HitResult raytraceresult = world.clip(new ClipContext(Vec3, Vector3d1, Block, ClipContext.Fluid.NONE, projectile));

		if (includeEntities)
		{
			if (raytraceresult.getType() != HitResult.Type.MISS)
			{
				Vector3d1 = raytraceresult.getLocation();
			}

			Entity entity = null;
			Vec3 entityHitVec = null;
			List<Entity> list = world.getEntities(projectile, projectile.getBoundingBox().move(d3, d4, d5).inflate(1.0D));
			double d6 = 0.0D;

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = (Entity)list.get(i);

				if ((ignoreCollideFlag || entity1.isPickable()) && (shouldExclude || !entity1.is(maybeExcludedEntity)) && !entity1.noPhysics)
				{
					AABB AABB = entity1.getBoundingBox().inflate(0.30000001192092896D);
					Optional<Vec3> innerHit = AABB.clip(Vec3, Vector3d1);

					if (innerHit.isPresent())
					{
						double d7 = Vec3.distanceToSqr(innerHit.get());

						if (d7 < d6 || d6 == 0.0D)
						{
							entity = entity1;
							entityHitVec = innerHit.get();
							d6 = d7;
						}
					}
				}
			}

			if (entity != null)
			{
				raytraceresult = new EntityHitResult(entity, entityHitVec);
			}
		}

		return raytraceresult;
	}
	
	public static HitResult nearbyRayTrace(Level world, HitResult result, Predicate<? super Entity> selector) {
		return nearbyRayTrace(world, result, .5, selector);
	}
	
	public static HitResult nearbyRayTrace(Level world, HitResult result, double maxDist, Predicate<? super Entity> selector) {
		if (result == null || (result.getType() == HitResult.Type.ENTITY)) {
			return result;
		}
		
		// Get entities near the result
		Vec3 hitPos = result.getLocation();
		List<Entity> entities = world.getEntities((Entity) null, new AABB(
				hitPos.x, hitPos.y, hitPos.z, hitPos.x, hitPos.y, hitPos.z
				).inflate(maxDist), selector);
		double minDist = 0;
		Entity minEnt = null;
		for (Entity ent : entities) {
			if (selector != null && !selector.test(ent)) {
				continue;
			}
			
			double distSq = hitPos.distanceToSqr(ent.position());
			if (minEnt == null || distSq < minDist) {
				minEnt = ent;
				minDist = distSq;
			}
		}
		
		if (minEnt != null) { 
			result = new EntityHitResult(minEnt);
		}
		
		return result;
	
	}
}
