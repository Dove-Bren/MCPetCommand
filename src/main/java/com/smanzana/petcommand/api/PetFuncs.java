package com.smanzana.petcommand.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

public class PetFuncs {
	
	public static List<LivingEntity> GetTamedEntities(LivingEntity owner) {
		return GetTamedEntities(owner, null);
	} 
	
	public static List<LivingEntity> GetTamedEntities(LivingEntity owner, @Nullable Predicate<LivingEntity> filter) {
		List<LivingEntity> ents = new ArrayList<>();
		
		Iterable<Entity> entities;
		
		if (owner.level instanceof ServerLevel) {
			entities = ((ServerLevel) owner.level).getEntities().getAll();
		} else {
			entities = ((ClientLevel) owner.level).entitiesForRendering();
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
			} else if (ent instanceof TamableAnimal) {
				TamableAnimal tame = (TamableAnimal) ent;
				if (tame.isTame() && tame.isOwnedBy(owner)) {
					ents.add(ent);
				}
			}
		}
		return ents;
	}

	public static @Nullable LivingEntity GetOwner(Mob entity) {
		LivingEntity ent = (LivingEntity) entity;
		if (ent instanceof ITameableEntity) {
			ITameableEntity tame = (ITameableEntity) ent;
			return tame.getLivingOwner();
		} else if (ent instanceof TamableAnimal) {
			TamableAnimal tame = (TamableAnimal) ent;
			return tame.getOwner();
		}
		return null;
	}
	
	public static @Nullable LivingEntity GetOwner(Entity entity) {
		if (entity instanceof Mob) {
			return GetOwner((Mob) entity);
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
		if (ent1 instanceof Player && ent2 instanceof Player) {
			return true;
		}
	
		// More hostile; assume anything here is not on same team
		return false;
	}
	
	public static final boolean IsPetSitting(LivingEntity pet) {
		final boolean sitting;
		if (pet == null) {
			sitting = false;
		} else if (pet instanceof ITameableEntity) {
			sitting = ((ITameableEntity) pet).isEntitySitting();
		} else if (pet instanceof TamableAnimal) {
			sitting = ((TamableAnimal) pet).isOrderedToSit();
		} else {
			sitting = false;
		}
		
		return sitting;
	}
	
	/**
	 * Attempts to set the provided entity as 'sitting.'
	 * Returns whether it was successful in figuring out how via generic methods.
	 * @param pet
	 * @return
	 */
	public static final boolean TryToSitPet(LivingEntity pet, boolean sit) {
		if (pet == null) {
			return false;
		}
		
		if (pet instanceof TamableAnimal tame) {
			tame.setOrderedToSit(sit);
			return true;
		}
		if (pet instanceof ITameableEntity tame) {
			return tame.setEntitySitting(sit);
		}
		
		return false;
	}
}
