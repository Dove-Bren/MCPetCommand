package com.smanzana.petcommand.sound;

import java.util.Random;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public enum PetCommandSounds {
	
	UI_TICK("ui.tick", SoundCategory.AMBIENT, .4f),
	;
	
	private ResourceLocation resource;
	private SoundCategory category;
	private SoundEvent event;
	private float volume;
	
	private static Random soundRandom = new Random();
	
	private PetCommandSounds(String suffix, SoundCategory category) {
		this(suffix, category, 1.0f);
	}
	
	private PetCommandSounds(String suffix, SoundCategory category, float volume) {
		this.resource = new ResourceLocation(PetCommand.MODID, suffix);
		this.category = category;
		this.event = new SoundEvent(resource);
		event.setRegistryName(resource);
		this.volume = volume;
	}
	
	public ResourceLocation getLocation() {
		return this.resource;
	}
	
	public void play(Entity at) {
		play(null, at.world, at.getPositionVec());
	}
	
	public void play(PlayerEntity at) {
		play(at, at.world, at.getPositionVec());
	}
	
	public void play(PlayerEntity player, World world, Vector3d at) {
		play(player, world, at.x, at.y, at.z);
	}
	
	public void play(World world, double x, double y, double z) {
		play(null, world, x, y, z);
	}
	
	public void playClient(Entity at) {
		playClient(at.world, at.getPosX(), at.getPosY(), at.getPosZ());
	}
	
	public void playClient(World world, double x, double y, double z) {
		world.playSound(x, y, z, event, category, volume, .8f + (soundRandom.nextFloat() * 0.4f), false);
	}
	
	public void play(PlayerEntity player, World world, double x, double y, double z) {
		world.playSound(player, x, y, z,
				event, category,
				volume, 0.8f + (soundRandom.nextFloat() * 0.4f));
	}
	
	public SoundEvent getEvent() {
		return event;
	}
	
}
