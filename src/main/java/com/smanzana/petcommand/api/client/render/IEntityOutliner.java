package com.smanzana.petcommand.api.client.render;

import net.minecraft.world.entity.Entity;

/**
 * Provides entity outlining like the glowing effect, but more generally applicable and doesn't need server effects to do.
 * Useful for highlighting entities. PetCommand itself uses this to highlight tamed pets that are currently selected.
 */
public interface IEntityOutliner {

	public static final record Outline(float red, float green, float blue, float alpha) {
		
		public static Outline White() {
			return new Outline(1f, 1f, 1f, 1f);
		}
	}
	
	public void add(Entity ent, Outline outline);
	
	public void remove(Entity ent);
	
}
