package com.smanzana.petcommand.network.message;

import java.util.function.Supplier;

import com.smanzana.petcommand.client.petgui.PetGUI;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client has performed some action in a dragon GUI
 * @author Skyler
 *
 */
public class PetGUIControlMessage {

	public static void handle(PetGUIControlMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Get ID
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			PetGUI.updateServerContainer(message.id, message.data);
		});
	}

	private final int id;
	private CompoundTag data;
	
	public PetGUIControlMessage(int id, CompoundTag data) {
		this.id = id;
		this.data = data;
	}

	public static PetGUIControlMessage decode(FriendlyByteBuf buf) {
		return new PetGUIControlMessage(buf.readVarInt(), buf.readNbt());
	}

	public static void encode(PetGUIControlMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.id);
		buf.writeNbt(msg.data);
	}

}
