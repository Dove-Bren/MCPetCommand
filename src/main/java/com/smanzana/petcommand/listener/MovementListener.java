package com.smanzana.petcommand.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MovementListener {

	protected Map<Entity, Vec3> lastPosCache = new HashMap<>();
	protected Map<Entity, Vec3> lastMoveCache = new HashMap<>();
	
	public MovementListener() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onTick(ServerTickEvent event) {
		if (event.phase == Phase.END) {
			updateTrackedEntities();
		}
	}
	
	protected void addEntity(Entity ent) {
		if (!lastPosCache.containsKey(ent)) {
			lastPosCache.put(ent, ent.position());
			lastMoveCache.put(ent, ent.getViewVector(.5f));
		}
	}
	
	/**
	 * Returns the position of an entity at the end of server ticking the last time it happened.
	 * This can serve as 'what position did the entity end up at last tick'.
	 * If entity is not tracked, returns its current position as a best-guess.
	 * @param ent
	 * @return
	 */
	public Vec3 getLastTickPos(Entity ent) {
		addEntity(ent);
		return lastPosCache.get(ent);
	}
	
	public Vec3 getLastMove(Entity ent) {
		addEntity(ent);
		return lastMoveCache.get(ent);
	}
	
	protected void updateTrackedEntities() {
		// Look at entities being tracked. If dead or removed, remove from tracking. Else stash their current positions.
		Iterator<Entry<Entity, Vec3>> it = lastPosCache.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Entity, Vec3> entry = it.next();
			if (entry.getKey() == null || !entry.getKey().isAlive()) {
				it.remove();
			} else {
				Vec3 last = entry.getValue();
				Vec3 cur = entry.getKey().position();
				entry.setValue(cur);
				if (last.distanceToSqr(cur) > .025) {
					// Update movement
					lastMoveCache.put(entry.getKey(), cur.subtract(last));
				}
			}
		}
	}
	
}
