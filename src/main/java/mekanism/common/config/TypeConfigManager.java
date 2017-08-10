package mekanism.common.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mekanism.common.base.IBlockType;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class TypeConfigManager 
{
	private Map<String, Boolean> config = new HashMap<String, Boolean>();
	
	public boolean isEnabled(String type)
	{
		return config.get(type) != null && config.get(type);
	}
	
	public void setEntry(String type, boolean enabled)
	{
		config.put(type, enabled);
	}
	
	public static void updateConfigRecipes(List blocks, TypeConfigManager manager)
	{
		for(Object obj : blocks) //enums are quirky
		{
			IBlockType type = (IBlockType)obj;
			
			if(manager.isEnabled(type.getBlockName()))
			{
				CraftingManager.getInstance().getRecipeList().removeAll(type.getRecipes());
				CraftingManager.getInstance().getRecipeList().addAll(type.getRecipes());
			}
			else {
				CraftingManager.getInstance().getRecipeList().removeAll(type.getRecipes());
			}
		}
	}

	public static TypeConfigManager readFromBuffer(ByteBuf buf){
		int count = buf.readInt();
		TypeConfigManager ret = new TypeConfigManager();
		for (int i = 0; i < count; i++){
			ret.setEntry(ByteBufUtils.readUTF8String(buf), buf.readBoolean());
		}
		return ret;
	}

	public ByteBuf writeToBuffer(ByteBuf buf){
		buf.writeInt(this.config.size());
		for (Map.Entry<String, Boolean> e : this.config.entrySet()){
			ByteBufUtils.writeUTF8String(buf, e.getKey());
			buf.writeBoolean(e.getValue());
		}
		return buf;
	}
}
