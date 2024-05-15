package com.smanzana.petcommand.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MovementListener {

	protected Map<Entity, Vector3d> lastPosCache = new HashMap<>();
	protected Map<Entity, Vector3d> lastMoveCache = new HashMap<>();
	
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
			lastPosCache.put(ent, ent.getPositionVec());
			lastMoveCache.put(ent, ent.getLook(.5f));
		}
	}
	
	/**
	 * Returns the position of an entity at the end of server ticking the last time it happened.
	 * This can serve as 'what position did the entity end up at last tick'.
	 * If entity is not tracked, returns its current position as a best-guess.
	 * @param ent
	 * @return
	 */
	public Vector3d getLastTickPos(Entity ent) {
		addEntity(ent);
		return lastPosCache.get(ent);
	}
	
	public Vector3d getLastMove(Entity ent) {
		addEntity(ent);
		return lastMoveCache.get(ent);
	}
	
	protected void updateTrackedEntities() {
		// Look at entities being tracked. If dead or removed, remove from tracking. Else stash their current positions.
		Iterator<Entry<Entity, Vector3d>> it = lastPosCache.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Entity, Vector3d> entry = it.next();
			if (entry.getKey() == null || !entry.getKey().isAlive()) {
				it.remove();
			} else {
				Vector3d last = entry.getValue();
				Vector3d cur = entry.getKey().getPositionVec();
				entry.setValue(cur);
				if (last.squareDistanceTo(cur) > .025) {
					// Update movement
					lastMoveCache.put(entry.getKey(), cur.subtract(last));
				}
			}
		}
	}
	
}
