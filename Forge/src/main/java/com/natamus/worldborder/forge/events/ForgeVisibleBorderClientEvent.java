package com.natamus.worldborder.forge.events;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.natamus.worldborder.forge.visibleborder.VisibleBorderClientState;
import com.natamus.worldborder.forge.visibleborder.VisibleBorderSnapshot;
import com.natamus.worldborder.util.Reference;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.BorderStatus;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeVisibleBorderClientEvent {
	private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (!event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_WEATHER)) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		Camera camera = event.getCamera();
		if (level == null || camera == null) {
			return;
		}

		BorderContext context = getActiveContext(minecraft, level, camera);
		if (context == null || context.snapshot.isFogStyle()) {
			return;
		}

		renderForcefieldBorder(minecraft, camera, context);
	}

	@SubscribeEvent
	public static void onRenderFog(ViewportEvent.RenderFog event) {
		Camera camera = event.getCamera();
		if (camera == null || !event.getType().equals(FogType.NONE)) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null) {
			return;
		}

		BorderContext context = getActiveContext(minecraft, level, camera);
		if (context == null || !context.snapshot.isFogStyle()) {
			return;
		}

		double distanceToBorder = Math.max(0.0D, getDistanceToBorder(context.cameraX, context.cameraZ, context.minX, context.maxX, context.minZ, context.maxZ));
		if (distanceToBorder > context.renderDistance) {
			return;
		}

		float borderDistance = (float)distanceToBorder;
		float near;
		float far;
		if (event.getMode().equals(FogRenderer.FogMode.FOG_SKY)) {
			near = 0.0F;
			far = borderDistance;
		}
		else {
			float fogSpan = Mth.clamp(borderDistance / 10.0F, 4.0F, 64.0F);
			near = borderDistance - fogSpan;
			far = borderDistance;
		}

		event.setNearPlaneDistance(near);
		event.setFarPlaneDistance(far);
		event.setFogShape(FogShape.CYLINDER);
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClientPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		VisibleBorderClientState.clear();
	}

	private static BorderContext getActiveContext(Minecraft minecraft, ClientLevel level, Camera camera) {
		VisibleBorderSnapshot snapshot = VisibleBorderClientState.getSnapshot();
		if (snapshot == null || !snapshot.showVisibleBorder) {
			return null;
		}

		VisibleBorderSnapshot.DimensionBounds bounds = snapshot.getDimensionBounds(level.dimension().location().toString());
		if (bounds == null || !bounds.enabled || !bounds.hasValidBounds()) {
			return null;
		}

		double renderDistance = (double)(minecraft.options.getEffectiveRenderDistance() * 16);
		Vec3 cameraPos = camera.getPosition();
		return new BorderContext(snapshot, bounds, renderDistance, cameraPos.x, cameraPos.y, cameraPos.z);
	}

	private static void renderForcefieldBorder(Minecraft minecraft, Camera camera, BorderContext context) {
		double minX = context.minX;
		double maxX = context.maxX;
		double minZ = context.minZ;
		double maxZ = context.maxZ;
		double cameraX = context.cameraX;
		double cameraY = context.cameraY;
		double cameraZ = context.cameraZ;
		double renderDistance = context.renderDistance;

		if (cameraX < maxX - renderDistance && cameraX > minX + renderDistance && cameraZ < maxZ - renderDistance && cameraZ > minZ + renderDistance) {
			return;
		}

		double alpha = 1.0D - getDistanceToBorder(cameraX, cameraZ, minX, maxX, minZ, maxZ) / renderDistance;
		alpha = Math.pow(alpha, 4.0D);
		alpha = Mth.clamp(alpha, 0.0D, 1.0D);
		if (alpha <= 0.0D) {
			return;
		}

		double depthFar = (double)minecraft.gameRenderer.getDepthFar();
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
		RenderSystem.depthMask(Minecraft.useShaderTransparency());

		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushPose();
		RenderSystem.applyModelViewMatrix();

		int color = BorderStatus.STATIONARY.getColor();
		float red = (float)(color >> 16 & 255) / 255.0F;
		float green = (float)(color >> 8 & 255) / 255.0F;
		float blue = (float)(color & 255) / 255.0F;
		RenderSystem.setShaderColor(red, green, blue, (float)alpha);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.polygonOffset(-3.0F, -3.0F);
		RenderSystem.enablePolygonOffset();
		RenderSystem.disableCull();

		float textureOffset = (float)(Util.getMillis() % 3000L) / 3000.0F;
		float verticalUvStart = (float)(-Mth.frac(cameraY * 0.5D));
		float verticalUvEnd = verticalUvStart + (float)depthFar;

		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		double minSegmentZ = Math.max((double)Mth.floor(cameraZ - renderDistance), minZ);
		double maxSegmentZ = Math.min((double)Mth.ceil(cameraZ + renderDistance), maxZ);
		float alternatingUvOffset = (float)(Mth.floor(minSegmentZ) & 1) * 0.5F;

		if (cameraX > maxX - renderDistance) {
			float segmentUv = alternatingUvOffset;
			for (double currentZ = minSegmentZ; currentZ < maxSegmentZ; segmentUv += 0.5F) {
				double segmentLength = Math.min(1.0D, maxSegmentZ - currentZ);
				float segmentLengthUv = (float)segmentLength * 0.5F;
				bufferbuilder.vertex(maxX - cameraX, -depthFar, currentZ - cameraZ).uv(textureOffset - segmentUv, textureOffset + verticalUvEnd).endVertex();
				bufferbuilder.vertex(maxX - cameraX, -depthFar, currentZ + segmentLength - cameraZ).uv(textureOffset - (segmentLengthUv + segmentUv), textureOffset + verticalUvEnd).endVertex();
				bufferbuilder.vertex(maxX - cameraX, depthFar, currentZ + segmentLength - cameraZ).uv(textureOffset - (segmentLengthUv + segmentUv), textureOffset + verticalUvStart).endVertex();
				bufferbuilder.vertex(maxX - cameraX, depthFar, currentZ - cameraZ).uv(textureOffset - segmentUv, textureOffset + verticalUvStart).endVertex();
				++currentZ;
			}
		}

		if (cameraX < minX + renderDistance) {
			float segmentUv = alternatingUvOffset;
			for (double currentZ = minSegmentZ; currentZ < maxSegmentZ; segmentUv += 0.5F) {
				double segmentLength = Math.min(1.0D, maxSegmentZ - currentZ);
				float segmentLengthUv = (float)segmentLength * 0.5F;
				bufferbuilder.vertex(minX - cameraX, -depthFar, currentZ - cameraZ).uv(textureOffset + segmentUv, textureOffset + verticalUvEnd).endVertex();
				bufferbuilder.vertex(minX - cameraX, -depthFar, currentZ + segmentLength - cameraZ).uv(textureOffset + segmentLengthUv + segmentUv, textureOffset + verticalUvEnd).endVertex();
				bufferbuilder.vertex(minX - cameraX, depthFar, currentZ + segmentLength - cameraZ).uv(textureOffset + segmentLengthUv + segmentUv, textureOffset + verticalUvStart).endVertex();
				bufferbuilder.vertex(minX - cameraX, depthFar, currentZ - cameraZ).uv(textureOffset + segmentUv, textureOffset + verticalUvStart).endVertex();
				++currentZ;
			}
		}

		double minSegmentX = Math.max((double)Mth.floor(cameraX - renderDistance), minX);
		double maxSegmentX = Math.min((double)Mth.ceil(cameraX + renderDistance), maxX);
		alternatingUvOffset = (float)(Mth.floor(minSegmentX) & 1) * 0.5F;

		if (cameraZ > maxZ - renderDistance) {
			float segmentUv = alternatingUvOffset;
			for (double currentX = minSegmentX; currentX < maxSegmentX; segmentUv += 0.5F) {
				double segmentLength = Math.min(1.0D, maxSegmentX - currentX);
				float segmentLengthUv = (float)segmentLength * 0.5F;
				bufferbuilder.vertex(currentX - cameraX, -depthFar, maxZ - cameraZ).uv(textureOffset + segmentUv, textureOffset + verticalUvEnd).endVertex();
				bufferbuilder.vertex(currentX + segmentLength - cameraX, -depthFar, maxZ - cameraZ).uv(textureOffset + segmentLengthUv + segmentUv, textureOffset + verticalUvEnd).endVertex();
				bufferbuilder.vertex(currentX + segmentLength - cameraX, depthFar, maxZ - cameraZ).uv(textureOffset + segmentLengthUv + segmentUv, textureOffset + verticalUvStart).endVertex();
				bufferbuilder.vertex(currentX - cameraX, depthFar, maxZ - cameraZ).uv(textureOffset + segmentUv, textureOffset + verticalUvStart).endVertex();
				++currentX;
			}
		}

		if (cameraZ < minZ + renderDistance) {
			float segmentUv = alternatingUvOffset;
			for (double currentX = minSegmentX; currentX < maxSegmentX; segmentUv += 0.5F) {
				double segmentLength = Math.min(1.0D, maxSegmentX - currentX);
				float segmentLengthUv = (float)segmentLength * 0.5F;
				bufferbuilder.vertex(currentX - cameraX, -depthFar, minZ - cameraZ).uv(textureOffset - segmentUv, textureOffset + verticalUvEnd).endVertex();
				bufferbuilder.vertex(currentX + segmentLength - cameraX, -depthFar, minZ - cameraZ).uv(textureOffset - (segmentLengthUv + segmentUv), textureOffset + verticalUvEnd).endVertex();
				bufferbuilder.vertex(currentX + segmentLength - cameraX, depthFar, minZ - cameraZ).uv(textureOffset - (segmentLengthUv + segmentUv), textureOffset + verticalUvStart).endVertex();
				bufferbuilder.vertex(currentX - cameraX, depthFar, minZ - cameraZ).uv(textureOffset - segmentUv, textureOffset + verticalUvStart).endVertex();
				++currentX;
			}
		}

		BufferUploader.drawWithShader(bufferbuilder.end());
		RenderSystem.enableCull();
		RenderSystem.polygonOffset(0.0F, 0.0F);
		RenderSystem.disablePolygonOffset();
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		modelViewStack.popPose();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
	}

	private static double getDistanceToBorder(double x, double z, double minX, double maxX, double minZ, double maxZ) {
		double toMinZ = z - minZ;
		double toMaxZ = maxZ - z;
		double toMinX = x - minX;
		double toMaxX = maxX - x;
		double minDistance = Math.min(toMinX, toMaxX);
		minDistance = Math.min(minDistance, toMinZ);
		return Math.min(minDistance, toMaxZ);
	}

	private static class BorderContext {
		public final VisibleBorderSnapshot snapshot;
		public final VisibleBorderSnapshot.DimensionBounds bounds;
		public final double renderDistance;
		public final double cameraX;
		public final double cameraY;
		public final double cameraZ;
		public final double minX;
		public final double maxX;
		public final double minZ;
		public final double maxZ;

		public BorderContext(VisibleBorderSnapshot snapshot, VisibleBorderSnapshot.DimensionBounds bounds, double renderDistance, double cameraX, double cameraY, double cameraZ) {
			this.snapshot = snapshot;
			this.bounds = bounds;
			this.renderDistance = renderDistance;
			this.cameraX = cameraX;
			this.cameraY = cameraY;
			this.cameraZ = cameraZ;
			this.minX = bounds.negativeX;
			this.maxX = bounds.positiveX;
			this.minZ = bounds.negativeZ;
			this.maxZ = bounds.positiveZ;
		}
	}
}
