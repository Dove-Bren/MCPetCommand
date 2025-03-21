package com.smanzana.petcommand.sound;

import java.util.Random;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public enum PetCommandSounds {
	
	UI_TICK("ui.tick", SoundSource.AMBIENT, .4f),
	;
	
	private ResourceLocation resource;
	private SoundSource category;
	private SoundEvent event;
	private float volume;
	
	private static Random soundRandom = new Random();
	
	private PetCommandSounds(String suffix, SoundSource category) {
		this(suffix, category, 1.0f);
	}
	
	private PetCommandSounds(String suffix, SoundSource category, float volume) {
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
		play(null, at.level, at.position());
	}
	
	public void play(Player at) {
		play(at, at.level, at.position());
	}
	
	public void play(Player player, Level world, Vec3 at) {
		play(player, world, at.x, at.y, at.z);
	}
	
	public void play(Level world, double x, double y, double z) {
		play(null, world, x, y, z);
	}
	
	public void playClient(Entity at) {
		playClient(at.level, at.getX(), at.getY(), at.getZ());
	}
	
	public void playClient(Level world, double x, double y, double z) {
		world.playLocalSound(x, y, z, event, category, volume, .8f + (soundRandom.nextFloat() * 0.4f), false);
	}
	
	public void play(Player player, Level world, double x, double y, double z) {
		world.playSound(player, x, y, z,
				event, category,
				volume, 0.8f + (soundRandom.nextFloat() * 0.4f));
	}
	
	public SoundEvent getEvent() {
		return event;
	}
	
}
