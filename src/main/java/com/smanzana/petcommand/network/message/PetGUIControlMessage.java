package com.smanzana.petcommand.network.message;

import java.util.function.Supplier;

import com.smanzana.petcommand.client.petgui.PetGUI;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
	private CompoundNBT data;
	
	public PetGUIControlMessage(int id, CompoundNBT data) {
		this.id = id;
		this.data = data;
	}

	public static PetGUIControlMessage decode(PacketBuffer buf) {
		return new PetGUIControlMessage(buf.readVarInt(), buf.readNbt());
	}

	public static void encode(PetGUIControlMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.id);
		buf.writeNbt(msg.data);
	}

}
