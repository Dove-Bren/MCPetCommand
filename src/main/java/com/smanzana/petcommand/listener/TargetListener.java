package com.smanzana.petcommand.listener;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetCommandAPI;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TargetListener {
	
	private int serverTickCount;
	private int clientTickCount;
	
	public TargetListener() {
		MinecraftForge.EVENT_BUS.register(this);
		serverTickCount = 0;
		clientTickCount = 0;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onAITarget(LivingChangeTargetEvent event) {
		if (!event.isCanceled() && event.getEntityLiving() instanceof Mob) {
			Mob mob = (Mob) event.getEntityLiving();
			PetCommandAPI.GetTargetManager(mob).updateTarget(mob, event.getNewTarget());
		}
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			serverTickCount++;
			if (serverTickCount % 20 == 0) {
				PetCommand.GetServerTargetManager().clean(serverTickCount % 200 == 0);
			}
		}
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			clientTickCount++;
			if (clientTickCount % 20 == 0) {
				PetCommand.GetClientTargetManager().clean(clientTickCount % 200 == 0);
			}
		}
	}
	
}
