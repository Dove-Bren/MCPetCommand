package com.smanzana.petcommand.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.smanzana.petcommand.client.overlay.OverlayRenderer;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class PetCommandRenderTypes extends RenderType {

	protected static final RenderType PET_TARGET_ICON;
	
	private static final String Name(String suffix) {
		return "petcommandrender_" + suffix;
	}
	
	static {
		// Define render types
		RenderType.CompositeState glState;
				
		glState = RenderType.CompositeState.builder()
				.setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(OverlayRenderer.GUI_PET_ICONS, false, false))
				.setCullState(NO_CULL)
				.setLightmapState(NO_LIGHTMAP)
				.setOutputState(ITEM_ENTITY_TARGET)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.createCompositeState(false);
		PET_TARGET_ICON = RenderType.create(Name("TargetIcon"), DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 32, false, false, glState);
	}

	private PetCommandRenderTypes(String p_173178_, VertexFormat p_173179_, Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
		super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
		throw new IllegalStateException("Now supposed to be instantiated");
	}
	
	public static final RenderType getPetTargetIcon() {
		return PET_TARGET_ICON;
	}
}
