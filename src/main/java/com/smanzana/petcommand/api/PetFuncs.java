package com.smanzana.petcommand.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class PetFuncs {
	
	public static List<LivingEntity> GetTamedEntities(LivingEntity owner) {
		return GetTamedEntities(owner, null);
	} 
	
	public static List<LivingEntity> GetTamedEntities(LivingEntity owner, @Nullable Predicate<LivingEntity> filter) {
		List<LivingEntity> ents = new ArrayList<>();
		
		Iterable<Entity> entities;
		
		if (owner.level instanceof ServerWorld) {
			entities = ((ServerWorld) owner.level).getEntities().collect(Collectors.toList());
		} else {
			entities = ((ClientWorld) owner.level).entitiesForRendering();
		}
		
		for (Entity e : entities) {
			if (!(e instanceof LivingEntity)) {
				continue;
			}
			
			LivingEntity ent = (LivingEntity) e;
			if (filter != null && !filter.test(ent)) {
				continue;
			}

			if (ent instanceof ITameableEntity) {
				ITameableEntity tame = (ITameableEntity) ent;
				if (tame.isEntityTamed() && tame.getLivingOwner() != null && tame.getLivingOwner().equals(owner)) {
					ents.add(ent);
				}
			} else if (ent instanceof TameableEntity) {
				TameableEntity tame = (TameableEntity) ent;
				if (tame.isTame() && tame.isOwnedBy(owner)) {
					ents.add(ent);
				}
			}
		}
		return ents;
	}

	public static @Nullable LivingEntity GetOwner(MobEntity entity) {
		LivingEntity ent = (LivingEntity) entity;
		if (ent instanceof ITameableEntity) {
			ITameableEntity tame = (ITameableEntity) ent;
			return tame.getLivingOwner();
		} else if (ent instanceof TameableEntity) {
			TameableEntity tame = (TameableEntity) ent;
			return tame.getOwner();
		}
		return null;
	}
	
	public static @Nullable LivingEntity GetOwner(Entity entity) {
		if (entity instanceof MobEntity) {
			return GetOwner((MobEntity) entity);
		}
		
		return null;
	}

	public static boolean IsSameTeam(LivingEntity ent1, LivingEntity ent2) {
		if (ent1 == ent2) {
			return true;
		}
	
		if (ent1 == null || ent2 == null) {
			return false;
		}
	
		if (ent1.getTeam() != null || ent2.getTeam() != null) { // If teams are at play, just use those.
			return ent1.isAlliedTo(ent2);
		}
		
		LivingEntity ent1Owner = PetFuncs.GetOwner(ent1);
		if (ent1Owner != null) {
			// Are they on the same team as our owner?
			return IsSameTeam((LivingEntity) ent1Owner, ent2);
		}
		
		LivingEntity ent2Owner = PetFuncs.GetOwner(ent2);
		if (ent2Owner != null) {
			// Are we on the same team as their owner?
			return IsSameTeam(ent1, ent2Owner);
		}
	
		// Non-owned entities with no teams involved.
		// Assume mobs are on a different team than anything else
		// return (ent1 instanceof IMob == ent2 instanceof IMob);
	
		// If both are players and teams aren't involved, assume they can work together
		if (ent1 instanceof PlayerEntity && ent2 instanceof PlayerEntity) {
			return true;
		}
	
		// More hostile; assume anything here is not on same team
		return false;
	}
}
