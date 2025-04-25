package com.smanzana.petcommand.proxy;

import javax.annotation.Nullable;

import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.client.pet.ISelectionManager;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.ITargetManager;
import com.smanzana.petcommand.client.petgui.PetGUI;
import com.smanzana.petcommand.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

public class CommonProxy {

	public CommonProxy() {
		
	}
	
	public boolean isServer() {
		return true;
	}
	
	public @Nullable Player getPlayer() {
		return null;
	}
	
	public <T extends IEntityPet> void openPetGUI(Player player, T pet) {
		if (!player.level.isClientSide()) {
			this.openContainer(player, PetGUI.PetContainer.Make(pet, player));
		}
	}
	
	public void openContainer(Player player, IPackedContainerProvider provider) {
		if (!player.level.isClientSide() && player instanceof ServerPlayer) {
			NetworkHooks.openGui((ServerPlayer) player, provider, provider.getData());
		}
	}
	
	public ITargetManager getTargetManager(LivingEntity entity) {
		return PetCommand.GetServerTargetManager();
	}

	public ISelectionManager getSelectionManager() {
		return null;
	}
	
}
