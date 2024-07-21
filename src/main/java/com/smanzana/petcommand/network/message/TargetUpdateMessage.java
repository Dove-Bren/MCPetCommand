package com.smanzana.petcommand.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is sending info about a change in targets to a client
 * @author Skyler
 *
 */
public class TargetUpdateMessage {

	public static void handle(TargetUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().submit(() -> {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) {
				return;
			}
			try {
				MobEntity mob = (MobEntity) mc.player.level.getEntity(message.sourceID);
				LivingEntity target = message.targetID == null ? null : (LivingEntity) mc.player.level.getEntity(message.targetID);
				PetCommand.GetClientTargetManager().updateTarget(mob, target);
			} catch (Exception e) {
				PetCommand.LOGGER.error("Received a target update for entities we didn't know: " + message.sourceID + " => " + (message.targetID == null ? "NULL" : message.targetID));
				e.printStackTrace();
			}
		});
	}
		
	private final int sourceID;
	private final @Nullable Integer targetID;
	
	public TargetUpdateMessage(int sourceID, @Nullable Integer targetID) {
		this.sourceID = sourceID;
		this.targetID = targetID;
	}
	
	public TargetUpdateMessage(MobEntity source, @Nullable LivingEntity target) {
		this(source.getId(), target == null ? null : target.getId());
	}

	public static TargetUpdateMessage decode(PacketBuffer buf) {
		return new TargetUpdateMessage(buf.readVarInt(), buf.readBoolean() ? buf.readVarInt() : null);
	}

	public static void encode(TargetUpdateMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.sourceID);
		buf.writeBoolean(msg.targetID != null);
		if (msg.targetID != null) {
			buf.writeVarInt(msg.targetID);
		}
	}

}
