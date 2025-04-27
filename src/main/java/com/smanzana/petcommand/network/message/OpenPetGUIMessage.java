package com.smanzana.petcommand.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.petcommand.api.PetCommandAPI;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has requested a pet GUI be opened
 * @author Skyler
 *
 */
public class OpenPetGUIMessage {

	public static void handle(OpenPetGUIMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Get ID
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			@Nullable Entity ent = ctx.get().getSender().getLevel().getEntity(message.entID);
			if (ent == null || ent.distanceTo(ctx.get().getSender()) > 10 || !(ent instanceof LivingEntity)) {
				; // do nothing
			} else {
				PetCommandAPI.OpenPetGUI(ctx.get().getSender(), (LivingEntity) ent);
			}
		});
	}

	private final int entID;
	
	public OpenPetGUIMessage(int id) {
		this.entID = id;
	}
	
	public OpenPetGUIMessage(LivingEntity entity) {
		this(entity.getId());
	}

	public static OpenPetGUIMessage decode(FriendlyByteBuf buf) {
		return new OpenPetGUIMessage(buf.readVarInt());
	}

	public static void encode(OpenPetGUIMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.entID);
	}

}
