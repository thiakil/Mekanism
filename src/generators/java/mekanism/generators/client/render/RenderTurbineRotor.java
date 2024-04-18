package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.tileentity.ModelTileEntityRenderer;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderTurbineRotor extends ModelTileEntityRenderer<TileEntityTurbineRotor, ModelTurbine> {

    @Nullable
    public static RenderTurbineRotor INSTANCE;

    public RenderTurbineRotor(BlockEntityRendererProvider.Context context) {
        super(context, ModelTurbine::new);
        INSTANCE = this;
    }

    public VertexConsumer getBuffer(@NotNull MultiBufferSource renderer) {
        return model.getBuffer(renderer);
    }

    public RenderType getRenderType() {
        return model.getRenderType();
    }

    @Override
    protected void render(TileEntityTurbineRotor tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        VertexConsumer buffer = getBuffer(renderer);
        int baseIndex = tile.getPosition() * 2;
        int housedBlades = tile.getHousedBlades();
        if (housedBlades == 0) {
            return;
        }

        matrix.translate(0.5, 0, 0.5);

        //Bottom blade
        renderSingleBlade(matrix, buffer, light, overlayLight, baseIndex, true);
        //Top blade
        if (housedBlades == 2) {
            renderSingleBlade(matrix, buffer, light, overlayLight, baseIndex + 1, false);
        }
    }

    public void renderSingleBlade(PoseStack matrix, VertexConsumer buffer, int light, int overlayLight, int index, boolean isLower) {
        matrix.pushPose();
        if (isLower) {
            matrix.translate(0, -1, 0);
        } else {
            matrix.translate(0, -0.5, 0);
        }
        model.render(matrix, buffer, light, overlayLight, index);
        matrix.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.TURBINE_ROTOR;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityTurbineRotor tile) {
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityTurbineRotor tile, Vec3 camera) {
        return tile.getMultiblockUUID() == null && tile.getHousedBlades() > 0 && super.shouldRender(tile, camera);
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityTurbineRotor tile) {
        int radius = tile.getRadius();
        if (tile.blades == 0 || radius == -1) {
            //If there are no blades default to the collision box of the rotor
            return super.getRenderBoundingBox(tile);
        }
        BlockPos pos = tile.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-radius, 0, -radius), pos.offset(radius, 0, radius));
    }
}