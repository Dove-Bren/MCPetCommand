package com.smanzana.petcommand.client.render;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.ResourceLocation;

public class BoundIronGolemRenderer extends IronGolemRenderer {
	
	private static final ResourceLocation BOUND_GOLEM_TEXTURES = new ResourceLocation(PetCommand.MODID, "textures/entity/bound_iron_golem/bound_iron_golem.png");

	public BoundIronGolemRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public ResourceLocation getEntityTexture(IronGolemEntity entity) {
		return BOUND_GOLEM_TEXTURES;
	}

}
