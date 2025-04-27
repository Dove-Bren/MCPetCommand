package com.smanzana.petcommand.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Intended to be a client-only persisted set of overrides for certain preferences per pet.
 * For example, an override on the color of the pet's icon.
 */
public class PetOverrides {

	protected static record Override(Integer color, Integer valueIdx, Boolean hide) {
		
		/**
		 * Returns null if the override doesn't express anything and should be removed from the map
		 * @return
		 */
		public Override compact() {
			if (this.equals(EMPTY)) {
				return null;
			}
			return this;
		}
		
		public final CompoundTag toNBT() {
			CompoundTag tag = new CompoundTag();
			
			if (color != null) {
				tag.putInt("color", color());
			}
			if (valueIdx != null) {
				tag.putInt("valueIdx", valueIdx());
			}
			if (hide != null) {
				tag.putBoolean("hide", hide());
			}
			
			return tag;
		}
		
		public static final Override fromNBT(CompoundTag tag) {
			final Integer color;
			final Integer valueIdx;
			final Boolean hide;
			
			if (tag.contains("color", Tag.TAG_INT)) {
				color = tag.getInt("color");
			} else {
				color = null;
			}
			
			if (tag.contains("valueIdx", Tag.TAG_INT)) {
				valueIdx = tag.getInt("valueIdx");
			} else {
				valueIdx = null;
			}
			
			if (tag.contains("hide")) {
				hide = tag.getBoolean("hide");
			} else {
				hide = null;
			}
			
			return new Override(color, valueIdx, hide);
		}
	}
	
	protected static final Override EMPTY = new Override(null, null, null);
	
	private final Map<UUID, Override> overrides;
	
	public PetOverrides() {
		overrides = new HashMap<>();
	}
	
	protected final File getConfigFile() {
		return new File(FMLPaths.CONFIGDIR.get().toFile(), "PetCommand_Overrides.dat");
	}
	
	public void loadFromDisk() {
		File file = getConfigFile();
		try {
			if (file.exists()) {
				CompoundTag tag = NbtIo.read(file);
				this.load(tag);
			}
		} catch (Exception e) {
			
		}
	}
	
	protected void load(CompoundTag tag) {
		this.overrides.clear();
		
		if (tag != null) {
			for (String key : tag.getAllKeys()) {
				overrides.put(UUID.fromString(key), Override.fromNBT(tag.getCompound(key)));
			}
		}
	}
	
	public void saveToDisk() {
		final CompoundTag tag = save();
		File file = getConfigFile();
		try {
			NbtIo.write(tag, file);
		} catch (Exception e) {
			
		}
	}
	
	protected CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		
		for (Entry<UUID, Override> entry : this.overrides.entrySet()) {
			tag.put(entry.getKey().toString(), entry.getValue().toNBT());
		}
		
		return tag;
	}
	
	
	
	
	
	
	
	public @Nullable Integer getColor(UUID id) {
		return overrides.getOrDefault(id, EMPTY).color();
	}
	
	public void setColor(UUID id, @Nullable Integer color) {
		overrides.merge(id, new Override(color, null, null), (existing, fresh) -> new Override(fresh.color(), existing.valueIdx(), existing.hide()).compact());
		this.saveToDisk();
	}

	public @Nullable Integer getValueIdx(UUID id) {
		return overrides.getOrDefault(id, EMPTY).valueIdx();
	}
	
	public void setValueIdx(UUID id, @Nullable Integer valueIdx) {
		overrides.merge(id, new Override(null, valueIdx, null), (existing, fresh) -> new Override(existing.color(), fresh.valueIdx(), existing.hide()).compact());
		this.saveToDisk();
	}

	public @Nullable Boolean getHide(UUID id) {
		return overrides.getOrDefault(id, EMPTY).hide();
	}
	
	public void setHide(UUID id, @Nullable Boolean hide) {
		overrides.merge(id, new Override(null, null, hide), (existing, fresh) -> new Override(existing.color(), existing.valueIdx(), fresh.hide()).compact());
		this.saveToDisk();
	}
	
}
