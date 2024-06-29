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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;

public class TargetManager implements ITargetManager {
	
	private final Map<MobEntity, LivingEntity> targets;
	private final Map<LivingEntity, List<MobEntity>> targeters;
	
	public TargetManager() {
		targets = new HashMap<>();
		targeters = new HashMap<>();
	}
	
	public @Nullable LivingEntity getTarget(MobEntity entity) {
		return targets.get(entity);
	}
	
	protected @Nullable LivingEntity removeMapping(MobEntity mob) {
		@Nullable LivingEntity target = targets.remove(mob); // Easy part
		if (target != null) {
			targeters.computeIfAbsent(target, t -> new ArrayList<>()).remove(mob);
		}
		return target;
	}
	
	protected void addMapping(MobEntity mob, @Nullable LivingEntity target) {
		targets.put(mob, target);
		if (target != null) {
			targeters.computeIfAbsent(target, t -> new ArrayList<>()).add(mob);
		}
	}
	
	public void updateTarget(MobEntity mob, @Nullable LivingEntity target) {
		if (shouldTrack(mob)) {
			final LivingEntity prev = removeMapping(mob);
			addMapping(mob, target);
			if (prev != target && !mob.world.isRemote()) {
				broadcastChange(mob, target);
			}
		}
	}

	@Override
	public List<MobEntity> getEntitiesTargetting(LivingEntity target) {
		return targeters.computeIfAbsent(target, t -> new ArrayList<>());
	}
	
	public void clean(boolean deepClean) {
		List<MobEntity> sources = Lists.newArrayList(targets.keySet());
		for (MobEntity source : sources) {
			if (!source.isAlive()) {
				removeMapping(source);
			}
		}
		
		if (deepClean) {
			Iterator<Entry<LivingEntity, List<MobEntity>>> it = targeters.entrySet().iterator();
			while (it.hasNext()) {
				final Entry<LivingEntity, List<MobEntity>> row = it.next();
				if (!row.getKey().isAlive()) {
					it.remove();
				} else {
					Iterator<MobEntity> listIt = row.getValue().iterator();
					while (listIt.hasNext()) {
						MobEntity targeter = listIt.next();
						if (!targeter.isAlive()) {
							PetCommand.LOGGER.debug("Found stale entry in targetter map for entity: " + row.getKey() + " => " + row.getValue());
							listIt.remove();
						}
					}
				}
			}
		}
	}
	
	protected boolean shouldTrack(MobEntity entity) {
		return PetFuncs.GetOwner(entity) != null;
	}
	
	protected void broadcastChange(MobEntity entity, @Nullable LivingEntity target) {
		NetworkHandler.sendToOwner(new TargetUpdateMessage(entity, target), entity);
	}
}
