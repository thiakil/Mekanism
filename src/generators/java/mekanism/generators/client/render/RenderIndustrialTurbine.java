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
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.data.ChemicalRenderData.GasRenderData;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderIndustrialTurbine extends MultiblockTileEntityRenderer<TurbineMultiblockData, TileEntityTurbineCasing> {

    private static final Cache<TurbineKey, VertexBuffer> ROTOR_BUFFERS = CacheBuilder.newBuilder()
          .<TurbineKey, VertexBuffer>removalListener(notification -> notification.getValue().close())
          .maximumSize(10)
          .build();
    private static final Cache<GasRenderData, VertexBuffer> STEAM_BUFFER = CacheBuilder.newBuilder()
          .<GasRenderData, VertexBuffer>removalListener(notification -> notification.getValue().close())
          .maximumSize(5)
          .build();
    private static final float BASE_SPEED = 180F;

    public RenderIndustrialTurbine(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityTurbineCasing tile, TurbineMultiblockData multiblock, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light,
          int overlayLight, ProfilerFiller profiler) {
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
            rotorBuffer.drawWithShader(matrix.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
            VertexBuffer.unbind();
            renderType.clearRenderState();

            matrix.popPose();
        }
        profiler.popPush("Turbine steam");
        if (!multiblock.gasTank.isEmpty() && multiblock.length() > 0) {
            int height = multiblock.lowerVolume / (multiblock.length() * multiblock.width());
            if (height > 0) {
                GasRenderData gasRenderData = new GasRenderData(multiblock.renderLocation, multiblock.width() - 2, height, multiblock.length() - 2, multiblock.gasTank.getStack().getType());

                VertexBuffer gasBuffer = STEAM_BUFFER.getIfPresent(gasRenderData);
                RenderType renderType = MekanismRenderType.translucentDepthBlocks();

                if (gasBuffer == null) {
                    gasBuffer = new VertexBuffer(Usage.STATIC);
                    BufferBuilder builder = new BufferBuilder(renderType.bufferSize());
                    builder.begin(renderType.mode(), renderType.format());
                    RenderResizableCuboid.renderCube(ModelRenderer.getModel(gasRenderData, 1F), new PoseStack(), builder, gasRenderData.getColorARGB(1F), gasRenderData.calculateGlowLight(LightTexture.FULL_SKY), overlayLight, FaceDisplay.FRONT, getCamera(), null);
                    gasBuffer.bind();
                    gasBuffer.upload(builder.end());
                    STEAM_BUFFER.put(gasRenderData, gasBuffer);
                }
                renderType.setupRenderState();
                RenderSystem.setShaderColor(1, 1, 1, multiblock.prevSteamScale);
                gasBuffer.bind();
                matrix.pushPose();
                matrix.translate(gasRenderData.location.getX() - pos.getX() + 1, gasRenderData.location.getY() - pos.getY(), gasRenderData.location.getZ() - pos.getZ() + 1);
                gasBuffer.drawWithShader(matrix.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                matrix.popPose();
                VertexBuffer.unbind();
                renderType.clearRenderState();
                RenderSystem.setShaderColor(1, 1, 1, 1);

            }
        }
        profiler.pop();
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.INDUSTRIAL_TURBINE;
    }

    @Override
    protected boolean shouldRender(TileEntityTurbineCasing tile, TurbineMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && multiblock.complex != null;
    }

    private record TurbineKey(int blades, int lightLevel) {}
}