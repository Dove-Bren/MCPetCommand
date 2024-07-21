package com.smanzana.petcommand.network.message;

import java.util.function.Supplier;

import com.smanzana.petcommand.client.petgui.PetGUI;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Server is sending some syncing data to the client GUI
 * @author Skyler
 *
 */
public class PetGUISyncMessage {

	public static void handle(PetGUISyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Get ID
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().submit(() -> {
			PetGUI.updateClientContainer(message.data);
		});
	}

	private final CompoundTag data;
	
	public PetGUISyncMessage(CompoundTag data) {
		this.data = data;
	}

	public static PetGUISyncMessage decode(FriendlyByteBuf buf) {
		return new PetGUISyncMessage(buf.readNbt());
	}

	public static void encode(PetGUISyncMessage msg, FriendlyByteBuf buf) {
		buf.writeNbt(msg.data);
	}

}
