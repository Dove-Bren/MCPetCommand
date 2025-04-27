package com.smanzana.petcommand.pet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.EPetOrderType;
import com.smanzana.petcommand.api.pet.EPetPlacementMode;
import com.smanzana.petcommand.api.pet.EPetTargetMode;
import com.smanzana.petcommand.api.pet.IPetOrderManager;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.network.message.PetCommandSettingsSyncMessage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
public class PetCommandManager extends SavedData implements IPetOrderManager {
	
	private static final class PetCommandSettings {
		
		private static final String NBT_ENTRY_PLACEMENT = "placement";
		private static final String NBT_ENTRY_TARGET = "target";
		private static final String NBT_ENTRY_ORDERS = "orders";
		
		public static PetCommandSettings Empty = new PetCommandSettings();
		
		public EPetPlacementMode placementMode;
		public EPetTargetMode targetMode;
		
		protected final Map<UUID, PetOrder> orders;
		
		public PetCommandSettings() {
			placementMode = EPetPlacementMode.FREE;
			targetMode = EPetTargetMode.FREE;
			orders = new HashMap<>();
		}
		
		public CompoundTag writeToNBT(@Nullable CompoundTag nbt) {
			if (nbt == null) {
				nbt = new CompoundTag();
			}
			
			nbt.putString(NBT_ENTRY_PLACEMENT, placementMode.name());
			nbt.putString(NBT_ENTRY_TARGET, targetMode.name());
			
			if (!orders.isEmpty()) {
				CompoundTag orderTag = new CompoundTag();
				for (Map.Entry<UUID, PetOrder> entry : orders.entrySet()) {
					orderTag.put(entry.getKey().toString(), entry.getValue().toNBT());
				}
				nbt.put(NBT_ENTRY_ORDERS, orderTag);
			}
			
			return nbt;
		}
		
		public static PetCommandSettings FromNBT(CompoundTag nbt) {
			PetCommandSettings settings = new PetCommandSettings();
			
			try {
				settings.placementMode = EPetPlacementMode.valueOf(nbt.getString(NBT_ENTRY_PLACEMENT).toUpperCase());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				settings.targetMode = EPetTargetMode.valueOf(nbt.getString(NBT_ENTRY_TARGET).toUpperCase());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (nbt.contains(NBT_ENTRY_ORDERS)) {
				CompoundTag orderTag = nbt.getCompound(NBT_ENTRY_ORDERS);
				for (String key : orderTag.getAllKeys()) {
					settings.orders.put(UUID.fromString(key), PetOrder.FromNBT(orderTag.getCompound(key)));
				}
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
	
	protected void sendSettingsToClient(ServerPlayer player) {
		NetworkHandler.sendTo(
				new PetCommandSettingsSyncMessage(generateClientSettings(player.getUUID())),
				player);
	}
	
	@SubscribeEvent
	public void onConnect(PlayerLoggedInEvent event) {
		if (event.getPlayer().level.isClientSide) {
			return;
		}
		
		sendSettingsToClient((ServerPlayer) event.getPlayer());
	}
	
	protected @Nonnull PetCommandSettings getSettings(@Nonnull UUID uuid) {
		final PetCommandSettings settings;
		synchronized(playerSettings) {
			settings = playerSettings.get(uuid);
		}
		
		return settings == null ? PetCommandSettings.Empty : settings;
	}
	
	public EPetPlacementMode getPlacementMode(LivingEntity entity) {
		return getPlacementMode(entity.getUUID());
	}
	
	public EPetPlacementMode getPlacementMode(UUID uuid) {
		final PetCommandSettings settings = getSettings(uuid);
		return settings.placementMode;
	}
	
	public EPetTargetMode getTargetMode(LivingEntity entity) {
		return getTargetMode(entity.getUUID());
	}
	
	public EPetTargetMode getTargetMode(UUID uuid) {
		final PetCommandSettings settings = getSettings(uuid);
		return settings.targetMode;
	}
	
	public @Nullable PetOrder getPetOrder(LivingEntity owner, LivingEntity pet) {
		return getPetOrder(owner.getUUID(), pet.getUUID());
	}
	
	public @Nullable PetOrder getPetOrder(UUID ownerID, UUID petID) {
		final PetCommandSettings settings = getSettings(ownerID);
		return settings.orders.getOrDefault(petID, null);
	}
	
	public void setPlacementMode(LivingEntity entity, EPetPlacementMode mode) {
		setPlacementMode(entity.getUUID(), mode);
	}
	
	public void setPlacementMode(UUID uuid, EPetPlacementMode mode) {
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
	
	public void setTargetMode(LivingEntity entity, EPetTargetMode mode) {
		setTargetMode(entity.getUUID(), mode);
	}
	
	public void setTargetMode(UUID uuid, EPetTargetMode mode) {
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
	
	protected void forAllOwned(LivingEntity owner, Consumer<LivingEntity> petAction) {
		for (LivingEntity e : PetFuncs.GetTamedEntities(owner)) {
			if (owner.distanceTo(e) > 100) {
				continue;
			}
			
			petAction.accept(e);
		}
	}
	
	public void commandAllToAttack(LivingEntity owner, LivingEntity target) {
		forAllOwned(owner, (e) -> {
			if (e instanceof IEntityPet) {
				((IEntityPet) e).onAttackCommand(target);
			} else if (e instanceof Mob) {
				((Mob) e).setTarget(target);
			}
		});
	}
	
	public void commandToStop(LivingEntity owner, LivingEntity pet) {
		if (PetFuncs.GetOwner(pet) == null || !PetFuncs.GetOwner(pet).equals(owner)) {
			return;
		}
		
		if (pet instanceof IEntityPet p) {
			p.onStopCommand();
		} else if (pet instanceof Mob mob) {
			mob.setTarget(null);
		}
		this.setPetOrder(owner, pet, null);
	}
	
	public void commandAllStop(LivingEntity owner) {
		forAllOwned(owner, (e) -> {
			commandToStop(owner, e);
		});
	}
	
	public void setPetOrder(LivingEntity owner, LivingEntity pet, @Nullable PetOrder order) {
		if (PetFuncs.GetOwner(pet) == null || !PetFuncs.GetOwner(pet).equals(owner)) {
			return;
		}
		
		final UUID ownerID = owner.getUUID();
		final UUID petID = pet.getUUID();
		
		synchronized(playerSettings) {
			PetCommandSettings settings = playerSettings.computeIfAbsent(ownerID, (i) -> new PetCommandSettings());
			if (order == null) {
				settings.orders.remove(petID);
			} else {
				settings.orders.put(petID, order);
			}
		}
		
		this.setDirty();
		
		if (owner instanceof ServerPlayer player) {
			this.sendSettingsToClient(player);
		}
	}

	@Override
	public EPetOrderType getCurrentOrder(LivingEntity owner, LivingEntity pet) {
		PetOrder order = this.getPetOrder(owner, pet);
		return order == null ? null : order.type();
	}

	@Override
	public boolean clearOrder(LivingEntity owner, LivingEntity pet) {
		this.setPetOrder(owner, pet, null);
		return true;
	}
}
