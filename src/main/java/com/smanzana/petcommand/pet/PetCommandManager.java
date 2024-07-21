package com.smanzana.petcommand.pet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.PetPlacementMode;
import com.smanzana.petcommand.api.pet.PetTargetMode;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.network.message.PetCommandSettingsSyncMessage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Pet command and settings manager.
 * Does two jobs:
 * 1) Stores various settings about how intelligent pets should act through saves
 * 2) Handles requests to direct pet actions
 * @author Skyler
 *
 */
public class PetCommandManager extends SavedData {
	
	private static final class PetCommandSettings {
		
		private static final String NBT_ENTRY_PLACEMENT = "placement";
		private static final String NBT_ENTRY_TARGET = "target";
		
		public static PetCommandSettings Empty = new PetCommandSettings();
		
		public PetPlacementMode placementMode;
		public PetTargetMode targetMode;
		
		public PetCommandSettings() {
			placementMode = PetPlacementMode.FREE;
			targetMode = PetTargetMode.FREE;
		}
		
		public CompoundTag writeToNBT(@Nullable CompoundTag nbt) {
			if (nbt == null) {
				nbt = new CompoundTag();
			}
			
			nbt.putString(NBT_ENTRY_PLACEMENT, placementMode.name());
			nbt.putString(NBT_ENTRY_TARGET, targetMode.name());
			
			return nbt;
		}
		
		public static PetCommandSettings FromNBT(CompoundTag nbt) {
			PetCommandSettings settings = new PetCommandSettings();
			
			try {
				settings.placementMode = PetPlacementMode.valueOf(nbt.getString(NBT_ENTRY_PLACEMENT).toUpperCase());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				settings.targetMode = PetTargetMode.valueOf(nbt.getString(NBT_ENTRY_TARGET).toUpperCase());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return settings;
		}
	}

	public static final String DATA_NAME =  PetCommand.MODID + "_PetCommandData";
	
	private static final String NBT_SETTINGS = "playerSettings";
	
	private Map<UUID, PetCommandSettings> playerSettings;
	
	public PetCommandManager() {
		this.playerSettings = new HashMap<>();
	}
	
	public static PetCommandManager Load(CompoundTag nbt) {
		PetCommandManager manager = new PetCommandManager();
		CompoundTag subtag = nbt.getCompound(NBT_SETTINGS);
		for (String key : subtag.getAllKeys()) {
			UUID uuid = null;
			try {
				uuid = UUID.fromString(key);
			} catch (Exception e) {
				e.printStackTrace();
				uuid = null;
			}
			
			if (uuid == null) {
				continue;
			}
			
			manager.playerSettings.put(uuid, PetCommandSettings.FromNBT(subtag.getCompound(key)));
		}
		return manager;
	}
	
	@Override
	public CompoundTag save(CompoundTag compound) {
		synchronized(playerSettings) {
			CompoundTag subtag = new CompoundTag();
			for (Entry<UUID, PetCommandSettings> entry : playerSettings.entrySet()) {
				subtag.put(entry.getKey().toString(), entry.getValue().writeToNBT(null));
			}
			compound.put(NBT_SETTINGS, subtag);
		}
		
		return compound;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void overrideClientSettings(CompoundTag settingsNBT) {
		PetCommandSettings settings = PetCommandSettings.FromNBT(settingsNBT);
		final UUID ID = PetCommand.GetProxy().getPlayer().getUUID();
		synchronized(playerSettings) {
			playerSettings.put(ID, settings);
		}
	}
	
	protected CompoundTag generateClientSettings(UUID clientID) {
		CompoundTag nbt = new CompoundTag();
		
		PetCommandSettings settings = getSettings(clientID);
		nbt = settings.writeToNBT(nbt);
		
		return nbt;
	}
	
	@SubscribeEvent
	public void onConnect(PlayerLoggedInEvent event) {
		if (event.getPlayer().level.isClientSide) {
			return;
		}
		
		NetworkHandler.sendTo(
				new PetCommandSettingsSyncMessage(generateClientSettings(event.getPlayer().getUUID())),
				(ServerPlayer) event.getPlayer());
	}
	
	protected @Nonnull PetCommandSettings getSettings(@Nonnull UUID uuid) {
		final PetCommandSettings settings;
		synchronized(playerSettings) {
			settings = playerSettings.get(uuid);
		}
		
		return settings == null ? PetCommandSettings.Empty : settings;
	}
	
	public PetPlacementMode getPlacementMode(LivingEntity entity) {
		return getPlacementMode(entity.getUUID());
	}
	
	public PetPlacementMode getPlacementMode(UUID uuid) {
		final PetCommandSettings settings = getSettings(uuid);
		return settings.placementMode;
	}
	
	public PetTargetMode getTargetMode(LivingEntity entity) {
		return getTargetMode(entity.getUUID());
	}
	
	public PetTargetMode getTargetMode(UUID uuid) {
		final PetCommandSettings settings = getSettings(uuid);
		return settings.targetMode;
	}
	
	public void setPlacementMode(LivingEntity entity, PetPlacementMode mode) {
		setPlacementMode(entity.getUUID(), mode);
	}
	
	public void setPlacementMode(UUID uuid, PetPlacementMode mode) {
		synchronized(playerSettings) {
			PetCommandSettings settings = playerSettings.get(uuid);
			if (settings == null) {
				settings = new PetCommandSettings();
				playerSettings.put(uuid, settings);
			}
			
			settings.placementMode = mode;
		}
		
		this.setDirty();
	}
	
	public void setTargetMode(LivingEntity entity, PetTargetMode mode) {
		setTargetMode(entity.getUUID(), mode);
	}
	
	public void setTargetMode(UUID uuid, PetTargetMode mode) {
		synchronized(playerSettings) {
			PetCommandSettings settings = playerSettings.get(uuid);
			if (settings == null) {
				settings = new PetCommandSettings();
				playerSettings.put(uuid, settings);
			}
			
			settings.targetMode = mode;
		}
		
		this.setDirty();
	}
	
	public void commandToAttack(LivingEntity owner, IEntityPet pet, LivingEntity target) {
		if (!owner.equals(pet.getOwner())) {
			return;
		}
		
		pet.onAttackCommand(target);
	}
	
	public void commandToAttack(LivingEntity owner, Mob pet, LivingEntity target) {
		if (pet instanceof IEntityPet) {
			commandToAttack(owner, (IEntityPet) pet, target);
			return;
		}
		
		if (!PetFuncs.GetOwner(pet).equals(owner)) {
			return;
		}
		
		pet.setTarget(target);
	}
	
	protected void forAllOwned(LivingEntity owner, Function<Entity, Integer> petAction) {
		for (LivingEntity e : PetFuncs.GetTamedEntities(owner)) {
			if (owner.distanceTo(e) > 100) {
				continue;
			}
			
			petAction.apply(e);
		}
	}
	
	public void commandAllToAttack(LivingEntity owner, LivingEntity target) {
		forAllOwned(owner, (e) -> {
			if (e instanceof IEntityPet) {
				((IEntityPet) e).onAttackCommand(target);
			} else if (e instanceof Mob) {
				((Mob) e).setTarget(target);
			}
			return 0;
		});
	}
	
	public void commandAllStopAttacking(LivingEntity owner) {
		forAllOwned(owner, (e) -> {
			if (e instanceof IEntityPet) {
				((IEntityPet) e).onStopCommand();
			} else if (e instanceof Mob) {
				((Mob) e).setTarget(null);
			}
			return 0;
		});
	}
}
