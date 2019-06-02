package mekanism.client.render.obj;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.repack.forge.OBJModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;

public class MekanismOBJModel extends OBJModel {

    private OBJModelType modelType;
    private ResourceLocation location;

    public MekanismOBJModel(OBJModelType type, MaterialLibrary matLib, ResourceLocation modelLocation) {
        super(matLib, modelLocation);
        modelType = type;
        location = modelLocation;
    }

    @Nonnull
    @Override
    public OBJBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        OBJBakedModel preBaked = super.bake(state, format, bakedTextureGetter);
        if (modelType == OBJModelType.GLOW_PANEL) {
            return new GlowPanelModel(preBaked, this, state, format, preBaked.getTextures(), null);
        } else if (modelType == OBJModelType.TRANSMITTER) {
            return new TransmitterModel(preBaked, this, state, format, preBaked.getTextures(), null);
        }

        return preBaked;
    }

    @Nonnull
    @Override
    public MekanismOBJModel process(@Nonnull ImmutableMap<String, String> customData) {
        return new MekanismOBJModel(modelType, getMatLib(), location);
    }

    @Nonnull
    @Override
    public MekanismOBJModel retexture(@Nonnull ImmutableMap<String, String> textures) {
        return new MekanismOBJModel(modelType, getMatLib().makeLibWithReplacements(textures), location);
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getTextures() {
        return super.getTextures().stream().filter(r -> !r.getPath().startsWith("#")).collect(Collectors.toList());
    }

    public enum OBJModelType {
        GLOW_PANEL,
        TRANSMITTER
    }
}