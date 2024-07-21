package com.smanzana.petcommand.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.ai.IFollowOwnerGoal;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIStatAdapter;
import com.smanzana.petcommand.api.client.petgui.sheet.PetInventorySheet;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.PetAction;
import com.smanzana.petcommand.api.pet.PetInfo.SecondaryFlavor;
import com.smanzana.petcommand.util.ArrayUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.ResetAngerGoal;
import net.minecraft.entity.ai.goal.ShowVillagerFlowerGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class BoundIronGolemEntity extends IronGolemEntity implements IEntityPet {
	
	public static final String ID = "bound_iron_golem";
	
	private static final String NBT_OWNER_ID = "bound_owner_id";
	private static final String NBT_INVENTORY = "inventory";
	protected static final DataParameter<Boolean> ATTACKING = EntityDataManager.defineId(BoundIronGolemEntity.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.defineId(BoundIronGolemEntity.class, DataSerializers.OPTIONAL_UUID);
	private static final int INVENTORY_SIZE = 9;
	
	protected IInventory inventory;

	public BoundIronGolemEntity(EntityType<? extends IronGolemEntity> type, World worldIn) {
		super(type, worldIn);
		this.setPlayerCreated(false);
		this.inventory = new Inventory(INVENTORY_SIZE);
	}
	
	@Override
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(priority++, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
		this.goalSelector.addGoal(priority++, new DummyFollowOwnerGoal()); // Adding dummy to influence where advanced follow is inserted by advanced follow
		this.goalSelector.addGoal(priority, new WaterAvoidingRandomWalkingGoal(this, 1.0D)); // Note not increasing priority like in base to make it a choice
		this.goalSelector.addGoal(priority++, new ShowVillagerFlowerGoal(this));
		this.goalSelector.addGoal(priority, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(priority++, new LookRandomlyGoal(this));
		

		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::canAttackEntity));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<>(this, MobEntity.class, 5, false, false, (p_234199_0_) -> {
			return p_234199_0_ instanceof IMob && !(p_234199_0_ instanceof CreeperEntity);
		}));
		this.targetSelector.addGoal(priority++, new ResetAngerGoal<>(this, false));
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		
		entityData.define(ATTACKING, false);
		entityData.define(OWNER_UNIQUE_ID, Optional.empty());
	}
	
	@Override
	public void addAdditionalSaveData(CompoundNBT compound) {
		super.addAdditionalSaveData(compound);
		
		if (this.getOwnerID() != null) {
			compound.putUUID(NBT_OWNER_ID, this.getOwnerID());
		}
		
		// Write inventory
		{
			ListNBT invTag = new ListNBT();
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				CompoundNBT tag = new CompoundNBT();
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
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		
		this.setOwnerID(compound.hasUUID(NBT_OWNER_ID)
				? compound.getUUID(NBT_OWNER_ID)
				: null);
				
		// Read inventory
		{
			ListNBT list = compound.getList(NBT_INVENTORY, NBT.TAG_COMPOUND);
			this.inventory = new Inventory(INVENTORY_SIZE);
			
			for (int i = 0; i < INVENTORY_SIZE; i++) {
				CompoundNBT tag = list.getCompound(i);
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
	public ActionResultType /*processInteract*/ mobInteract(PlayerEntity player, Hand hand) {
		ActionResultType parent = super.mobInteract(player, hand);
		if (parent != ActionResultType.PASS) {
			return parent;
		}
		if (!level.isClientSide()) {
			PetCommandAPI.OpenPetGUI(player, this);
		}
		return ActionResultType.SUCCESS;
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
	
	protected PetAction getCurrentAction() {
		if (this.isAttacking()) {
			return PetAction.ATTACKING;
		} else if (this.getOfferFlowerTick() > 0) {
			return PetAction.WORKING;
		} else {
			return PetAction.IDLING;
		}
	}

	@Override
	public PetInfo getPetSummary() {
		final int maxAttack = 10; // Copied from IronGolemEntity. This is max cooldown
		return PetInfo.claim(this.getHealth(), this.getMaxHealth(), Math.max(0, maxAttack - this.getAttackAnimationTick()), maxAttack, SecondaryFlavor.GOOD, getCurrentAction());
	}

	@Override
	public UUID getPetID() {
		return this.getUUID();
	}

	@Override
	public boolean isBigPet() {
		return false;
	}

	@Override
	public IPetGUISheet<BoundIronGolemEntity>[] getContainerSheets(PlayerEntity player) {
		return ArrayUtil.MakeArray(
			new PetInventorySheet<BoundIronGolemEntity>(this, this.inventory) {
				@Override
				public boolean shouldShow(BoundIronGolemEntity golem, IPetContainer<BoundIronGolemEntity> container) {
					return true;
				}
			}
		);
	}

	@Override
	public PetGUIStatAdapter<? extends IEntityPet> getGUIAdapter() {
		return GolemGUIStatAdapter.INSTANCE;
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
		this.remove();
	}

	protected static final class DummyFollowOwnerGoal extends Goal implements IFollowOwnerGoal {

		@Override
		public boolean canUse() {
			return false;
		}
		
	}
	
	protected static final class GolemGUIStatAdapter implements PetGUIStatAdapter<BoundIronGolemEntity> {
		public static final GolemGUIStatAdapter INSTANCE = new GolemGUIStatAdapter();
		
		protected GolemGUIStatAdapter() {
			
		}
		
		@Override
		public boolean supportsSecondaryAmt(BoundIronGolemEntity pet) {
			return false;
		}
		
		@Override
		public boolean supportsTertiaryAmt(BoundIronGolemEntity pet) {
			return false;
		}
		
		@Override
		public boolean supportsQuaternaryAmt(BoundIronGolemEntity pet) {
			return false;
		}

		@Override
		public float getSecondaryAmt(BoundIronGolemEntity pet) {
			return 0;
		}

		@Override
		public float getMaxSecondaryAmt(BoundIronGolemEntity pet) {
			return 0;
		}

		@Override
		public String getSecondaryLabel(BoundIronGolemEntity pet) {
			return null;
		}

		@Override
		public float getTertiaryAmt(BoundIronGolemEntity pet) {
			return 0;
		}

		@Override
		public float getMaxTertiaryAmt(BoundIronGolemEntity pet) {
			return 0;
		}

		@Override
		public String getTertiaryLabel(BoundIronGolemEntity pet) {
			return null;
		}

		@Override
		public float getQuaternaryAmt(BoundIronGolemEntity pet) {
			return 0;
		}

		@Override
		public float getMaxQuaternaryAmt(BoundIronGolemEntity pet) {
			return 0;
		}

		@Override
		public String getQuaternaryLabel(BoundIronGolemEntity pet) {
			return null;
		}
	}
	
	public static void EntityInteractListener(PlayerInteractEvent.EntityInteract event) {
		if (!event.isCanceled() && !event.getEntity().level.isClientSide()) {
			if (event.getTarget() instanceof IronGolemEntity
					&& !(event.getTarget() instanceof BoundIronGolemEntity)) {
				ItemStack stack = event.getItemStack();
				if (!stack.isEmpty() && stack.getItem() == Items.POPPY) {
					event.getWorld().playSound(null, event.getPos(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundCategory.NEUTRAL, 1f, 1f);
					TransformToBound((IronGolemEntity) event.getTarget(), event.getPlayer());
					stack.shrink(1);
					event.setCancellationResult(ActionResultType.SUCCESS);
				}
			}
		}
	}
	
	public static final void TransformToBound(IronGolemEntity entity, @Nullable LivingEntity owner) {
		BoundIronGolemEntity ent = PetCommandEntities.BOUND_IRON_GOLEM.create(entity.level);
		ent.copyPosition(entity);
		if (entity.hasCustomName()) {
			ent.setCustomName(entity.getCustomName());
			ent.setCustomNameVisible(entity.isCustomNameVisible());
		}
		ent.setOwner(owner);
		entity.level.addFreshEntity(ent);
		entity.remove();
	}
}
