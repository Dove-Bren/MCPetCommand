package com.smanzana.petcommand;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.smanzana.petcommand.api.ai.IFollowOwnerGoal;
import com.smanzana.petcommand.api.entity.ITameableEntity;
import com.smanzana.petcommand.config.ModConfig;
import com.smanzana.petcommand.entity.BoundIronGolemEntity;
import com.smanzana.petcommand.entity.ai.FollowOwnerAdvancedGoal;
import com.smanzana.petcommand.entity.ai.PetTargetGoal;
import com.smanzana.petcommand.listener.MovementListener;
import com.smanzana.petcommand.listener.TargetListener;
import com.smanzana.petcommand.pet.PetCommandManager;
import com.smanzana.petcommand.pet.TargetManager;
import com.smanzana.petcommand.proxy.ClientProxy;
import com.smanzana.petcommand.proxy.CommonProxy;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

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
	private TargetListener targetListener;

	private final TargetManager serverTargetManager;
	private final TargetManager clientTargetManager;

	public PetCommand() {
		instance = this;
		
		proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		
		(new ModConfig()).register();
		
		serverTargetManager = new TargetManager();
		clientTargetManager = new TargetManager();
		
		MinecraftForge.EVENT_BUS.register(this);
		movementListener = new MovementListener();
		targetListener = new TargetListener();
		MinecraftForge.EVENT_BUS.addListener(BoundIronGolemEntity::EntityInteractListener);
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
	
	public static TargetListener GetTargetListener() {
		return instance.targetListener;
	}
	
	public static TargetManager GetServerTargetManager() {
		return instance.serverTargetManager;
	}
	
	public static TargetManager GetClientTargetManager() {
		return instance.clientTargetManager;
	}
	
	public static @Nullable Entity GetEntityByUUID(Level world, UUID id) {
		// Copied out of NostrumMagica's Entities util
		if (world.isClientSide() && world instanceof ClientLevel) {
			Iterable<Entity> entities = ((ClientLevel)world).entitiesForRendering();
			for (Entity ent : entities) {
				if (ent.getUUID().equals(id)) {
					return ent;
				}
			}
		} else if (world instanceof ServerLevel) {
			return ((ServerLevel) world).getEntity(id);
		}
		
		return null;
	}
	
	private void initPetCommandManager(Level world) {
		petCommandManager = (PetCommandManager) ((ServerLevel) world).getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(PetCommandManager::Load, PetCommandManager::new,
				PetCommandManager.DATA_NAME);

		// TODO I think this is automatic now?
//		if (petCommandManager == null) {
//			petCommandManager = new PetCommandManager();
//			world.getMapStorage().setData(PetCommandManager.DATA_NAME, petCommandManager);
//		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isClientSide()) {
			// force an exception here if this is wrong
			ServerLevel world = (ServerLevel) event.getWorld();
			
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

		if (!(e.getEntity() instanceof Mob)) {
			return;
		}
		
		if (!(e.getEntity() instanceof TamableAnimal) && !(e.getEntity() instanceof ITameableEntity)) {
			return;
		}

		final Mob living = (Mob) e.getEntity();

		// Follow task for pets
		{
			WrappedGoal existingTask = null;
			WrappedGoal followTask = null;
			
			// Get private goal list
			living.goalSelector.getAvailableGoals();
			Set<WrappedGoal> goals = living.goalSelector.getAvailableGoals();//ObfuscationReflectionHelper.getPrivateValue(GoalSelector.class, living.goalSelector, "f_25345_"); 

			// Scan for existing task
			for (WrappedGoal entry : goals) {
				if (entry.getGoal() instanceof FollowOwnerAdvancedGoal) {
					if (existingTask == null) {
						existingTask = entry;
					} else if (existingTask.getPriority() > entry.getPriority()) {
						existingTask = entry; // cause > priority means less priority lol
					}
				} else if (entry.getGoal() instanceof FollowOwnerGoal
						|| entry.getGoal() instanceof IFollowOwnerGoal) {
					if (followTask == null) {
						followTask = entry;
					} else if (followTask.getPriority() > entry.getPriority()) {
						followTask = entry;
					}
				}
			}

			if (existingTask == null) {
				// Gotta inject task. May have to make space for it.
				FollowOwnerAdvancedGoal<Mob> task = new FollowOwnerAdvancedGoal<Mob>(living,
						1.5f, 0f, .5f);
				if (followTask == null) {
					// Can just add at end
					living.goalSelector.addGoal(100, task);
				} else {
					List<WrappedGoal> removes = Lists.newArrayList(goals);
					final int priority = followTask.getPriority();
					removes.removeIf((entry) -> {
						return entry.getPriority() < priority;
					});

					living.goalSelector.addGoal(priority, task);
					for (WrappedGoal entry : removes) {
						living.goalSelector.removeGoal(entry.getGoal());
						living.goalSelector.addGoal(entry.getPriority() + 1, entry.getGoal());
					}
				}
			}
		}

		// Target task for pets
		if (living instanceof PathfinderMob) {
			PathfinderMob creature = (PathfinderMob) living;
			boolean hasTaskAlready = false;
			
			// Get private goal list
			Set<WrappedGoal> targetGoals = living.targetSelector.getAvailableGoals(); 

			// Scan for existing task
			for (WrappedGoal entry : targetGoals) {
				if (entry.getGoal() instanceof PetTargetGoal) {
					hasTaskAlready = true;
					break;
				}
			}

			if (!hasTaskAlready) {
				List<WrappedGoal> removes = Lists.newArrayList(targetGoals);

				living.targetSelector.addGoal(1, new PetTargetGoal<PathfinderMob>(creature));
				for (WrappedGoal entry : removes) {
					living.targetSelector.removeGoal(entry.getGoal());
					living.targetSelector.addGoal(entry.getPriority() + 1, entry.getGoal());
				}
			}
		}
	}

}
