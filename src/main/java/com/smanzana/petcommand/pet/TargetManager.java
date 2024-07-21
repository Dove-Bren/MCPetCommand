package com.smanzana.petcommand.pet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.pet.ITargetManager;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.network.message.TargetUpdateMessage;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class TargetManager implements ITargetManager {
	
	private final Map<Mob, LivingEntity> targets;
	private final Map<LivingEntity, List<Mob>> targeters;
	
	public TargetManager() {
		targets = new HashMap<>();
		targeters = new HashMap<>();
	}
	
	public @Nullable LivingEntity getTarget(Mob entity) {
		return targets.get(entity);
	}
	
	protected @Nullable LivingEntity removeMapping(Mob mob) {
		@Nullable LivingEntity target = targets.remove(mob); // Easy part
		if (target != null) {
			targeters.computeIfAbsent(target, t -> new ArrayList<>()).remove(mob);
		}
		return target;
	}
	
	protected void addMapping(Mob mob, @Nullable LivingEntity target) {
		targets.put(mob, target);
		if (target != null) {
			targeters.computeIfAbsent(target, t -> new ArrayList<>()).add(mob);
		}
	}
	
	public void updateTarget(Mob mob, @Nullable LivingEntity target) {
		if (shouldTrack(mob)) {
			final LivingEntity prev = removeMapping(mob);
			addMapping(mob, target);
			if (prev != target && !mob.level.isClientSide()) {
				broadcastChange(mob, target);
			}
		}
	}

	@Override
	public List<Mob> getEntitiesTargetting(LivingEntity target) {
		return targeters.computeIfAbsent(target, t -> new ArrayList<>());
	}
	
	public void clean(boolean deepClean) {
		List<Mob> sources = Lists.newArrayList(targets.keySet());
		for (Mob source : sources) {
			if (!source.isAlive()) {
				removeMapping(source);
			}
		}
		
		if (deepClean) {
			Iterator<Entry<LivingEntity, List<Mob>>> it = targeters.entrySet().iterator();
			while (it.hasNext()) {
				final Entry<LivingEntity, List<Mob>> row = it.next();
				if (!row.getKey().isAlive()) {
					it.remove();
				} else {
					Iterator<Mob> listIt = row.getValue().iterator();
					while (listIt.hasNext()) {
						Mob targeter = listIt.next();
						if (!targeter.isAlive()) {
							PetCommand.LOGGER.debug("Found stale entry in targetter map for entity: " + row.getKey() + " => " + row.getValue());
							listIt.remove();
						}
					}
				}
			}
		}
	}
	
	protected boolean shouldTrack(Mob entity) {
		return PetFuncs.GetOwner(entity) != null;
	}
	
	protected void broadcastChange(Mob entity, @Nullable LivingEntity target) {
		NetworkHandler.sendToOwner(new TargetUpdateMessage(entity, target), entity);
	}
}
