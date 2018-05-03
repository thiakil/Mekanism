package mekanism.client.render;

import mekanism.common.ColourRGBA;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

public class TransformedQuadBuilder implements IVertexConsumer
{
	private final VertexFormat format;
	private final float[][][] unpackedData;
	private int tint = -1;
	private EnumFacing orientation;
	private TextureAtlasSprite texture;
	private boolean applyDiffuseLighting = true;
	
	private int vertices = 0;
	private int elements = 0;
	private boolean full = false;
	private boolean contractUVs = false;
	
	public TransformedQuadBuilder(VertexFormat format)
	{
		this.format = format;
		unpackedData = new float[4][format.getElementCount()][4];
	}
	
	@Override
	public VertexFormat getVertexFormat()
	{
		return format;
	}
	
	public void setContractUVs(boolean value)
	{
		this.contractUVs = value;
	}
	@Override
	public void setQuadTint(int tint)
	{
		this.tint = tint;
	}
	
	@Override
	public void setQuadOrientation(EnumFacing orientation)
	{
		this.orientation = orientation;
	}
	
	@Override
	public void setTexture(TextureAtlasSprite texture)
	{
		this.texture = texture;
	}
	
	@Override
	public void setApplyDiffuseLighting(boolean diffuse)
	{
		this.applyDiffuseLighting = diffuse;
	}
	
	@Override
	public void put(int element, float... data)
	{
		for(int i = 0; i < 4; i++)
		{
			if(i < data.length)
			{
				unpackedData[vertices][element][i] = data[i];
			}
			else
			{
				unpackedData[vertices][element][i] = 0;
			}
		}
		elements++;
		if(elements == format.getElementCount())
		{
			vertices++;
			elements = 0;
		}
		if(vertices == 4)
		{
			full = true;
		}
	}
	
	private final float eps = 1f / 0x100;
	
	public UnpackedBakedQuad build()
	{
		if(!full)
		{
			throw new IllegalStateException("not enough data");
		}
		if(texture == null)
		{
			throw new IllegalStateException("texture not set");
		}
		if(contractUVs)
		{
			float tX = texture.getIconWidth() / (texture.getMaxU() - texture.getMinU());
			float tY = texture.getIconHeight() / (texture.getMaxV() - texture.getMinV());
			float tS = tX > tY ? tX : tY;
			float ep = 1f / (tS * 0x100);
			int uve = 0;
			while(uve < format.getElementCount())
			{
				VertexFormatElement e = format.getElement(uve);
				if(e.getUsage() == VertexFormatElement.EnumUsage.UV && e.getIndex() == 0)
				{
					break;
				}
				uve++;
			}
			if(uve == format.getElementCount())
			{
				throw new IllegalStateException("Can't contract UVs: format doesn't contain UVs");
			}
			float[] uvc = new float[4];
			for(int v = 0; v < 4; v++)
			{
				for(int i = 0; i < 4; i++)
				{
					uvc[i] += unpackedData[v][uve][i] / 4;
				}
			}
			for(int v = 0; v < 4; v++)
			{
				for (int i = 0; i < 4; i++)
				{
					float uo = unpackedData[v][uve][i];
					float un = uo * (1 - eps) + uvc[i] * eps;
					float ud = uo - un;
					float aud = ud;
					if(aud < 0) aud = -aud;
					if(aud < ep) // not moving a fraction of a pixel
					{
						float udc = uo - uvc[i];
						if(udc < 0) udc = -udc;
						if(udc < 2 * ep) // center is closer than 2 fractions of a pixel, don't move too close
						{
							un = (uo + uvc[i]) / 2;
						}
						else // move at least by a fraction
						{
							un = uo + (ud < 0 ? ep : -ep);
						}
					}
					unpackedData[v][uve][i] = un;
				}
			}
		}
		return new UnpackedBakedQuad(unpackedData, tint, orientation, texture, applyDiffuseLighting, format);
	}
	
	public TransformedQuadBuilder setColor(ColourRGBA color){
		int colorIndex = format.getElements().indexOf(DefaultVertexFormats.COLOR_4UB);
		if (colorIndex == -1){
			return this;
		}
		float a = color.valA/255F;
		float r = color.valR/255F;
		float g = color.valG/255F;
		float b = color.valB/255F;
		
		for (int vertex = 0; vertex < 4; vertex++){
			unpackedData[vertex][colorIndex][0] *= r;
			unpackedData[vertex][colorIndex][1] *= g;
			unpackedData[vertex][colorIndex][2] *= b;
			unpackedData[vertex][colorIndex][3] *= a;
		}
		return this;
	}
	
	public TransformedQuadBuilder setLight(int skylightIn, int blockLightIn){
		int lightMapE = format.getElements().indexOf(DefaultVertexFormats.TEX_2S);
		if (lightMapE == -1){
			return this;
		}
		float skyLight = skylightIn / 15F;
		float blockLight = blockLightIn / 15F;
		for (int vertex = 0; vertex < 4; vertex++) {
			unpackedData[vertex][lightMapE][0] = blockLight;
			unpackedData[vertex][lightMapE][1] = skyLight;
		}
		return this;
	}
	
	public TransformedQuadBuilder retexture(TextureAtlasSprite newSprite){
		if (!format.hasUvOffset(0)){
			return this;
		}
		int uvEl = format.getElements().indexOf(DefaultVertexFormats.TEX_2F);
		for (int vertex = 0; vertex < 4; vertex++) {
			unpackedData[vertex][uvEl][0] = newSprite.getInterpolatedU(texture.getUnInterpolatedU(unpackedData[vertex][uvEl][0]));
			unpackedData[vertex][uvEl][1] = newSprite.getInterpolatedV(texture.getUnInterpolatedV(unpackedData[vertex][uvEl][1]));
		}
		texture = newSprite;
		return this;
	}
	
	public void pipe(IVertexConsumer consumer)
	{
		int[] eMap = LightUtil.mapFormats(consumer.getVertexFormat(), format);
		
		if(tint != -1)
		{
			consumer.setQuadTint(tint);
		}
		consumer.setTexture(texture);
		consumer.setApplyDiffuseLighting(applyDiffuseLighting);
		consumer.setQuadOrientation(orientation);
		for(int v = 0; v < 4; v++)
		{
			for(int e = 0; e < consumer.getVertexFormat().getElementCount(); e++)
			{
				if(eMap[e] != format.getElementCount())
				{
					consumer.put(e, unpackedData[v][eMap[e]]);
				}
				else
				{
					consumer.put(e);
				}
			}
		}
	}
}
