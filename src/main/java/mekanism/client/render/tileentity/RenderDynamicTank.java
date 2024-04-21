package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.RenderTickHandler.VBORenderer;
import mekanism.client.render.data.RenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@NothingNullByDefault
public class RenderDynamicTank extends MultiblockTileEntityRenderer<TankMultiblockData, TileEntityDynamicTank> implements VBORenderer<TileEntityDynamicTank> {

    public RenderDynamicTank(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityDynamicTank tile, TankMultiblockData multiblock, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light,
          int overlayLight, ProfilerFiller profiler) {
        BlockPos center = multiblock.getBounds().getCenter();
        RenderTickHandler.queueVBORender(getCamera(), tile, tile.getBlockPos(), center, LevelRenderer.getLightColor(tile.getLevel(), center), overlayLight, this);
    }

    @Nullable
    private RenderData getRenderData(TankMultiblockData multiblock) {
        CurrentType currentType = multiblock.mergedTank.getCurrentType();
        if (currentType == CurrentType.EMPTY) {
            return null;
        }
        return (switch (currentType) {
            case FLUID -> RenderData.Builder.create(multiblock.getFluidTank().getFluid());
            case GAS -> RenderData.Builder.create(multiblock.getGasTank().getStack());
            case INFUSION -> RenderData.Builder.create(multiblock.getInfusionTank().getStack());
            case PIGMENT -> RenderData.Builder.create(multiblock.getPigmentTank().getStack());
            case SLURRY -> RenderData.Builder.create(multiblock.getSlurryTank().getStack());
            default -> throw new IllegalStateException("Unknown current type.");
        }).ofBordered(multiblock).build();
    }

    @Override
    public void renderVBO(Camera camera, TileEntityDynamicTank tileEntityDynamicTank, PoseStack matrix, Matrix4f projectionMatrix, int light, int overlayLight, ProfilerFiller profiler) {
        TankMultiblockData multiblock = tileEntityDynamicTank.getMultiblock();
        RenderData data = getRenderData(multiblock);
        if (data != null) {
            MekanismRenderer.renderObjectAndValvesVBO(camera, data, multiblock.valves, tileEntityDynamicTank.getBlockPos(), matrix, projectionMatrix, overlayLight, multiblock.prevScale);
        }
    }

    @Override
    public String getProfilerSection() {
        return ProfilerConstants.DYNAMIC_TANK;
    }

    @Override
    protected boolean shouldRender(TileEntityDynamicTank tile, TankMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && !multiblock.isEmpty();
    }
}