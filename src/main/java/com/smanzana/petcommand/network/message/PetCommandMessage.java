package com.smanzana.petcommand.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.pet.PetPlacementMode;
import com.smanzana.petcommand.api.pet.PetTargetMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has issued a pet command.
 * May be about one pet or all pets.
 * @author Skyler
 *
 */
public class PetCommandMessage {

	public static void handle(PetCommandMessage message, Supplier<NetworkEvent.Context> ctx) {
		final ServerPlayer sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		// Can't call from network threads because manager doesn't sync entity target modification
		
		ctx.get().enqueueWork(() -> {
			final @Nullable LivingEntity target;
			final @Nullable Mob pet;
			
			if (message.targetUUID != null) {
				Entity e = PetCommand.GetEntityByUUID(sp.level, message.targetUUID);
				if (e instanceof LivingEntity) {
					target = (LivingEntity) e;
				} else {
					target = null;
				}
			} else {
				target = null;
			}
			
			if (message.petUUID != null) {
				Entity e = PetCommand.GetEntityByUUID(sp.level, message.petUUID);
				if (e instanceof Mob) {
					pet = (Mob) e;
				} else {
					pet = null;
				}
			} else {
				pet = null;
			}
			
			switch (message.type) {
			case STOP:
				//if (pet == null) {
				PetCommand.GetPetCommandManager().commandAllStopAttacking(sp);
				//}
				break;
			case ATTACK:
				if (target == null) {
					PetCommand.LOGGER.error("Received pet attack command with no target");
					break;
				}
				
				if (pet == null) {
					PetCommand.GetPetCommandManager().commandAllToAttack(sp, target);
				} else {
					PetCommand.GetPetCommandManager().commandToAttack(sp, pet, target);
				}
				break;
			case SET_PLACEMENT_MODE:
				if (message.placementMode == null) {
					PetCommand.LOGGER.error("Received pet placement mode with null mode");
					break;
				}
				
				PetCommand.GetPetCommandManager().setPlacementMode(sp, message.placementMode);
				break;
			case SET_TARGET_MODE:
				if (message.targetMode == null) {
					PetCommand.LOGGER.error("Received pet target mode with null mode");
					break;
				}
				
				PetCommand.GetPetCommandManager().setTargetMode(sp, message.targetMode);
				break;
			}
		});
	}
	
	public static enum PetCommandMessageType {
		STOP,
		ATTACK,
		SET_PLACEMENT_MODE,
		SET_TARGET_MODE,
	}
	
	protected final PetCommandMessageType type;
	protected final @Nullable UUID petUUID;
	protected final @Nullable UUID targetUUID;
	protected final @Nullable PetPlacementMode placementMode;
	protected final @Nullable PetTargetMode targetMode;

	
	private PetCommandMessage(PetCommandMessageType type,
			@Nullable UUID petUUID,
			@Nullable UUID targetUUID,
			@Nullable PetPlacementMode placement,
			@Nullable PetTargetMode target) {
		this.type = type;
		this.petUUID = petUUID;
		this.targetUUID = targetUUID;
		this.placementMode = placement;
		this.targetMode = target;
	}
	
	public static PetCommandMessage AllStop() {
		return new PetCommandMessage(PetCommandMessageType.STOP, null, null, null, null);
	}
	
	public static PetCommandMessage PetStop(LivingEntity pet) {
		return new PetCommandMessage(PetCommandMessageType.STOP, pet.getUUID(), null, null, null);
	}
	
	public static PetCommandMessage AllAttack(LivingEntity target) {
		return new PetCommandMessage(PetCommandMessageType.ATTACK, null, target.getUUID(), null, null);
	}
	
	public static PetCommandMessage PetAttack(LivingEntity pet, LivingEntity target) {
		return new PetCommandMessage(PetCommandMessageType.ATTACK, pet.getUUID(), target.getUUID(), null, null);
	}
	
	public static PetCommandMessage AllPlacementMode(PetPlacementMode mode) {
		return new PetCommandMessage(PetCommandMessageType.SET_PLACEMENT_MODE, null, null, mode, null);
	}
	
	public static PetCommandMessage AllTargetMode(PetTargetMode mode) {
		return new PetCommandMessage(PetCommandMessageType.SET_TARGET_MODE, null, null, null, mode);
	}
	
	public static PetCommandMessage decode(FriendlyByteBuf buf) {
		final PetCommandMessageType type;
		final @Nullable UUID petUUID;
		final @Nullable UUID targetUUID;
		final @Nullable PetPlacementMode placementMode;
		final @Nullable PetTargetMode targetMode;
		
		type = buf.readEnum(PetCommandMessageType.class);
		petUUID = buf.readBoolean() ? buf.readUUID() : null;
		targetUUID = buf.readBoolean() ? buf.readUUID() : null;
		placementMode = buf.readBoolean() ? buf.readEnum(PetPlacementMode.class) : null;
		targetMode = buf.readBoolean() ? buf.readEnum(PetTargetMode.class) : null;
		
		return new PetCommandMessage(type, petUUID, targetUUID, placementMode, targetMode);
	}

	public static void encode(PetCommandMessage msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.type);
		
		buf.writeBoolean(msg.petUUID != null);
		if (msg.petUUID != null) {
			buf.writeUUID(msg.petUUID);
		}
		
		buf.writeBoolean(msg.targetUUID != null);
		if (msg.targetUUID != null) {
			buf.writeUUID(msg.targetUUID);
		}
		
		buf.writeBoolean(msg.placementMode != null);
		if (msg.placementMode != null) {
			buf.writeEnum(msg.placementMode);
		}
		
		buf.writeBoolean(msg.targetMode != null);
		if (msg.targetMode != null) {
			buf.writeEnum(msg.targetMode);
		}
	}
	
}
