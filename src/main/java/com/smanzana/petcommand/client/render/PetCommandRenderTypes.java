package com.smanzana.petcommand.client.render;

import org.lwjgl.opengl.GL11;

import com.smanzana.petcommand.client.overlay.OverlayRenderer;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class PetCommandRenderTypes {

	public static final RenderType PET_TARGET_ICON;
	
	private static final String Name(String suffix) {
		return "petcommandrender_" + suffix;
	}
	
	static {
		
		//final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_");
		final RenderState.TargetState ITEM_ENTITY_TARGET = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_241712_U_");
		final RenderState.LayerState VIEW_OFFSET_Z_LAYERING = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_239235_M_");
		final RenderState.CullState NO_CULL = new RenderState.CullState(false);
		//final RenderState.DepthTestState DEPTH_EQUAL = new RenderState.DepthTestState("==", GL11.GL_EQUAL);
		//final RenderState.DepthTestState NO_DEPTH = new RenderState.DepthTestState("none", GL11.GL_ALWAYS);
		final RenderState.LightmapState NO_LIGHTING = new RenderState.LightmapState(false);
	    //final RenderState.LightmapState LIGHTMAP_ENABLED = new RenderState.LightmapState(true);
	    //final RenderState.LineState LINE_2 = new RenderState.LineState(OptionalDouble.of(2));
	    //final RenderState.LineState LINE_3 = new RenderState.LineState(OptionalDouble.of(3));
	    //@SuppressWarnings("deprecation")
		//final RenderState.TextureState BLOCK_SHEET = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, false);
	    final RenderState.AlphaState HALF_ALPHA = new RenderState.AlphaState(.5f);
	    //final RenderState.ShadeModelState SHADE_ENABLED = new RenderState.ShadeModelState(true);
	    //final RenderState.WriteMaskState NO_DEPTH_WRITE = new RenderState.WriteMaskState(true, false);
		
		// Define render types
		RenderType.State glState;
				
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(OverlayRenderer.GUI_PET_ICONS, false, false))
				.cull(NO_CULL)
				.lightmap(NO_LIGHTING)
				.alpha(HALF_ALPHA) // cutout
				.target(ITEM_ENTITY_TARGET)
				.layer(VIEW_OFFSET_Z_LAYERING)
			.build(false);
		PET_TARGET_ICON = RenderType.makeType(Name("TargetIcon"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 32, glState);
	}
}
