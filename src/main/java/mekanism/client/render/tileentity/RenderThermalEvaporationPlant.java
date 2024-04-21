package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.RenderTickHandler.VBORenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@NothingNullByDefault
public class RenderThermalEvaporationPlant extends MultiblockTileEntityRenderer<EvaporationMultiblockData, TileEntityThermalEvaporationController> implements VBORenderer<TileEntityThermalEvaporationController> {

    public RenderThermalEvaporationPlant(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityThermalEvaporationController tile, EvaporationMultiblockData multiblock, float partialTick, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, ProfilerFiller profiler) {
        RenderTickHandler.queueVBORender(getCamera(), tile, tile.getBlockPos(), multiblock.getBounds().getCenter(), light, overlayLight, this);
    }

    @Override
    public String getProfilerSection() {
        return ProfilerConstants.THERMAL_EVAPORATION_CONTROLLER;
    }

    @Override
    protected boolean shouldRender(TileEntityThermalEvaporationController tile, EvaporationMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && !multiblock.inputTank.isEmpty() && multiblock.renderLocation != null;
    }

    @Override
    public void renderVBO(TileEntityThermalEvaporationController tile, PoseStack matrix, Matrix4f projectionMatrix, int light, int overlayLight, ProfilerFiller profiler) {
        EvaporationMultiblockData multiblock = tile.getMultiblock();
        FluidRenderData data = RenderData.Builder.create(multiblock.inputTank.getFluid())
              .location(multiblock.renderLocation.offset(1, 0, 1))
              .dimensions(2, multiblock.height() - 1, 2)
              .build();
        MekanismRenderer.renderObjectAndValvesVBO(data, multiblock.valves, tile.getBlockPos(), matrix, projectionMatrix, overlayLight, Math.min(1, multiblock.prevScale));
    }
}