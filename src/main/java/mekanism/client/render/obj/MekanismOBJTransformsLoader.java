package mekanism.client.render.obj;

import mekanism.client.ClientProxy;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom OBJ loader that loads a minecraft json from the standard location, for the camera transforms,
 * and loads them into a normal OBJ model (located in models/obj/) from Forge's loader.
 *
 * Items MUST be manually registered to the loader with {@link MekanismOBJTransformsLoader#INSTANCE#registerOBJWithTransforms(ResourceLocation)}.
 *
 * @author Thiakil
 */
@SideOnly(Side.CLIENT)
public class MekanismOBJTransformsLoader implements ICustomModelLoader
{
	public static MekanismOBJTransformsLoader INSTANCE = new MekanismOBJTransformsLoader();

	private IResourceManager resourceManager;

	private List<ResourceLocation> knownOBJJsons = new ArrayList<>();

	private Class vanillaModelWrapper;
	private Field vanillaModelField;
	private ICustomModelLoader vanillaLoader;

	private MekanismOBJTransformsLoader(){
		try
		{
			vanillaModelWrapper = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
			vanillaModelField = ReflectionHelper.findField(vanillaModelWrapper, "model");
			vanillaLoader = (ICustomModelLoader)ReflectionHelper.findField(Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaLoader"), "INSTANCE").get(null);
		} catch (ClassNotFoundException e){
			Mekanism.logger.error("[MekanismOBJTransformsLoader] Did not find VanillaModelWrapper", e);
		} catch (Exception e){
			Mekanism.logger.error("[MekanismOBJTransformsLoader] Didn't find method/field", e);
		}
	}

	public void registerOBJWithTransforms(ResourceLocation loc){
		knownOBJJsons.add(loc);
		for (String ren : ClientProxy.CUSTOM_RENDERS){
			if (ren.equals(loc.getResourcePath())){
				throw new RuntimeException("Known object is in CUSTOM_RENDERS, did you forget to remove it?");
			}
		}
	}

	@Override
	public boolean accepts(@Nonnull ResourceLocation modelLocation)
	{
		if (modelLocation instanceof ModelResourceLocation){//let variants handle their thing
			return false;
		}
		ResourceLocation baseLoc = new ResourceLocation(modelLocation.getResourceDomain(), modelLocation.getResourcePath().replaceAll("models/(item|block)/", ""));
		return knownOBJJsons.contains(baseLoc);
	}

	@Override
	public IModel loadModel(@Nonnull ResourceLocation modelLocation) throws Exception
	{
		Mekanism.logger.info("Attempting to load {}, {}, {}", modelLocation, getOBJLocation(modelLocation), getJSONLocation(modelLocation));
		OBJModel objModel;
		try
		{
			objModel = (OBJModel) OBJLoader.INSTANCE.loadModel(getOBJLocation(modelLocation));
		} catch (Exception e){
			Mekanism.logger.error("Could not load OBJ", e);
			throw new RuntimeException(e);
		}

		return new MekanismOBJModelWithTransforms(objModel.getMatLib(), modelLocation,loadJSON(modelLocation));
	}

	private @Nonnull ItemCameraTransforms loadJSON(ResourceLocation modelLocation)
	{
		IModel transformsModel;
		ItemCameraTransforms transforms = ItemCameraTransforms.DEFAULT;
		try
		{
			transformsModel = vanillaLoader.loadModel(getJSONLocation(modelLocation));
			if(vanillaModelWrapper.isInstance(transformsModel))
			{
				transformsModel.getTextures();//force it to load parents
				ModelBlock baseModel = (ModelBlock) vanillaModelField.get(transformsModel);
				transforms = baseModel.getAllTransforms();
			}
		} catch (Exception e){
			Mekanism.logger.error("Could not load JSON", e);
			//throw new RuntimeException(e);
		}
		return transforms;
	}

	private static ResourceLocation getOBJLocation(ResourceLocation loc){
		String resPath = loc.getResourcePath();
		if (resPath.contains("models/")){
			resPath = resPath.replaceFirst("models/(item|block)/", "models/obj/");
		}
		else
		{
			resPath = "models/obj/"+resPath;
		}
		return new ResourceLocation(loc.getResourceDomain(), resPath+".obj");
	}

	private static ResourceLocation getJSONLocation(ResourceLocation loc){
		String resPath = loc.getResourcePath();
		if (!resPath.contains("models/")){
			if (loc instanceof ModelResourceLocation && ((ModelResourceLocation)loc).getVariant().equals("inventory")){
				resPath = "models/item/"+resPath;
			}
			else
			{
				resPath = "models/block/" + resPath;
			}
		}
		return new ResourceLocation(loc.getResourceDomain(), resPath);
	}

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
		this.resourceManager = resourceManager;
	}
}
