package com.smanzana.petcommand;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.smanzana.petcommand.config.ModConfig;
import com.smanzana.petcommand.entity.ITameableEntity;
import com.smanzana.petcommand.entity.task.FollowOwnerAdvancedGoal;
import com.smanzana.petcommand.entity.task.FollowOwnerGenericGoal;
import com.smanzana.petcommand.entity.task.PetTargetGoal;
import com.smanzana.petcommand.listener.MovementListener;
import com.smanzana.petcommand.pet.PetCommandManager;
import com.smanzana.petcommand.proxy.ClientProxy;
import com.smanzana.petcommand.proxy.CommonProxy;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PetCommand.MODID)
public class PetCommand
{

	public static final String MODID = "petcommand";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	private static PetCommand instance;
	
	private CommonProxy proxy;
	private PetCommandManager petCommandManager;
	private MovementListener movementListener;

	public PetCommand() {
		instance = this;
		
		proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		
		(new ModConfig()).register();
		
		MinecraftForge.EVENT_BUS.register(this);
		movementListener = new MovementListener();
	}
	
	public static CommonProxy GetProxy() {
		return instance.proxy;
	}
	
	public static PetCommandManager GetPetCommandManager() {
		if (instance.petCommandManager == null) {
			if (instance.proxy.isServer()) {
				throw new RuntimeException("Accessing PetCommandManager before a world has been loaded!");
			} else {
				instance.petCommandManager = new PetCommandManager();
			}
		}
		return instance.petCommandManager;
	}
	
	public static MovementListener GetMovementListener() {
		return instance.movementListener;
	}
	
	public static @Nullable Entity GetEntityByUUID(World world, UUID id) {
		// Copied out of NostrumMagica's Entities util
		if (world.isRemote() && world instanceof ClientWorld) {
			Iterable<Entity> entities = ((ClientWorld)world).getAllEntities();
			for (Entity ent : entities) {
				if (ent.getUniqueID().equals(id)) {
					return ent;
				}
			}
		} else if (world instanceof ServerWorld) {
			return ((ServerWorld) world).getEntityByUuid(id);
		}
		
		return null;
	}
	
	public static List<LivingEntity> GetTamedEntities(LivingEntity owner) {
		return GetTamedEntities(owner, null);
	} 
	
	public static List<LivingEntity> GetTamedEntities(LivingEntity owner, @Nullable Predicate<LivingEntity> filter) {
		List<LivingEntity> ents = new ArrayList<>();
		
		Iterable<Entity> entities;
		
		if (owner.world instanceof ServerWorld) {
			entities = ((ServerWorld) owner.world).getEntities().collect(Collectors.toList());
		} else {
			entities = ((ClientWorld) owner.world).getAllEntities();
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
			} else if (ent instanceof TameableEntity) {
				TameableEntity tame = (TameableEntity) ent;
				if (tame.isTamed() && tame.isOwner(owner)) {
					ents.add(ent);
				}
			}
		}
		return ents;
	}

	public static @Nullable LivingEntity GetOwner(MobEntity entity) {
		LivingEntity ent = (LivingEntity) entity;
		if (ent instanceof ITameableEntity) {
			ITameableEntity tame = (ITameableEntity) ent;
			return tame.getLivingOwner();
		} else if (ent instanceof TameableEntity) {
			TameableEntity tame = (TameableEntity) ent;
			return tame.getOwner();
		}
		return null;
	}
	
	public static @Nullable LivingEntity GetOwner(Entity entity) {
		if (entity instanceof MobEntity) {
			return GetOwner((MobEntity) entity);
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
			return ent1.isOnSameTeam(ent2);
		}
		
		LivingEntity ent1Owner = GetOwner(ent1);
		if (ent1Owner != null) {
			// Are they on the same team as our owner?
			return IsSameTeam((LivingEntity) ent1Owner, ent2);
		}
		
		LivingEntity ent2Owner = GetOwner(ent2);
		if (ent2Owner != null) {
			// Are we on the same team as their owner?
			return IsSameTeam(ent1, ent2Owner);
		}

		// Non-owned entities with no teams involved.
		// Assume mobs are on a different team than anything else
		// return (ent1 instanceof IMob == ent2 instanceof IMob);

		// If both are players and teams aren't involved, assume they can work together
		if (ent1 instanceof PlayerEntity && ent2 instanceof PlayerEntity) {
			return true;
		}

		// More hostile; assume anything here is not on same team
		return false;
	}
	
	private void initPetCommandManager(World world) {
		petCommandManager = (PetCommandManager) ((ServerWorld) world).getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(PetCommandManager::new,
				PetCommandManager.DATA_NAME);

		// TODO I think this is automatic now?
//		if (petCommandManager == null) {
//			petCommandManager = new PetCommandManager();
//			world.getMapStorage().setData(PetCommandManager.DATA_NAME, petCommandManager);
//		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote()) {
			// force an exception here if this is wrong
			ServerWorld world = (ServerWorld) event.getWorld();
			
			// Do the correct initialization for persisted data
			//initPetSoulRegistry(world);
			initPetCommandManager(world);
		}
	}
	
	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent e) {
		if (e.isCanceled()) {
			return;
		}

		if (!(e.getEntity() instanceof MobEntity)) {
			return;
		}
		
		if (!(e.getEntity() instanceof TameableEntity) && !(e.getEntity() instanceof ITameableEntity)) {
			return;
		}

		final MobEntity living = (MobEntity) e.getEntity();

		// Follow task for pets
		{
			PrioritizedGoal existingTask = null;
			PrioritizedGoal followTask = null;
			
			// Get private goal list
			LinkedHashSet<PrioritizedGoal> goals = ObfuscationReflectionHelper.getPrivateValue(GoalSelector.class, living.goalSelector, "field_220892_d"); 

			// Scan for existing task
			for (PrioritizedGoal entry : goals) {
				if (entry.getGoal() instanceof FollowOwnerAdvancedGoal) {
					if (existingTask == null) {
						existingTask = entry;
					} else if (existingTask.getPriority() > entry.getPriority()) {
						existingTask = entry; // cause > priority means less priority lol
					}
				} else if (entry.getGoal() instanceof FollowOwnerGoal
						|| entry.getGoal() instanceof FollowOwnerGenericGoal) {
					if (followTask == null) {
						followTask = entry;
					} else if (followTask.getPriority() > entry.getPriority()) {
						followTask = entry;
					}
				}
			}

			if (existingTask == null) {
				// Gotta inject task. May have to make space for it.
				FollowOwnerAdvancedGoal<MobEntity> task = new FollowOwnerAdvancedGoal<MobEntity>(living,
						1.5f, 0f, .5f);
				if (followTask == null) {
					// Can just add at end
					living.goalSelector.addGoal(100, task);
				} else {
					List<PrioritizedGoal> removes = Lists.newArrayList(goals);
					final int priority = followTask.getPriority();
					removes.removeIf((entry) -> {
						return entry.getPriority() < priority;
					});

					living.goalSelector.addGoal(priority, task);
					for (PrioritizedGoal entry : removes) {
						living.goalSelector.removeGoal(entry.getGoal());
						living.goalSelector.addGoal(entry.getPriority() + 1, entry.getGoal());
					}
				}
			}
		}

		// Target task for pets
		if (living instanceof CreatureEntity) {
			CreatureEntity creature = (CreatureEntity) living;
			boolean hasTaskAlready = false;
			
			// Get private goal list
			LinkedHashSet<PrioritizedGoal> targetGoals = ObfuscationReflectionHelper.getPrivateValue(GoalSelector.class, living.targetSelector, "field_220892_d"); 

			// Scan for existing task
			for (PrioritizedGoal entry : targetGoals) {
				if (entry.getGoal() instanceof PetTargetGoal) {
					hasTaskAlready = true;
					break;
				}
			}

			if (!hasTaskAlready) {
				List<PrioritizedGoal> removes = Lists.newArrayList(targetGoals);

				living.targetSelector.addGoal(1, new PetTargetGoal<CreatureEntity>(creature));
				for (PrioritizedGoal entry : removes) {
					living.targetSelector.removeGoal(entry.getGoal());
					living.targetSelector.addGoal(entry.getPriority() + 1, entry.getGoal());
				}
			}
		}
	}

}
