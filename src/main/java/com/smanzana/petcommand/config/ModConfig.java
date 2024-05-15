package com.smanzana.petcommand.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;

public class ModConfig {
	
	public static ModConfig config;
	
	private ForgeConfigSpec clientSpec;
	private ModConfigClient client;
	
	private ForgeConfigSpec commonSpec;
	private ModConfigCommon common;
	
	public ModConfig() {
		final Pair<ModConfigCommon, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(ModConfigCommon::new);
		commonSpec = commonPair.getRight();
		common = commonPair.getLeft();
		
		final Pair<ModConfigClient, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ModConfigClient::new);
		clientSpec = clientPair.getRight();
		client = clientPair.getLeft();
		
		ModConfig.config = this;
		
	}
	
	public void register() {
		ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, clientSpec);
		ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, commonSpec);
	}
	
	public boolean showHealthbars() {
		return client.configShowHealthbars.get();
	}
	
	public boolean showBigHealthbars() {
		return client.configShowBigHealthbars.get();
	}
	
	public int getHealthbarAnchorX() {
		return client.configHealthbarAnchorX.get();
	}
	
	public int getHealthbarAnchorY() {
		return client.configHealthbarAnchorY.get();
	}
}
