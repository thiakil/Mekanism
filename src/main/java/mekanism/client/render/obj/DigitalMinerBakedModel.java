package mekanism.client.render.obj;

import com.google.common.collect.ImmutableMap;
import mekanism.common.block.states.BlockStateMachine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.ExtendedBlockState;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

/**
 * Created by Xander V on 24/09/2017.
 */
public class DigitalMinerBakedModel extends OBJGlowableModel.OBJBakedModel
{
	private List<BakedQuad> quadsActive = null;
	private List<BakedQuad> quadsbase = null;

	private List<String> baseGroups;
	private List<String> activeGroups = Arrays.asList("monitor3_glow", "monitor2_glow", "monitor1_glow");

	public DigitalMinerBakedModel(OBJGlowableModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures)
	{
		super(model, state, format, textures);
		this.baseGroups = new ArrayList<>();
		for (String g : model.getMatLib().getGroups().keySet()){
			if (!activeGroups.contains(g)){
				baseGroups.add(g);
			}
		}
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState blockState, EnumFacing side, long rand)
	{
		if (quadsActive == null || quadsbase == null){
			this.quadsbase = this.buildQuads(new OBJGlowableModel.OBJState(baseGroups, true, this.state));
			this.quadsActive = new ArrayList<>(this.quadsbase);
			this.quadsActive.addAll(this.buildQuads(new OBJGlowableModel.OBJState(activeGroups, true, this.state)));
		}
		if (blockState != null){
			if (blockState.getValue(BlockStateMachine.activeProperty)){
				return quadsActive;
			}
		}
		return quadsbase;
	}
}
