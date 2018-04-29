package mekanism.client.render.transmitter;

import java.util.HashMap;

import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
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
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fluids.Fluid;

import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class RenderMechanicalPipe extends RenderTransmitterBase<TileEntityMechanicalPipe>
{
	private static HashMap<Integer, HashMap<Fluid, DisplayInteger[]>> cachedLiquids = new HashMap<>();
	
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

			int skyLight = rendererDispatcher.world.getLightFor(EnumSkyBlock.SKY, pipe.getPos()) << 4;

			buffer.setTranslation(x, y, z);

			for(EnumFacing side : EnumFacing.VALUES)
			{
				if(pipe.getConnectionType(side) == ConnectionType.NORMAL)
				{
					if(pipe.getConnectionType(side) == ConnectionType.NORMAL)
					{
						renderSide(buffer, pipe, side, fluidStack, stage, gas ? scale : 1, scale, skyLight);
					}
				}
				else if(pipe.getConnectionType(side) != ConnectionType.NONE)
				{
					buffer.setTranslation(x+0.5, y+0.5, z+0.5);

					renderFluidInOut(buffer, side, pipe);

					buffer.setTranslation(x, y, z);
				}
			}

			renderSide(buffer, pipe, null, fluidStack, stage, gas ? scale : 1, scale, skyLight);
		}
	}

	public void renderOld(TileEntityMechanicalPipe pipe, double x, double y, double z, float partialTick, int destroyStage, float alpha)
	{
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

		if(scale > 0.01 && fluid != null)
		{
			push();

			MekanismRenderer.glowOn(fluid.getLuminosity());
			MekanismRenderer.color(fluidStack != null ? fluidStack.getFluid().getColor(fluidStack) : fluid.getColor());

			bindTexture(MekanismRenderer.getBlocksTexture());
			GL11.glTranslated(x, y, z);

			boolean gas = fluid.isGaseous();

			for(EnumFacing side : EnumFacing.VALUES)
			{
				if(pipe.getConnectionType(side) == ConnectionType.NORMAL)
				{
					DisplayInteger[] displayLists = getListAndRender(side, fluid);

					if(displayLists != null)
					{
						if(!gas)
						{
							displayLists[Math.max(3, (int)((float)scale*(stages-1)))].render();
						}
						else {
							GL11.glColor4f(1F, 1F, 1F, scale);
							displayLists[stages-1].render();
						}
					}
				}
				else if(pipe.getConnectionType(side) != ConnectionType.NONE) 
				{
					GL11.glTranslated(0.5, 0.5, 0.5);
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder worldRenderer = tessellator.getBuffer();
					
					if(renderFluidInOut(worldRenderer, side, pipe))
					{
						tessellator.draw();
					}
					
					GL11.glTranslated(-0.5, -0.5, -0.5);
				}
			}

			DisplayInteger[] displayLists = getListAndRender(null, fluid);

			if(displayLists != null)
			{
				if(!gas)
				{
					displayLists[Math.max(3, (int)((float)scale*(stages-1)))].render();
				}
				else {
					GL11.glColor4f(1F, 1F, 1F, scale);
					displayLists[stages-1].render();
				}
			}

			MekanismRenderer.glowOff();
			MekanismRenderer.resetColor();

			pop();
		}
	}

	private void renderSide(BufferBuilder buffer, TileEntityMechanicalPipe pipe, EnumFacing side, FluidStack fluidStack, int stage, float alphaScale, float scale, int skyLight){
		TextureAtlasSprite sprite = MekanismRenderer.getFluidTexture(fluidStack, FluidType.STILL);
		int sideOrdinal = side != null ? side.ordinal() : 6;
		double minX, minY, minZ, maxX, maxY, maxZ;
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
		
		final int lightMapV =  ( fluidStack.getFluid().getLuminosity(fluidStack) << 4 );

		if (side != EnumFacing.UP && side != EnumFacing.DOWN)
		{
			// Top
			if (side != null || pipe.getConnectionType(EnumFacing.UP) == ConnectionType.NONE)
			{
				buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, lightMapV).endVertex();
			}

			// bottom
			if (side != null || pipe.getConnectionType(EnumFacing.DOWN) == ConnectionType.NONE)
			{
				buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, lightMapV).endVertex();
			}
		}

		//if (scale > minZ) {

		v2 -= (sprite.getMaxV() - sprite.getMinV()) * (1 - scale);

		if (side != EnumFacing.NORTH && side != EnumFacing.SOUTH)
		{
			//NORTH
			if (side != null || pipe.getConnectionType(EnumFacing.SOUTH) == ConnectionType.NONE)
			{
				buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, lightMapV).endVertex();
			}

			//SOUTH
			if (side != null || pipe.getConnectionType(EnumFacing.NORTH) == ConnectionType.NONE)
			{
				buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, lightMapV).endVertex();
			}
		}

		if (side != EnumFacing.EAST && side != EnumFacing.WEST)
		{
			//EAST
			if (side != null || pipe.getConnectionType(EnumFacing.WEST) == ConnectionType.NONE)
			{
				buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, lightMapV).endVertex();
			}

			//WEST
			if (side != null || pipe.getConnectionType(EnumFacing.EAST) == ConnectionType.NONE)
			{
				buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(skyLight, lightMapV).endVertex();
				buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(skyLight, lightMapV).endVertex();
			}
		}
	}
	
	private DisplayInteger[] getListAndRender(EnumFacing side, Fluid fluid)
	{
		if(fluid == null)
		{
			return null;
		}
		
		int sideOrdinal = side != null ? side.ordinal() : 6;

		if(cachedLiquids.containsKey(sideOrdinal) && cachedLiquids.get(sideOrdinal).containsKey(fluid))
		{
			return cachedLiquids.get(sideOrdinal).get(fluid);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.WATER;
		toReturn.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));

		if(side != null)
		{
			toReturn.setSideRender(side, false);
			toReturn.setSideRender(side.getOpposite(), false);
		}

		DisplayInteger[] displays = new DisplayInteger[stages];

		if(cachedLiquids.containsKey(sideOrdinal))
		{
			cachedLiquids.get(sideOrdinal).put(fluid, displays);
		}
		else {
			HashMap<Fluid, DisplayInteger[]> map = new HashMap<>();
			map.put(fluid, displays);
			cachedLiquids.put(sideOrdinal, map);
		}

		for(int i = 0; i < stages; i++)
		{
			displays[i] = DisplayInteger.createAndStart();

			switch(sideOrdinal)
			{
				case 6:
				{
					toReturn.minX = 0.25 + offset;
					toReturn.minY = 0.25 + offset;
					toReturn.minZ = 0.25 + offset;

					toReturn.maxX = 0.75 - offset;
					toReturn.maxY = 0.25 + offset + ((float)i / (float)stages)*height;
					toReturn.maxZ = 0.75 - offset;
					break;
				}
				case 0://down
				{
					toReturn.minX = 0.5 - (((float)i / (float)stages)*height)/2;
					toReturn.minY = 0.0;
					toReturn.minZ = 0.5 - (((float)i / (float)stages)*height)/2;

					toReturn.maxX = 0.5 + (((float)i / (float)stages)*height)/2;
					toReturn.maxY = 0.25 + offset;
					toReturn.maxZ = 0.5 + (((float)i / (float)stages)*height)/2;
					break;
				}
				case 1://up
				{
					toReturn.minX = 0.5 - (((float)i / (float)stages)*height)/2;
					toReturn.minY = 0.25 - offset + ((float)i / (float)stages)*height;
					toReturn.minZ = 0.5 - (((float)i / (float)stages)*height)/2;

					toReturn.maxX = 0.5 + (((float)i / (float)stages)*height)/2;
					toReturn.maxY = 1.0;
					toReturn.maxZ = 0.5 + (((float)i / (float)stages)*height)/2;
					break;
				}
				case 2://north
				{
					toReturn.minX = 0.25 + offset;
					toReturn.minY = 0.25 + offset;
					toReturn.minZ = 0.0;

					toReturn.maxX = 0.75 - offset;
					toReturn.maxY = 0.25 + offset + ((float)i / (float)stages)*height;
					toReturn.maxZ = 0.25 + offset;
					break;
				}
				case 3://south
				{
					toReturn.minX = 0.25 + offset;
					toReturn.minY = 0.25 + offset;
					toReturn.minZ = 0.75 - offset;

					toReturn.maxX = 0.75 - offset;
					toReturn.maxY = 0.25 + offset + ((float)i / (float)stages)*height;
					toReturn.maxZ = 1.0;
					break;
				}
				case 4://west
				{
					toReturn.minX = 0.0;
					toReturn.minY = 0.25 + offset;
					toReturn.minZ = 0.25 + offset;

					toReturn.maxX = 0.25 + offset;
					toReturn.maxY = 0.25 + offset + ((float)i / (float)stages)*height;
					toReturn.maxZ = 0.75 - offset;
					break;
				}
				case 5://east
				{
					toReturn.minX = 0.75 - offset;
					toReturn.minY = 0.25 + offset;
					toReturn.minZ = 0.25 + offset;

					toReturn.maxX = 1.0;
					toReturn.maxY = 0.25 + offset + ((float)i / (float)stages)*height;
					toReturn.maxZ = 0.75 - offset;
					break;
				}
			}

			MekanismRenderer.renderObject(toReturn);
			DisplayInteger.endList();
		}

		return displays;
	}

	public boolean renderFluidInOut(BufferBuilder renderer, EnumFacing side, TileEntityMechanicalPipe pipe)
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
			renderTransparency(renderer, tex, getModelForSide(pipe, side), c);

			return true;
		}

		return false;
	}

	@Override
	public void renderTransparency(BufferBuilder renderer, TextureAtlasSprite icon, IBakedModel cc, ColourRGBA color)
	{
		for(EnumFacing side : EnumFacing.values())
		{
			for(BakedQuad quad : cc.getQuads(null, side, 0))
			{
				quad = MekanismRenderer.iconTransform(quad, icon);
				LightUtil.renderQuadColor(renderer, quad, color.argb());
			}
		}

		for(BakedQuad quad : cc.getQuads(null, null, 0))
		{
			quad = MekanismRenderer.iconTransform(quad, icon);
			LightUtil.renderQuadColor(renderer, quad, color.argb());
		}
	}

	private void processQuad(BufferBuilder renderer, BakedQuad quad, ColourRGBA color){
		int formatSize = quad.getFormat().getElementCount();
		int[] vertextData = quad.getVertexData();
		for (int vertex = 0; vertex < 4; vertex++){
			for (int element = 0; element < formatSize; element++){
				VertexFormatElement el = quad.getFormat().getElement(element);
				switch (el.getUsage()){
					case POSITION:

				}
			}
		}
	}
	
    public static void onStitch()
    {
    	cachedLiquids.clear();
    }
}
