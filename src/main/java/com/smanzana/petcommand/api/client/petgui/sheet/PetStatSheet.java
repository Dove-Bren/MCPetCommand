package com.smanzana.petcommand.api.client.petgui.sheet;

import java.util.List;
import java.util.Set;

import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.PetValue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Default implementation of a pet stat and attribute sheet.
 * @param <T>
 */
public class PetStatSheet<T extends LivingEntity> implements IPetGUISheet<T> {
	
	protected final T pet;
	
	public PetStatSheet(T pet) {
		this.pet = pet;
	}
	
	@Override
	public void showSheet(T pet, Player player, IPetContainer<T> container, int width, int height, int offsetX, int offsetY) {
		
	}

	@Override
	public void hideSheet(T pet, Player player, IPetContainer<T> container) {
		container.clearSlots();
	}
	
	protected void drawAttribute(GuiGraphics graphics, Font font, String label, String value) {
		final String labelStr = label + ": ";
		final int width = font.width(labelStr);
		graphics.drawString(font, labelStr, 0, 0, 0xFFFFFFFF);
		graphics.drawString(font, value, width, 0, 0xFFAAAAAA);
	}
	
	protected void drawAttribute(GuiGraphics graphics, Font font, Component label, String value) {
		final Component labelStr = label.copy().append(": ");
		final int width = font.width(labelStr);
		graphics.drawString(font, labelStr, 0, 0, 0xFFFFFFFF);
		graphics.drawString(font, value, width, 0, 0xFFAAAAAA);
	}

	@Override
	public void draw(GuiGraphics graphics, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		final int rowMarginBase = 2;
		
		final Font font = mc.font;
		final PetInfo info = pet instanceof IEntityPet p ? p.getPetSummary() : null;
		
		final int rowMargin = font.lineHeight + rowMarginBase;
		
		// Draw sheet
		graphics.pose().pushPose();
		{
			graphics.pose().translate(5, 3, 0); // margin
			
			// HP and pet values pulled out first
			if (info != null) {
				drawAttribute(graphics, font, "Health", String.format("%d / %d", (int) info.getCurrentHp(), (int) info.getMaxHp()));
				graphics.pose().translate(0, rowMargin, 0);
				
				List<PetValue> values = info.getPetValues();
				if (values != null) {
					for (PetValue value : values) {
						drawAttribute(graphics, font, value.label(), String.format("%d / %d", (int) value.current(), (int) value.max()));
						graphics.pose().translate(0, rowMargin, 0);
					}
				}
			} else {
				drawAttribute(graphics, font, "Health", String.format("%d / %d", (int) pet.getHealth(), (int) pet.getMaxHealth()));
				graphics.pose().translate(0, rowMargin, 0);
			}
			
			// Vanilla attributes
			final Set<Attribute> hideAttributes = Set.of(
					Attributes.MAX_HEALTH,
					Attributes.FOLLOW_RANGE,
					Attributes.SPAWN_REINFORCEMENTS_CHANCE,
					ForgeMod.SWIM_SPEED.get(),
					ForgeMod.ENTITY_GRAVITY.get(),
					ForgeMod.NAMETAG_DISTANCE.get()
					);
			var attributes = pet.getAttributes();
			for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
				final var holder = ForgeRegistries.ATTRIBUTES.getHolder(attribute).get();
				if (attribute == null || !attributes.hasAttribute(holder) || hideAttributes.contains(attribute) ) {
					continue;
				}
				
				AttributeInstance attr = attributes.getInstance(holder);
				if (attr.getValue() == 0) {
					continue;
				}
				
				drawAttribute(graphics, font, Component.translatable(attr.getAttribute().getDescriptionId()), String.format("%.2f", attr.getValue()));
				graphics.pose().translate(0, rowMargin, 0);
			}
		}
		graphics.pose().popPose();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return false;
	}

	@Override
	public void handleMessage(CompoundTag data) {
		
	}

	@Override
	public String getButtonText() {
		return "Stats";
	}

	@Override
	public void overlay(GuiGraphics graphics, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

	@Override
	public boolean shouldShow(T pet, IPetContainer<T> container) {
		return true;
	}

}
