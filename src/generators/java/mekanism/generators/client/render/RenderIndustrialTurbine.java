package mekanism.generators.client.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.math.Axis;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.RenderTickHandler.VBORenderer;
import mekanism.client.render.data.ChemicalRenderData.GasRenderData;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@NothingNullByDefault
public class RenderIndustrialTurbine extends MultiblockTileEntityRenderer<TurbineMultiblockData, TileEntityTurbineCasing> implements VBORenderer<TileEntityTurbineCasing> {

    private static final Cache<TurbineKey, VertexBuffer> ROTOR_BUFFERS = CacheBuilder.newBuilder()
          .<TurbineKey, VertexBuffer>removalListener(notification -> notification.getValue().close())
          .maximumSize(10)
          .build();
    private static final float BASE_SPEED = 180F;

    public RenderIndustrialTurbine(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityTurbineCasing tile, TurbineMultiblockData multiblock, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light,
          int overlayLight, ProfilerFiller profiler) {
        if (multiblock.renderLocation == null) {
            return;
        }
        RenderTickHandler.queueVBORender(getCamera(), tile, tile.getBlockPos(), multiblock.getBounds().getCenter(), light, overlayLight, this);
    }

    @Override
    public String getProfilerSection() {
        return GeneratorsProfilerConstants.INDUSTRIAL_TURBINE;
    }

    @Override
    protected boolean shouldRender(TileEntityTurbineCasing tile, TurbineMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && multiblock.complex != null;
    }

    @Override
    public void renderVBO(TileEntityTurbineCasing tile, PoseStack matrix, Matrix4f projectionMatrix, int light, int overlayLight, ProfilerFiller profiler) {
        TurbineMultiblockData multiblock = tile.getMultiblock();
        if (multiblock.renderLocation == null) {
            return;
        }
        BlockPos pos = tile.getBlockPos();
        profiler.push(GeneratorsProfilerConstants.TURBINE_ROTOR);
        if (RenderTurbineRotor.INSTANCE != null) {
            BlockPos complexPos = multiblock.complex;
            matrix.pushPose();

            if (!Minecraft.getInstance().isPaused()) {
                float rotateSpeed = Math.min(multiblock.clientRotation * BASE_SPEED, 20F);
                multiblock.clientCurrentRotation = (multiblock.clientCurrentRotation + rotateSpeed) % 360;
            }

            matrix.translate(complexPos.getX() - pos.getX(), complexPos.getY() - pos.getY(), complexPos.getZ() - pos.getZ());
            matrix.translate(0.5, 0, 0.5);

            matrix.mulPose(Axis.YP.rotationDegrees(multiblock.clientCurrentRotation));

            RenderType renderType = RenderTurbineRotor.INSTANCE.getRenderType();

            TurbineKey turbineKey = new TurbineKey(multiblock.blades, light);
            VertexBuffer rotorBuffer = ROTOR_BUFFERS.getIfPresent(turbineKey);

            if (rotorBuffer == null) {
                rotorBuffer = new VertexBuffer(Usage.STATIC);
                BufferBuilder builder = new BufferBuilder(renderType.bufferSize());
                builder.begin(renderType.mode(), renderType.format());
                final PoseStack poseStack = new PoseStack();
                for (int i = 0; i < multiblock.blades; i++) {
                    poseStack.translate(0, -0.5, 0);
                    RenderTurbineRotor.INSTANCE.renderSingleBlade(poseStack, builder, light, overlayLight, multiblock.blades - i, true);
                }
                rotorBuffer.bind();
                rotorBuffer.upload(builder.end());
                ROTOR_BUFFERS.put(turbineKey, rotorBuffer);
            }

            renderType.setupRenderState();
            rotorBuffer.bind();
            rotorBuffer.drawWithShader(matrix.last().pose(), projectionMatrix, RenderSystem.getShader());
            VertexBuffer.unbind();
            renderType.clearRenderState();

            matrix.popPose();
        }
        profiler.popPush("Turbine steam");
        if (!multiblock.gasTank.isEmpty() && multiblock.length() > 0) {
            int height = multiblock.lowerVolume / (multiblock.length() * multiblock.width());
            if (height > 0) {
                GasRenderData gasRenderData = new GasRenderData(multiblock.renderLocation.offset(1, 0, 1), multiblock.width() - 2, height, multiblock.length() - 2, multiblock.gasTank.getStack().getType());
                MekanismRenderer.renderObjectVBO(gasRenderData, pos, matrix, projectionMatrix, overlayLight, multiblock.prevSteamScale);

            }
        }
        profiler.pop();
    }

    private record TurbineKey(int blades, int lightLevel) {}
}