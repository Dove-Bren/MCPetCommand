package com.smanzana.petcommand.network.message;

import java.util.function.Supplier;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client player's attribtes are being refreshed from server
 * @author Skyler
 *
 */
public class PetCommandSettingsSyncMessage {

	public static void handle(PetCommandSettingsSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().submit(() -> {
			PetCommand.GetPetCommandManager().overrideClientSettings(message.data);
		});
	}
		
	private final CompoundTag data;
	
	public PetCommandSettingsSyncMessage(CompoundTag nbt) {
		data = nbt;
	}

	public static PetCommandSettingsSyncMessage decode(FriendlyByteBuf buf) {
		return new PetCommandSettingsSyncMessage(buf.readNbt());
	}

	public static void encode(PetCommandSettingsSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeNbt(msg.data);
	}

}
