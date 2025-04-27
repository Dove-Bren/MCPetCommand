package com.smanzana.petcommand.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.pet.EPetPlacementMode;
import com.smanzana.petcommand.api.pet.EPetTargetMode;
import com.smanzana.petcommand.pet.PetOrder;

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
				if (pet == null) {
					PetCommand.GetPetCommandManager().commandAllStop(sp);
				} else {
					PetCommand.GetPetCommandManager().commandToStop(sp, pet);
				}
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
			case SET_ORDER:
				if (message.order == null) {
					PetCommand.LOGGER.error("Received pet order with no order attached");
					break;
				}
				
				PetCommand.GetPetCommandManager().setPetOrder(sp, pet, message.order);
				break;
			}
		});
	}
	
	public static enum PetCommandMessageType {
		STOP,
		ATTACK,
		SET_PLACEMENT_MODE,
		SET_TARGET_MODE,
		SET_ORDER,
	}
	
	protected final PetCommandMessageType type;
	protected final @Nullable UUID petUUID;
	protected final @Nullable UUID targetUUID;
	protected final @Nullable EPetPlacementMode placementMode;
	protected final @Nullable EPetTargetMode targetMode;
	protected final @Nullable PetOrder order;

	
	private PetCommandMessage(PetCommandMessageType type,
			@Nullable UUID petUUID,
			@Nullable UUID targetUUID,
			@Nullable EPetPlacementMode placement,
			@Nullable EPetTargetMode target,
			@Nullable PetOrder order) {
		this.type = type;
		this.petUUID = petUUID;
		this.targetUUID = targetUUID;
		this.placementMode = placement;
		this.targetMode = target;
		this.order = order;
	}
	
	public static PetCommandMessage AllStop() {
		return new PetCommandMessage(PetCommandMessageType.STOP, null, null, null, null, null);
	}
	
	public static PetCommandMessage PetStop(LivingEntity pet) {
		return new PetCommandMessage(PetCommandMessageType.STOP, pet.getUUID(), null, null, null, null);
	}
	
	public static PetCommandMessage AllAttack(LivingEntity target) {
		return new PetCommandMessage(PetCommandMessageType.ATTACK, null, target.getUUID(), null, null, null);
	}
	
	public static PetCommandMessage PetAttack(LivingEntity pet, LivingEntity target) {
		return new PetCommandMessage(PetCommandMessageType.ATTACK, pet.getUUID(), target.getUUID(), null, null, null);
	}
	
	public static PetCommandMessage AllPlacementMode(EPetPlacementMode mode) {
		return new PetCommandMessage(PetCommandMessageType.SET_PLACEMENT_MODE, null, null, mode, null, null);
	}
	
	public static PetCommandMessage AllTargetMode(EPetTargetMode mode) {
		return new PetCommandMessage(PetCommandMessageType.SET_TARGET_MODE, null, null, null, mode, null);
	}
	
	public static PetCommandMessage PetOrder(LivingEntity pet, PetOrder order) {
		return new PetCommandMessage(PetCommandMessageType.SET_ORDER, pet.getUUID(), null, null, null, order);
	}
	
	public static PetCommandMessage decode(FriendlyByteBuf buf) {
		final PetCommandMessageType type;
		final @Nullable UUID petUUID;
		final @Nullable UUID targetUUID;
		final @Nullable EPetPlacementMode placementMode;
		final @Nullable EPetTargetMode targetMode;
		final @Nullable PetOrder order;
		
		type = buf.readEnum(PetCommandMessageType.class);
		petUUID = buf.readBoolean() ? buf.readUUID() : null;
		targetUUID = buf.readBoolean() ? buf.readUUID() : null;
		placementMode = buf.readBoolean() ? buf.readEnum(EPetPlacementMode.class) : null;
		targetMode = buf.readBoolean() ? buf.readEnum(EPetTargetMode.class) : null;
		order = buf.readBoolean() ? PetOrder.FromNBT(buf.readNbt()) : null;
		
		return new PetCommandMessage(type, petUUID, targetUUID, placementMode, targetMode, order);
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
		
		buf.writeBoolean(msg.order != null);
		if (msg.order != null) {
			buf.writeNbt(msg.order.toNBT());
		}
	}
	
}
