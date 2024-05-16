package com.smanzana.petcommand.api.client.petgui;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

public abstract class PetGUIRenderHelper {

	/**
	 * Render an inventory slot with the given transformation matrix, width, and height.
	 * This isn't optimized for rendering much, and binds a texture etc.
	 * @param matrixStackIn
	 * @param width
	 * @param height
	 */
	public static final void DrawSingleSlot(MatrixStack matrixStackIn, int width, int height) {
		if (Impl != null) {
			Impl.drawSingleSlot(matrixStackIn, width, height);
		}
	}
	
	public static final void DrawSlots(MatrixStack matrixStackIn, int width, int height, int count, int columns) {
		if (Impl != null) {
			Impl.drawSlots(matrixStackIn, width, height, count, columns);
		}
	}
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////
	//   
	//                         Implementation                   //
	//
	//////////////////////////////////////////////////////////////
	protected static @Nullable PetGUIRenderHelper Impl;
	
	protected static final void ProvideImpl(PetGUIRenderHelper api) {
		Impl = api;
	}
	
	protected abstract void drawSingleSlot(MatrixStack matrixStackIn, int width, int height);
	protected abstract void drawSlots(MatrixStack matrixStackIn, int width, int height, int count, int columns);
}
