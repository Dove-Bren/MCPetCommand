package com.smanzana.petcommand.api.pet;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.petcommand.api.PetCommandAPI;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

public class PetInfo {
	
	public static enum ValueFlavor {
		/**
		 * A bad thing, but expected to slowly grow as times goes on.
		 * For example, 'fatigue'.
		 * "Full" is very bad.
		 */
		GRADUAL_BAD(0xFF4BAD20, 0xFFB51921),
		
		/**
		 * A bad thing. Like permanent-deathliness or something
		 */
		BAD(0xFFB51921, 0xFFB51921),
		
		/**
		 * Like XP. A good thing, but expected to be growing regularly
		 */
		PROGRESS(0xFFFFB2FF, 0xFFFFB2FF),
		
		/**
		 * A good thing. Like health, but not health. Like mana?
		 */
		GOOD(0xFF4BAD20, 0xFF4BAD20),
		
		/**
		 * A good thing that's expected to slowly grow or drop. For example, energy.
		 * "Full" is great.
		 */
		GRADUAL_GOOD(0xFFB51921, 0xFF4BAD20);
		
		protected final float emptyR;
		protected final float emptyG;
		protected final float emptyB;
		protected final float emptyA;
		
		protected final float diffR;
		protected final float diffG;
		protected final float diffB;
		protected final float diffA;
		
		// ARGB
		private ValueFlavor(int empty, int full) {
			emptyR = (float) ((empty >> 16) & 0xFF) / 256f;
			emptyG = (float) ((empty >> 8) & 0xFF) / 256f;
			emptyB = (float) ((empty >> 0) & 0xFF) / 256f;
			emptyA = (float) ((empty >> 24) & 0xFF) / 256f;
			
			float fullR = (float) ((full >> 16) & 0xFF) / 256f;
			float fullG = (float) ((full >> 8) & 0xFF) / 256f;
			float fullB = (float) ((full >> 0) & 0xFF) / 256f;
			float fullA = (float) ((full >> 24) & 0xFF) / 256f;
			
			diffR = fullR - emptyR;
			diffG = fullG - emptyG;
			diffB = fullB - emptyB;
			diffA = fullA - emptyA;
		}
		
		public float colorR(float perc) {
			return emptyR + (perc * diffR);
		}
		
		public float colorG(float perc) {
			return emptyG + (perc * diffG);
		}
		
		public float colorB(float perc) {
			return emptyB + (perc * diffB);
		}
		
		public float colorA(float perc) {
			return emptyA + (perc * diffA);
		}
	}
	
	public static record PetValue(double current, double max, @Nonnull ValueFlavor flavor, @Nullable Component label) { }
	
	public static final PetValue EmptyValue = new PetValue(0, 1, ValueFlavor.PROGRESS, null);
	
	public static enum PetAction {
		STAY,
		ATTACK,
		IDLE,
		WORK,
		WAIT
	}

	// All pets should have these
	private double currentHp;
	private double maxHp;
	private double hpPercent; // out of 1.0

	// Pets can provide a set of extra metrics for display, with info about how to display it
	private final List<PetValue> values;
	
	private PetAction action;
	
	private int refCount;
	
	protected PetInfo() {
		refCount = 0;
		values = new ArrayList<>();
	}
	
	protected void set(PetAction action, double hp, double maxHp, PetValue ... values) {
		this.currentHp = hp;
		this.maxHp = maxHp;
		this.hpPercent = maxHp > 0 ? (hp / maxHp) : 0;
		
		this.values.clear();
		if (values.length == 0) {
			this.values.add(EmptyValue);
		} else {
			for (PetValue value : values) {
				this.values.add(value);
			}
		}
		
		this.action = action == null ? PetAction.WAIT : action;
	}
	
	protected void set(PetAction action, double hp, double maxHp, double secondary, double maxSecondary, ValueFlavor flavor, Component secondaryLabel) {
		set(action, hp, maxHp, new PetValue(secondary, maxSecondary, flavor == null ? ValueFlavor.PROGRESS : flavor, secondaryLabel));
	}
	
	protected void set(double hp, double maxHp, double secondary, double maxSecondary, Component secondaryLabel) {
		set(null, hp, maxHp, secondary, maxSecondary, null, secondaryLabel);
	}
	
	protected void set(double hp, double maxHp, PetAction action) {
		set(action, hp, maxHp, EmptyValue);
	}
	
	protected void set(double hp, double maxHp) {
		set(null, hp, maxHp, EmptyValue);
	}
	
	protected PetInfo addRef() {
		refCount++;
		return this;
	}
	
	protected boolean removeRef() {
		refCount--;
		if (refCount < 0) {
			throw new RuntimeException("Invalid number of removes");
		}
		
		return refCount == 0;
	}
	
	private void validate() {
		// No getters should be called if refcount is 0
		if (refCount <= 0) {
			throw new RuntimeException("PetInfo was released but is still being used");
		}
	}
	
	public void release() {
		release(this);
	}
	
	public double getCurrentHp() {
		validate();
		return currentHp;
	}

	public double getMaxHp() {
		validate();
		return maxHp;
	}

	public double getHpPercent() {
		validate();
		return hpPercent;
	}
	
	public List<PetValue> getPetValues() {
		return this.values;
	}
	
	public PetAction getPetAction() {
		validate();
		return this.action;
	}


	/**************************************************
	 * 
	 *    Pooling methods
	 * 
	 **************************************************/
	
	private static final List<PetInfo> availableInfos = new ArrayList<>();
	private static final int ChunkSize = 32;
	
	public static final PetInfo claim() {
		synchronized(availableInfos) {
			if (availableInfos.isEmpty()) {
				// Imagine if I allocated an array of PetInfos and then indexed into those
				// like a C mem pool that grabbed contiguous chunks lol
				for (int i = 0; i < ChunkSize; i++) {
					availableInfos.add(new PetInfo());
				}
			}
			
			return availableInfos.remove(availableInfos.size() - 1).addRef();
		}
	}
	
	public static final PetInfo claim(PetAction action, double hp, double maxHp, PetValue ...values) {
		PetInfo info = claim();
		info.set(action, hp, maxHp, values);
		return info;
	}
	
	public static final PetInfo claim(PetAction action, double hp, double maxHp, double secondary, double maxSecondary, ValueFlavor flavor, Component secondaryLabel) {
		PetInfo info = claim();
		info.set(action, hp, maxHp, secondary, maxSecondary, flavor, secondaryLabel);
		return info;
	}
	
	public static final PetInfo claim(double hp, double maxHp, double secondary, double maxSecondary, Component secondaryLabel) {
		PetInfo info = claim();
		info.set(hp, maxHp, secondary, maxSecondary, secondaryLabel);
		return info;
	}
	
	public static final PetInfo claim(double hp, double maxHp, PetAction action) {
		PetInfo info = claim();
		info.set(hp, maxHp, action);
		return info;
	}
	
	public static final PetInfo claim(double hp, double maxHp) {
		PetInfo info = claim();
		info.set(hp, maxHp);
		return info;
	}
	
	public static final void release(PetInfo info) {
		// if pooled is false, they shouldn't call this... but oh well.
		if (info.removeRef()) {
			synchronized(availableInfos) {
				availableInfos.add(info);
			}
		}
	}
	
	public static PetInfo Wrap(LivingEntity entity) {
		if (entity instanceof TamableAnimal) {
			final @Nullable LivingEntity target = PetCommandAPI.GetTargetManager(entity).getTarget((TamableAnimal) entity);
			final PetAction action = ((TamableAnimal) entity).isInSittingPose()
					? PetAction.STAY
					: (target != null && target.isAlive()) ? PetAction.ATTACK		
					: PetAction.IDLE;
			return claim(entity.getHealth(), entity.getMaxHealth(), action);
		} else {
			return claim(entity.getHealth(), entity.getMaxHealth());
		}
	}
	
	
	
	/**************************************************
	 * 
	 *    Managed methods
	 * 
	 **************************************************/
	
	public static class ManagedPetInfo extends PetInfo {

		protected ManagedPetInfo() {
			super();
			super.addRef();
		}
		
		@Override
		public void set(PetAction action, double hp, double maxHp, PetValue ...values) {
			super.set(action, hp, maxHp, values);
		}
		
		@Override
		public void set(PetAction action, double hp, double maxHp, double secondary, double maxSecondary, ValueFlavor flavor, Component secondaryLabel) {
			super.set(action, hp, maxHp, secondary, maxSecondary, flavor, secondaryLabel);
		}
		
		@Override
		public void set(double hp, double maxHp, double secondary, double maxSecondary, Component secondaryLabel) {
			super.set(hp, maxHp, secondary, maxSecondary, secondaryLabel);
		}
		
		@Override
		public void set(double hp, double maxHp, PetAction action) {
			super.set(hp, maxHp, action);
		}
		
		@Override
		public void set(double hp, double maxHp) {
			super.set(hp, maxHp, (PetAction)null);
		}
		
		@Override
		protected boolean removeRef() {
			return false;
		}
		
		@Override
		protected PetInfo addRef() {
			return this;
		}
	}
	
	/**
	 * Create a PetInfo that isn't pooled or managed by the pool.
	 * There's no refcounting or pooling mechanics. Just use and let GC when applicable.
	 * If you are calling this very frequently and the lifespan of the infos is short,
	 * consider using the pooling calls (#claim(), #release())
	 * @return
	 */
	public static ManagedPetInfo createManaged() {
		return new ManagedPetInfo();
	}
}
