package mekanism.client.render.transmitter;

import java.util.HashMap;

import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.ColourRGBA;
import mekanism.common.config.MekanismConfig.client;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.transmitters.grid.FluidNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.fluids.Fluid;

import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe>
{
	//private static HashMap<Integer, HashMap<Fluid, DisplayInteger[]>> cachedLiquids = new HashMap<>();
	
	private static final int stages = 100;
	private static final double height = 0.45;
	private static final double offset = 0.015;
	
	public RenderMechanicalPipe()
	{
		super();
	}

	@Override
	public final void render(TileEntityMechanicalPipe te, double x, double y, double z, float partialTicks, int destroyStage, float partial)
	{
		if(client.opaqueTransmitters)
		{
			return;
		}
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.enableCull();

		if (Minecraft.isAmbientOcclusionEnabled())
		{
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		}
		else
		{
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partial, buffer);

		/*buffer.sortVertexData((float) TileEntityRendererDispatcher.staticPlayerX,
				(float) TileEntityRendererDispatcher.staticPlayerY, (float) TileEntityRendererDispatcher.staticPlayerZ);*/

		buffer.setTranslation(0, 0, 0);
		tessellator.draw();

		RenderHelper.enableStandardItemLighting();
	}

	//NB while this is named a fast tesr, it needs culling enabled which the batching doesnt do!
	public void renderTileEntityFast(TileEntityMechanicalPipe pipe, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer){
		if(client.opaqueTransmitters)
		{
			return;
		}
		float targetScale;

		if(pipe.getTransmitter().hasTransmitterNetwork())
		{
			targetScale = pipe.getTransmitter().getTransmitterNetwork().fluidScale;
		}
		else {
			targetScale = (float)pipe.buffer.getFluidAmount() / (float)pipe.buffer.getCapacity();
		}

		if(Math.abs(pipe.currentScale - targetScale) > 0.01)
		{
			pipe.currentScale = (12 * pipe.currentScale + targetScale) / 13;
		}
		else {
			pipe.currentScale = targetScale;
		}

		Fluid fluid;
		FluidStack fluidStack;

		if(pipe.getTransmitter().hasTransmitterNetwork())
		{
			fluid = pipe.getTransmitter().getTransmitterNetwork().refFluid;
			fluidStack = pipe.getTransmitter().getTransmitterNetwork().buffer;
		}
		else {
			fluidStack = pipe.getBuffer();
			fluid = fluidStack == null ? null : pipe.getBuffer().getFluid();
		}

		float scale = Math.min(pipe.currentScale, 1);

		if(scale > 0.01 && fluidStack != null)
		{
			boolean gas = fluid.isGaseous();
			int stage = !gas ? Math.max(3, (int)((float)scale*(stages-1))) : stages-1;

			int skyLight = rendererDispatcher.world.getLightFor(EnumSkyBlock.SKY, pipe.getPos());

			buffer.setTranslation(x, y, z);
			
			int fluidLight = fluidStack.getFluid().getLuminosity(fluidStack);
			
			for(EnumFacing side : EnumFacing.VALUES)
			{
				if(pipe.getConnectionType(side) == ConnectionType.NORMAL)
				{
					if(pipe.getConnectionType(side) == ConnectionType.NORMAL)
					{
						renderSide(buffer, pipe, side, fluidStack, stage, gas ? scale : 1, scale, skyLight, fluidLight);
					}
				}
				else if(pipe.getConnectionType(side) != ConnectionType.NONE)
				{
					buffer.setTranslation(x+0.5, y+0.5, z+0.5);

					renderFluidInOut(buffer, side, pipe, skyLight, fluidLight);

					buffer.setTranslation(x, y, z);
				}
			}

			renderSide(buffer, pipe, null, fluidStack, stage, gas ? scale : 1, scale, skyLight, fluidLight);
		}
	}

	private void renderSide(BufferBuilder buffer, TileEntityMechanicalPipe pipe, EnumFacing side, FluidStack fluidStack, int stage, float alphaScale, float scale, int skyLight, int fluidLight){
		TextureAtlasSprite sprite = MekanismRenderer.getFluidTexture(fluidStack, FluidType.STILL);
		int sideOrdinal = side != null ? side.ordinal() : 6;
		double minX, minY, minZ, maxX, maxY, maxZ;
		fluidLight = fluidLight << 4;
		skyLight = skyLight << 4;
		switch(sideOrdinal)
		{
			case 6:
			{
				minX = 0.25 + offset;
				minY = 0.25 + offset;
				minZ = 0.25 + offset;

				maxX = 0.75 - offset;
				maxY = 0.25 + offset + ((float)stage / (float)stages)*height;
				maxZ = 0.75 - offset;
				break;
			}
			case 0://down
			{
				minX = 0.5 - (((float)stage / (float)stages)*height)/2;
				minY = 0.0;
				minZ = 0.5 - (((float)stage / (float)stages)*height)/2;

				maxX = 0.5 + (((float)stage / (float)stages)*height)/2;
				maxY = 0.25 + offset;
				maxZ = 0.5 + (((float)stage / (float)stages)*height)/2;
				break;
			}
			case 1://up
			{
				minX = 0.5 - (((float)stage / (float)stages)*height)/2;
				minY = 0.25 - offset + ((float)stage / (float)stages)*height;
				minZ = 0.5 - (((float)stage / (float)stages)*height)/2;

				maxX = 0.5 + (((float)stage / (float)stages)*height)/2;
				maxY = 1.0;
				maxZ = 0.5 + (((float)stage / (float)stages)*height)/2;
				break;
			}
			case 2://north
			{
				minX = 0.25 + offset;
				minY = 0.25 + offset;
				minZ = 0.0;

				maxX = 0.75 - offset;
				maxY = 0.25 + offset + ((float)stage / (float)stages)*height;
				maxZ = 0.25 + offset;
				break;
			}
			case 3://south
			{
				minX = 0.25 + offset;
				minY = 0.25 + offset;
				minZ = 0.75 - offset;

				maxX = 0.75 - offset;
				maxY = 0.25 + offset + ((float)stage / (float)stages)*height;
				maxZ = 1.0;
				break;
			}
			case 4://west
			{
				minX = 0.0;
				minY = 0.25 + offset;
				minZ = 0.25 + offset;

				maxX = 0.25 + offset;
				maxY = 0.25 + offset + ((float)stage / (float)stages)*height;
				maxZ = 0.75 - offset;
				break;
			}
			case 5://east
			{
				minX = 0.75 - offset;
				minY = 0.25 + offset;
				minZ = 0.25 + offset;

				maxX = 1.0;
				maxY = 0.25 + offset + ((float)stage / (float)stages)*height;
				maxZ = 0.75 - offset;
				break;
			}
			default:
				throw new IllegalStateException("unknown side ordinal: "+sideOrdinal);
		}
		float u1 = sprite.getMinU();
		float v1 = sprite.getMinV();
		float u2 = sprite.getMaxU();
		float v2 = sprite.getMaxV();

		int fluidColor = fluidStack.getFluid().getColor(fluidStack);

		float red = (fluidColor >> 16 & 0xFF) / 255.0F;
		float green = (fluidColor >> 8 & 0xFF) / 255.0F;
		float blue = (fluidColor & 0xFF) / 255.0F;
		float alpha = (Math.min((fluidColor >> 24 & 0xFF), 255) / 255.0F) * alphaScale;
		
		if (side != EnumFacing.UP && side != EnumFacing.DOWN)
		{
			// Top
			if (side != null || pipe.getConnectionType(EnumFacing.UP) == ConnectionType.NONE)
			{
				buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, fluidLight).endVertex();
			}

			// bottom
			if (side != null || pipe.getConnectionType(EnumFacing.DOWN) == ConnectionType.NONE)
			{
				buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, fluidLight).endVertex();
			}
		}

		//if (scale > minZ) {

		v2 -= (sprite.getMaxV() - sprite.getMinV()) * (1 - scale);

		if (side != EnumFacing.NORTH && side != EnumFacing.SOUTH)
		{
			//NORTH
			if (side != null || pipe.getConnectionType(EnumFacing.NORTH) == ConnectionType.NONE)
			{
				buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, fluidLight).endVertex();
			}

			//SOUTH
			if (side != null || pipe.getConnectionType(EnumFacing.SOUTH) == ConnectionType.NONE)
			{
				buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, fluidLight).endVertex();
			}
		}

		if (side != EnumFacing.EAST && side != EnumFacing.WEST)
		{
			//EAST
			if (side != null || pipe.getConnectionType(EnumFacing.WEST) == ConnectionType.NONE)
			{
				buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, fluidLight).endVertex();
			}

			//WEST
			if (side != null || pipe.getConnectionType(EnumFacing.EAST) == ConnectionType.NONE)
			{
				buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, fluidLight).endVertex();
				buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, fluidLight).endVertex();
			}
		}
	}

	public boolean renderFluidInOut(BufferBuilder renderer, EnumFacing side, TileEntityMechanicalPipe pipe, int skyLight, int fluidLight)
	{
		if(pipe != null && pipe.getTransmitter() != null && pipe.getTransmitter().getTransmitterNetwork() != null)
		{
			bindTexture(MekanismRenderer.getBlocksTexture());
			TextureAtlasSprite tex = MekanismRenderer.getFluidTexture(pipe.getTransmitter().getTransmitterNetwork().refFluid, FluidType.STILL);
			FluidNetwork fn = pipe.getTransmitter().getTransmitterNetwork();
			int color = fn.buffer != null ? fn.buffer.getFluid().getColor(fn.buffer) : fn.refFluid.getColor();
			ColourRGBA c = new ColourRGBA(1.0, 1.0, 1.0, pipe.currentScale);
			if (color != 0xFFFFFFFF){
				c.setRGBFromInt(color);
			}
			renderTransparency(renderer, tex, getModelForSide(pipe, side), c, skyLight, fluidLight);

			return true;
		}

		return false;
	}
	
	private void renderTransparency(BufferBuilder renderer, TextureAtlasSprite icon, IBakedModel cc, ColourRGBA color, int skyLight, int fluidLight)
	{
		//consumer code copied & adapted from net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColorSlow
		Lighter cons;
		if(renderer == Tessellator.getInstance().getBuffer())
		{
			cons = getTesselatorLighter();
		}
		else
		{
			cons = new Lighter(new VertexBufferConsumer(renderer));
		}
		int auxColor = color.argb();
		float b = (float)(auxColor & 0xFF) / 0xFF;
		float g = (float)((auxColor >>> 8) & 0xFF) / 0xFF;
		float r = (float)((auxColor >>> 16) & 0xFF) / 0xFF;
		float a = (float)((auxColor >>> 24) & 0xFF) / 0xFF;
		
		cons.setAuxColor(r, g, b, a);
		cons.setLight(skyLight, fluidLight);
		
		for(EnumFacing side : EnumFacing.values())
		{
			for(BakedQuad quad : cc.getQuads(null, side, 0))
			{
				quad = MekanismRenderer.iconTransform(quad, icon);
				quad.pipe(cons);
			}
		}

		for(BakedQuad quad : cc.getQuads(null, null, 0))
		{
			quad = MekanismRenderer.iconTransform(quad, icon);
			quad.pipe(cons);
		}
	}
	
    public static void onStitch()
    {
    	//cachedLiquids.clear();
    }
    
	private static Lighter tesselatorLighter = null;
	private static Lighter getTesselatorLighter(){
		if (tesselatorLighter == null){
			tesselatorLighter = new Lighter(LightUtil.getTessellator());
		}
		return tesselatorLighter;
	}
    
    private static class Lighter extends LightUtil.ItemConsumer  {
	
		float[] light = new float[2];
		
	    public Lighter(IVertexConsumer parent) {
		    super(parent);
	    }
	    
	    public void setLight(int skyLight, int blockLight){
	    	light[0] = blockLight / 15F;
	    	light[1] = skyLight / 15F;
	    }
	
	    @Override
	    public void put(int element, float... data) {
		    if (element != 3 || getVertexFormat().getElement(3) != DefaultVertexFormats.TEX_2S)
		    	super.put(element, data);
		    else
		        super.put(3, light);
	    }
    }
}
