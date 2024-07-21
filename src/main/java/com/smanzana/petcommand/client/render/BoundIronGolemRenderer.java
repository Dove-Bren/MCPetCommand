package com.smanzana.petcommand.client.render;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

public class BoundIronGolemRenderer extends IronGolemRenderer {
	
	private static final ResourceLocation BOUND_GOLEM_TEXTURES = new ResourceLocation(PetCommand.MODID, "textures/entity/bound_iron_golem/bound_iron_golem.png");

	public BoundIronGolemRenderer(EntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public ResourceLocation getTextureLocation(IronGolem entity) {
		return BOUND_GOLEM_TEXTURES;
	}

}
