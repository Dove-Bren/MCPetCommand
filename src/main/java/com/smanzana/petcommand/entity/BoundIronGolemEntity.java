package com.smanzana.petcommand.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.ai.IFollowOwnerGoal;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.sheet.PetInventorySheet;
import com.smanzana.petcommand.api.client.petgui.sheet.PetStatSheet;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.EPetAction;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.ValueFlavor;
import com.smanzana.petcommand.util.ArrayUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class BoundIronGolemEntity extends IronGolem implements IEntityPet {
	
	public static final String ID = "bound_iron_golem";
	
	private static final Component SwingLabel = new TranslatableComponent("value." + ID + ".cooldown");
	
	private static final String NBT_OWNER_ID = "bound_owner_id";
	private static final String NBT_INVENTORY = "inventory";
	protected static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(BoundIronGolemEntity.class, EntityDataSerializers.BOOLEAN);
	protected static final EntityDataAccessor<Optional<UUID>> OWNER_UNIQUE_ID = SynchedEntityData.defineId(BoundIronGolemEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final int INVENTORY_SIZE = 9;
	
	protected Container inventory;

	public BoundIronGolemEntity(EntityType<? extends BoundIronGolemEntity> type, Level worldIn) {
		super(type, worldIn);
		this.setPlayerCreated(false);
		this.inventory = new SimpleContainer(INVENTORY_SIZE);
	}
	
	@Override
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(priority++, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
		this.goalSelector.addGoal(priority++, new DummyFollowOwnerGoal()); // Adding dummy to influence where advanced follow is inserted by advanced follow
		this.goalSelector.addGoal(priority, new WaterAvoidingRandomStrollGoal(this, 1.0D)); // Note not increasing priority like in base to make it a choice
		this.goalSelector.addGoal(priority++, new OfferFlowerGoal(this));
		this.goalSelector.addGoal(priority, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(priority++, new RandomLookAroundGoal(this));
		

		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::canAttackEntity));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false, (p_234199_0_) -> {
			return p_234199_0_ instanceof Enemy && !(p_234199_0_ instanceof Creeper);
		}));
		this.targetSelector.addGoal(priority++, new ResetUniversalAngerTargetGoal<>(this, false));
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		
		entityData.define(ATTACKING, false);
		entityData.define(OWNER_UNIQUE_ID, Optional.empty());
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		if (this.getOwnerID() != null) {
			compound.putUUID(NBT_OWNER_ID, this.getOwnerID());
		}
		
		// Write inventory
		{
			ListTag invTag = new ListTag();
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				CompoundTag tag = new CompoundTag();
				ItemStack stack = inventory.getItem(i);
				if (!stack.isEmpty()) {
					stack.save(tag);
				}
				
				invTag.add(tag);
			}
			
			compound.put(NBT_INVENTORY, invTag);
		}
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		this.setOwnerID(compound.hasUUID(NBT_OWNER_ID)
				? compound.getUUID(NBT_OWNER_ID)
				: null);
				
		// Read inventory
		{
			ListTag list = compound.getList(NBT_INVENTORY, Tag.TAG_COMPOUND);
			this.inventory = new SimpleContainer(INVENTORY_SIZE);
			
			for (int i = 0; i < INVENTORY_SIZE; i++) {
				CompoundTag tag = list.getCompound(i);
				ItemStack stack = ItemStack.EMPTY;
				if (tag != null) {
					stack = ItemStack.of(tag);
				}
				this.inventory.setItem(i, stack);
			}
		}
	}
	
	@Override
	public void aiStep() {
		super.aiStep();
		
		if (!level.isClientSide) {
			if (this.getOwnerID() == null) {
				// Revert to normal iron golem
				transformBack();
			}
			
			this.setAttacking(this.getTarget() != null
					|| this.getPersistentAngerTarget() != null);
		}
	}
	
	@Override
	protected void dropEquipment() {
		if (!this.level.isClientSide) {
			if (this.inventory != null) {
				for (int i = 0; i < inventory.getContainerSize(); i++) {
					ItemStack stack = inventory.getItem(i);
					if (!stack.isEmpty()) {
						ItemEntity item = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), stack);
						this.level.addFreshEntity(item);
					}
				}
			}
			
			inventory.clearContent();
		}
	}
	
	protected @Nullable UUID getOwnerID() {
		return this.entityData.get(OWNER_UNIQUE_ID).orElse(null);
	}
	
	protected void setOwnerID(UUID ownerID) {
		this.entityData.set(OWNER_UNIQUE_ID, Optional.ofNullable(ownerID));
	}
	
	protected void setOwner(@Nullable Entity owner) {
		final UUID id;
		if (owner == null) {
			id = null;
		} else {
			id = owner.getUUID();
		}
		this.setOwnerID(id);
	}
	
	protected void setAttacking(boolean isAttacking) {
		this.entityData.set(ATTACKING, isAttacking);
	}
	
	public boolean isAttacking() {
		return this.entityData.get(ATTACKING);
	}
	
	protected boolean canAttackEntity(LivingEntity entity) {
		return !isOwner(entity) && isAngryAt(entity);
	}
	
	protected boolean isOwner(LivingEntity entity) {
		return this.isEntityTamed() && this.getOwner() != entity;
	}
	
	@Override
	public InteractionResult /*processInteract*/ mobInteract(Player player, InteractionHand hand) {
		InteractionResult parent = super.mobInteract(player, hand);
		if (parent != InteractionResult.PASS) {
			return parent;
		}
		if (!level.isClientSide()) {
			PetCommandAPI.OpenPetGUI(player, this);
		}
		return InteractionResult.SUCCESS;
	} 

	@Override
	public Entity getOwner() {
		final UUID ownerID = this.getOwnerID();
		if (ownerID == null) {
			return null;
		}
		
		return PetCommand.GetEntityByUUID(level, ownerID);
	}

	@Override
	public boolean isEntityTamed() {
		return this.getOwnerID() != null;
	}

	@Override
	public boolean isEntitySitting() {
		return false;
	}
	
	protected EPetAction getCurrentAction() {
		final @Nullable EPetAction order = PetInfo.GetOrderAction(this);
		if (order != null) {
			return order;
		} else if (this.isAttacking()) {
			return EPetAction.ATTACK;
		} else if (this.getOfferFlowerTick() > 0) {
			return EPetAction.WORK;
		} else {
			return EPetAction.IDLE;
		}
	}

	@Override
	public PetInfo getPetSummary() {
		final int maxAttack = 10; // Copied from IronGolemEntity. This is max cooldown
		return PetInfo.claim(getCurrentAction(), this.getHealth(), this.getMaxHealth(), Math.max(0, maxAttack - this.getAttackAnimationTick()), maxAttack, ValueFlavor.GOOD, SwingLabel);
		
		// Testing code
//		int unused;
//		return PetInfo.claim(getCurrentAction(), this.getHealth(), this.getMaxHealth(),
//				new PetInfo.PetValue(Math.max(0, maxAttack - this.getAttackAnimationTick()), maxAttack, ValueFlavor.GOOD, SwingLabel),
//				new PetInfo.PetValue(2, 5, ValueFlavor.BAD, new TextComponent("Test Value")),
//				new PetInfo.PetValue(.3f, 1f, ValueFlavor.BAD, new TextComponent("Test Percent"))
//				);
	}

	@Override
	public UUID getPetID() {
		return this.getUUID();
	}

	@Override
	public boolean isBigPet() {
		return true;
	}

	@Override
	public IPetGUISheet<BoundIronGolemEntity>[] getContainerSheets(Player player) {
		return ArrayUtil.MakeArray(
			new PetStatSheet<>(this),
			new PetInventorySheet<BoundIronGolemEntity>(this, this.inventory) {
				@Override
				public boolean shouldShow(BoundIronGolemEntity golem, IPetContainer<BoundIronGolemEntity> container) {
					return true;
				}
			}
		);
	}

	protected void transformBack() {
		// Transform into the given entity
		Entity ent = EntityType.IRON_GOLEM.create(level);
		ent.copyPosition(this);
		if (this.hasCustomName()) {
			ent.setCustomName(this.getCustomName());
			ent.setCustomNameVisible(this.isCustomNameVisible());
		}
		level.addFreshEntity(ent);
		this.remove(RemovalReason.DISCARDED);
	}

	protected static final class DummyFollowOwnerGoal extends Goal implements IFollowOwnerGoal {

		@Override
		public boolean canUse() {
			return false;
		}
		
	}
	
	public static void EntityInteractListener(PlayerInteractEvent.EntityInteract event) {
		if (!event.isCanceled() && !event.getEntity().level.isClientSide()) {
			if (event.getTarget() instanceof IronGolem
					&& !(event.getTarget() instanceof BoundIronGolemEntity)) {
				ItemStack stack = event.getItemStack();
				if (!stack.isEmpty() && stack.getItem() == Items.POPPY) {
					event.getWorld().playSound(null, event.getPos(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 1f, 1f);
					TransformToBound((IronGolem) event.getTarget(), event.getPlayer());
					stack.shrink(1);
					event.setCancellationResult(InteractionResult.SUCCESS);
				}
			}
		}
	}
	
	public static final void TransformToBound(IronGolem entity, @Nullable LivingEntity owner) {
		BoundIronGolemEntity ent = PetCommandEntities.BOUND_IRON_GOLEM.create(entity.level);
		ent.copyPosition(entity);
		if (entity.hasCustomName()) {
			ent.setCustomName(entity.getCustomName());
			ent.setCustomNameVisible(entity.isCustomNameVisible());
		}
		ent.setOwner(owner);
		entity.level.addFreshEntity(ent);
		entity.remove(RemovalReason.DISCARDED);
	}

	@Override
	public boolean setEntitySitting(boolean sitting) {
		return false;
	}
}
