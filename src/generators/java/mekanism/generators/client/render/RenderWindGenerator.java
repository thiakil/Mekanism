package mekanism.generators.client.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.RenderTickHandler.VBORenderer;
import mekanism.client.render.tileentity.IWireFrameRenderer;
import mekanism.client.render.tileentity.ModelTileEntityRenderer;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

@NothingNullByDefault
public class RenderWindGenerator extends ModelTileEntityRenderer<TileEntityWindGenerator, ModelWindGenerator> implements IWireFrameRenderer, VBORenderer<TileEntityWindGenerator> {

    private static final Cache<LightData, WindBuffers> WIND_BUFFER = CacheBuilder.newBuilder()
          .<LightData, WindBuffers>removalListener(notification -> notification.getValue().close())
          .maximumSize(20)
          .build();

    public RenderWindGenerator(BlockEntityRendererProvider.Context context) {
        super(context, ModelWindGenerator::new);
    }

    @Override
    protected void render(TileEntityWindGenerator tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        RenderTickHandler.queueVBORender(getCamera(), tile, tile.getBlockPos(), tile.getBlockPos(), light, overlayLight, this);
    }

    @Override
    public void renderVBO(Camera camera, TileEntityWindGenerator tile, PoseStack matrix, Matrix4f projectionMatrix, int light, int overlayLight, ProfilerFiller profiler, float partialTick) {
        double angle = setupRenderer(tile, partialTick, matrix);
        RenderType renderType = model.getRenderType();

        LightData cacheKey = new LightData(light, overlayLight);
        WindBuffers windBuffers = WIND_BUFFER.getIfPresent(cacheKey);

        if (windBuffers == null) {
            VertexBuffer bladeBuffer = new VertexBuffer(Usage.STATIC);
            BufferBuilder builder = new BufferBuilder(renderType.bufferSize());
            builder.begin(renderType.mode(), renderType.format());
            PoseStack poseStack = new PoseStack();
            model.renderBladePartsToBuffer(poseStack, builder, light, overlayLight);
            bladeBuffer.bind();
            bladeBuffer.upload(builder.end());
            VertexBuffer.unbind();

            VertexBuffer staticBuffer = new VertexBuffer(Usage.STATIC);
            builder = new BufferBuilder(renderType.bufferSize());
            builder.begin(renderType.mode(), renderType.format());
            model.renderStaticPartsToBuffer(new PoseStack(), builder, light, overlayLight);
            staticBuffer.bind();
            staticBuffer.upload(builder.end());
            VertexBuffer.unbind();

            windBuffers = new WindBuffers(staticBuffer, bladeBuffer);
            WIND_BUFFER.put(cacheKey, windBuffers);
        }

        renderType.setupRenderState();
        windBuffers.staticParts.bind();
        windBuffers.staticParts.drawWithShader(matrix.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();
        windBuffers.bladeParts.bind();
        matrix.pushPose();
        matrix.rotateAround(Axis.ZP.rotationDegrees((float) angle), 0F, -3F, 0F);
        windBuffers.bladeParts.drawWithShader(matrix.last().pose(), projectionMatrix, RenderSystem.getShader());
        matrix.popPose();
        VertexBuffer.unbind();
        renderType.clearRenderState();
        matrix.popPose();//from setupRenderer
    }

    @Override
    public String getProfilerSection() {
        return GeneratorsProfilerConstants.WIND_GENERATOR;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityWindGenerator tile) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityWindGenerator tile) {
        //Note: we just extend it to the max size (including blades) it could be ignoring what direction it is actually facing
        BlockPos pos = tile.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-2, 0, -2), pos.offset(2, 6, 2));
    }

    @Override
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer) {
        if (tile instanceof TileEntityWindGenerator windGenerator) {
            double angle = setupRenderer(windGenerator, partialTick, matrix);
            model.renderWireFrame(matrix, buffer, angle);
            matrix.popPose();
        }
    }

    private double setupRenderer(TileEntityWindGenerator tile, float partialTick, PoseStack matrix) {
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.mulPose(Axis.ZP.rotationDegrees(180));
        double angle = tile.getAngle();
        if (tile.getActive() && partialTick > 0) {
            angle = (angle + tile.getHeightSpeedRatio() * partialTick) % 360;
        }
        return angle;
    }

    private record LightData(int light, int overlay) {}
    private record WindBuffers(VertexBuffer staticParts, VertexBuffer bladeParts) {

        void close() {
            staticParts.close();
            bladeParts.close();
        }
    }
}