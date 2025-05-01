package com.smanzana.petcommand.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.proxy.ClientProxy;
import com.smanzana.petcommand.util.RayTrace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Renderer for previewing pet orders in the world
 */
public class PetOrderRenderer {
	
	protected boolean enabled;
	protected final Minecraft mc;
	
	public PetOrderRenderer() {
		this.mc = Minecraft.getInstance();
		MinecraftForge.EVENT_BUS.register(this);
		
		// I could imagine this being more generic and rendering some number of pet orders -- the one you might be queuing up,
		// or some that were issued that are being acted on.
		// To not blow up the scope of it though, it's gonna be a specialized what-youre-queuing renderer
		
		enabled = false;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@SubscribeEvent
	public void onRender(RenderLevelStageEvent event) {
		if (!enabled || event.getStage() != Stage.AFTER_PARTICLES) {
			return;
		}
		
		if (!((ClientProxy) PetCommand.GetProxy()).isPetOrderPressed()) {
			return;
		}
		
		final Player player = PetCommand.GetProxy().getPlayer();
		final boolean isGuard = ((ClientProxy) PetCommand.GetProxy()).isPetOrderGuard();
		
		PoseStack matrixStack = event.getPoseStack();
		final float partialTicks = event.getPartialTick();
		
		HitResult result = RayTrace.raytrace(
				player.level, player,
				player.getEyePosition(partialTicks),
				player.getViewVector(partialTicks),
				100, (e) -> false);
		BlockPos pos = RayTrace.outsideBlockPosFromResult(result);
		if (pos != null) {
			final Vec3 renderOffset = Vec3.atBottomCenterOf(pos).subtract(event.getCamera().getPosition());
			MultiBufferSource buffer = mc.renderBuffers().bufferSource();
			final int light = 15728880;//mc.level.getBrightness(LightLayer.BLOCK, pos);
			final int ticks = event.getRenderTick();
			
			matrixStack.pushPose();
			matrixStack.translate(renderOffset.x, renderOffset.y, renderOffset.z);
			renderPlaceMarker(matrixStack, buffer, ticks, partialTicks, light);
			if (isGuard) {
				renderGuardMarker(matrixStack, buffer, ticks, partialTicks, light);
			} else {
				renderMoveToMarker(matrixStack, buffer, ticks, partialTicks, light);
			}
			
			matrixStack.popPose();
			
			renderBeam(matrixStack, buffer, ticks, partialTicks, new Vec3(pos.getX() + 5, pos.getY(), pos.getZ() + .5));
			
			//mc.renderBuffers().bufferSource().endBatch();
		}
	}
	
	private ItemStack markerItem = new ItemStack(() -> Items.AMETHYST_SHARD);
	private ItemStack guardItem = new ItemStack(() -> Items.SHIELD);
	
	protected void renderPlaceMarker(PoseStack matrixStackIn, MultiBufferSource bufferIn,  int renderTicks, float partialTicks, int packedLight) {
		final float floatProg = ((renderTicks + partialTicks) % 30f) / 30f;
		final float yMag = (float) Math.cos(floatProg * Math.PI * 2);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, .5 + (yMag * .125), 0);
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(135f));
		mc.getItemRenderer().renderStatic(markerItem, TransformType.FIXED, packedLight, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, 0);
		matrixStackIn.popPose();
		
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90f));
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(135f));
		mc.getItemRenderer().renderStatic(markerItem, TransformType.FIXED, packedLight, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, 0);
		matrixStackIn.popPose();
	}
	
	public static final ResourceLocation TEX_MOVE = new ResourceLocation(PetCommand.MODID, "textures/effect/move_arrow.png");
	protected void renderMoveToMarker(PoseStack matrixStackIn, MultiBufferSource bufferIn, int renderTicks, float partialTicks, int packedLight) {
		
		VertexConsumer buffer = bufferIn.getBuffer(PetCommandRenderTypes.PET_ORDER_MOVE_ARROW);
		{
			final float width = .5f;
			final float height = .5f;
			final float uOffset = 1f - (((renderTicks + partialTicks) % 60f) / 60f);
			final float minU = 1f + uOffset;
			final float maxU = 0 + uOffset;
			final float minV = 0f;
			final float maxV = 1f;
			final float color[] = {.4f, .9f, .6f, 1f};
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, .125f, 0);
			
			for (int i = 0; i < 4; i++) {
				matrixStackIn.pushPose();
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90f * i));
				matrixStackIn.translate(.25, 0, -height/2f);
				matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(20f));
				{
					final Matrix4f transform = matrixStackIn.last().pose();
					//blit(matrixStackIn, 0, 0, GUI_PET_ICON_FLOATTARGET_HOFFSET, GUI_PET_ICON_FLOATTARGET_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
					buffer.vertex(transform, 0f, 0f, height).color(color[0], color[1], color[2], color[3]).uv(minU, maxV).endVertex();
					buffer.vertex(transform, width, 0f, height).color(color[0], color[1], color[2], color[3]).uv(maxU, maxV).endVertex();
					buffer.vertex(transform, width, 0f, 0).color(color[0], color[1], color[2], color[3]).uv(maxU, minV).endVertex();
					buffer.vertex(transform, 0f, 0f, 0).color(color[0], color[1], color[2], color[3]).uv(minU, minV).endVertex();
				}
				matrixStackIn.popPose();
			}
			
			matrixStackIn.popPose();
		}
	}
	
	protected void renderGuardMarker(PoseStack matrixStackIn, MultiBufferSource bufferIn,  int renderTicks, float partialTicks, int packedLight) {
		final float floatProg = ((renderTicks + partialTicks) % 80f) / 80f;
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, .75, 0);
		for (int i = 0; i < 4; i++) {
			matrixStackIn.pushPose();
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees((90f * i) + (floatProg * 360f)));
			matrixStackIn.translate(-.125, 0, -.5);
			mc.getItemRenderer().renderStatic(guardItem, TransformType.FIXED, packedLight, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, 0);
			matrixStackIn.popPose();
			
		}
		matrixStackIn.popPose();
	}
	
	protected void renderBeam(PoseStack matrixStackIn, MultiBufferSource bufferIn, int renderTicks, float partialTicks, Vec3 dest) {
		
	}

}
