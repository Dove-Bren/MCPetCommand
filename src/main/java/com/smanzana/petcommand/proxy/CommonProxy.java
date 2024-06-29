package com.smanzana.petcommand.proxy;

import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.ITargetManager;
import com.smanzana.petcommand.client.petgui.PetGUI;
import com.smanzana.petcommand.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkHooks;

public class CommonProxy {

	public CommonProxy() {
		
	}
	
	public boolean isServer() {
		return true;
	}
	
	public @Nullable PlayerEntity getPlayer() {
		return null;
	}
	
	public <T extends IEntityPet> void openPetGUI(PlayerEntity player, T pet) {
		if (!player.world.isRemote()) {
			this.openContainer(player, PetGUI.PetContainer.Make(pet, player));
		}
	}
	
	public void openContainer(PlayerEntity player, IPackedContainerProvider provider) {
		if (!player.world.isRemote() && player instanceof ServerPlayerEntity) {
			NetworkHooks.openGui((ServerPlayerEntity) player, provider, provider.getData());
		}
	}
	
	public ITargetManager getTargetManager(LivingEntity entity) {
		return PetCommand.GetServerTargetManager();
	}
	
}
