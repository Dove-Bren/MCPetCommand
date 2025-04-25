package com.smanzana.petcommand.client.pet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nonnull;

import com.smanzana.petcommand.api.client.pet.ISelectionManager;
import com.smanzana.petcommand.client.render.OutlineRenderer;

import net.minecraft.world.entity.LivingEntity;

public class SelectionManager implements ISelectionManager {
	
	protected static final OutlineRenderer.Outline SelectionOutline = new OutlineRenderer.Outline(.4f, .9f, .5f, .9f);
	
	protected final Set<LivingEntity> selected;
	protected final OutlineRenderer outliner;
	
	public SelectionManager(final OutlineRenderer outliner) {
		selected = new HashSet<>();
		this.outliner = outliner;
	}
	
	@Override
	public synchronized boolean isSelected(@Nonnull LivingEntity pet) {
		return this.selected.contains(pet);
	}

	@Override
	public synchronized @Nonnull Set<LivingEntity> getSelectedPets() {
		return Set.copyOf(selected);
	}

	@Override
	public synchronized void setPet(@Nonnull LivingEntity pet, boolean selected) {
		final boolean changed;
		if (selected) {
			changed = this.selected.add(pet);
		} else {
			changed = this.selected.remove(pet);
		}
		
		if (changed) {
			if (selected) {
				applySelectionEffect(pet);
			} else {
				clearSelectionEffect(pet);
			}
		}
	}

	@Override
	public synchronized void clearSelection() {
		this.selected.forEach(this::clearSelectionEffect);
		this.selected.clear();
	}
	
	protected void applySelectionEffect(LivingEntity pet) {
		outliner.add(pet, SelectionOutline);
	}
	
	/**
	 * Remove any effects used to indicate pets are selected.
	 * Note that the pet may be removed from the world already or dying.
	 * @param pet
	 */
	protected void clearSelectionEffect(LivingEntity pet) {
		outliner.remove(pet);
	}
	
	protected synchronized void cleanup() {
		// Look through list of entities and make sure we should still track them
		Iterator<LivingEntity> it = this.selected.iterator();
		while (it.hasNext()) {
			LivingEntity ent = it.next();
			if (ent == null
					|| ent.isRemoved()
					|| ent.isDeadOrDying()) {
				it.remove();
				clearSelectionEffect(ent);
			}
		}
	}
	
	public void tick() {
		cleanup();
	}

}
