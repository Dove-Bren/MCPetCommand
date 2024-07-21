package com.smanzana.petcommand.network;

import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.network.message.PetCommandMessage;
import com.smanzana.petcommand.network.message.PetCommandSettingsSyncMessage;
import com.smanzana.petcommand.network.message.PetGUIControlMessage;
import com.smanzana.petcommand.network.message.PetGUISyncMessage;
import com.smanzana.petcommand.network.message.TargetUpdateMessage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {

	private static SimpleChannel syncChannel;
	
	private static int discriminator = 97;
	
	private static final String CHANNEL_SYNC_NAME = "petcommand_channel";
	private static final String PROTOCOL = "1";
	
	public static SimpleChannel getSyncChannel() {
		getInstance();
		return syncChannel;
	}
	
	private static NetworkHandler instance;
	
	public static NetworkHandler getInstance() {
		if (instance == null)
			instance = new NetworkHandler();
		
		return instance;
	}
	
	public NetworkHandler() {
		
		syncChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(PetCommand.MODID, CHANNEL_SYNC_NAME),
				() -> PROTOCOL,
				PROTOCOL::equals,
				PROTOCOL::equals
				);
		
		syncChannel.registerMessage(discriminator++, PetGUIControlMessage.class, PetGUIControlMessage::encode, PetGUIControlMessage::decode, PetGUIControlMessage::handle);
		syncChannel.registerMessage(discriminator++, PetGUISyncMessage.class, PetGUISyncMessage::encode, PetGUISyncMessage::decode, PetGUISyncMessage::handle);
		syncChannel.registerMessage(discriminator++, PetCommandMessage.class, PetCommandMessage::encode, PetCommandMessage::decode, PetCommandMessage::handle);
		syncChannel.registerMessage(discriminator++, PetCommandSettingsSyncMessage.class, PetCommandSettingsSyncMessage::encode, PetCommandSettingsSyncMessage::decode, PetCommandSettingsSyncMessage::handle);
		syncChannel.registerMessage(discriminator++, TargetUpdateMessage.class, TargetUpdateMessage::encode, TargetUpdateMessage::decode, TargetUpdateMessage::handle);
	}
	
	//NetworkHandler.sendTo(new ClientCastReplyMessage(false, att.getMana(), 0, null),
	//ctx.get().getSender());
	
	public static <T> void sendTo(T msg, ServerPlayerEntity player) {
		NetworkHandler.syncChannel.sendTo(msg, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static <T> void sendToServer(T msg) {
		NetworkHandler.syncChannel.sendToServer(msg);
	}

	public static <T> void sendToAll(T msg) {
		NetworkHandler.syncChannel.send(PacketDistributor.ALL.noArg(), msg);
	}

	public static <T> void sendToDimension(T msg, RegistryKey<World> dimension) {
		NetworkHandler.syncChannel.send(PacketDistributor.DIMENSION.with(() -> dimension), msg);
	}
	
	public static <T> void sendToAllAround(T msg, TargetPoint point) {
		NetworkHandler.syncChannel.send(PacketDistributor.NEAR.with(() -> point), msg);
	}

	public static <T> void sendToAllTracking(T msg, Entity ent) {
		NetworkHandler.syncChannel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> ent), msg);
	}
	
	public static <T> void sendToOwner(T msg, MobEntity entity) {
		@Nullable LivingEntity owner = PetFuncs.GetOwner(entity);
		if (owner != null && owner instanceof ServerPlayerEntity) {
			sendTo(msg, (ServerPlayerEntity) owner);
		}
	}

}
