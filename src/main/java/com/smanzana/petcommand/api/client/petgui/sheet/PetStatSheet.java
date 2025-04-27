package com.smanzana.petcommand.api.client.petgui.sheet;

import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.PetValue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
	
	protected void drawAttribute(PoseStack matrixStackIn, Font font, String label, String value) {
		final String labelStr = label + ": ";
		final int width = font.width(labelStr);
		font.draw(matrixStackIn, labelStr, 0, 0, 0xFFFFFFFF);
		font.draw(matrixStackIn, value, width, 0, 0xFFAAAAAA);
	}
	
	protected void drawAttribute(PoseStack matrixStackIn, Font font, Component label, String value) {
		final Component labelStr = label.copy().append(": ");
		final int width = font.width(labelStr);
		font.draw(matrixStackIn, labelStr, 0, 0, 0xFFFFFFFF);
		font.draw(matrixStackIn, value, width, 0, 0xFFAAAAAA);
	}

	@Override
	public void draw(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		final int rowMarginBase = 2;
		
		final Font font = mc.font;
		final PetInfo info = pet instanceof IEntityPet p ? p.getPetSummary() : null;
		
		final int rowMargin = font.lineHeight + rowMarginBase;
		
		// Draw sheet
		matrixStackIn.pushPose();
		{
			matrixStackIn.translate(5, 3, 0); // margin
			
			// HP and pet values pulled out first
			if (info != null) {
				drawAttribute(matrixStackIn, font, "Health", String.format("%d / %d", (int) info.getCurrentHp(), (int) info.getMaxHp()));
				matrixStackIn.translate(0, rowMargin, 0);
				
				List<PetValue> values = info.getPetValues();
				if (values != null) {
					for (PetValue value : values) {
						drawAttribute(matrixStackIn, font, value.label(), String.format("%d / %d", (int) value.current(), (int) value.max()));
						matrixStackIn.translate(0, rowMargin, 0);
					}
				}
			} else {
				drawAttribute(matrixStackIn, font, "Health", String.format("%d / %d", (int) pet.getHealth(), (int) pet.getMaxHealth()));
				matrixStackIn.translate(0, rowMargin, 0);
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
				if (attribute == null || !attributes.hasAttribute(attribute) || hideAttributes.contains(attribute) ) {
					continue;
				}
				
				AttributeInstance attr = attributes.getInstance(attribute);
				if (attr.getValue() == 0) {
					continue;
				}
				
				drawAttribute(matrixStackIn, font, new TranslatableComponent(attr.getAttribute().getDescriptionId()), String.format("%.2f", attr.getValue()));
				matrixStackIn.translate(0, rowMargin, 0);
			}
		}
		matrixStackIn.popPose();
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
	public void overlay(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

	@Override
	public boolean shouldShow(T pet, IPetContainer<T> container) {
		return true;
	}

}
