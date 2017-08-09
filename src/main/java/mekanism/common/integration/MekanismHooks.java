package mekanism.common.integration;

import mekanism.common.Mekanism;
import mekanism.common.integration.computer.CCPeripheral;
import mekanism.common.integration.computer.OCDriver;
import mekanism.common.integration.storagedrawer.StorageDrawerRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 * @author AidanBrady
 *
 */
public enum MekanismHooks
{
	COFH_CORE("cofhcore"),
	IC2("ic2", mekanism.common.integration.ic2.HookModule.class),
	RAILCRAFT("railcraft"),
	THERMALEXPANSION("thermalexpansion"),
	COMPUTERCRAFT("computercraft", CCPeripheral.CCPeripheralProvider.class),
	APPLIED_ENERGISTICS_2("appliedenergistics2", AE2Module.class),
	TESLA("tesla"),
	MCMULTIPART("mcmultipart"),
	METALLURGY_3_CORE("Metallurgy3Core"),
	METALLURGY_3_BASE("Metallurgy3Base"),
	OPENCOMPUTERS("opencomputers", OCDriver.class),
	GALACTICRAFT("Galacticraft API"),
	WAILA("waila"),
	BUILDCRAFT("buildCraft"),
	STORAGE_DRAWERS("storagedrawers", StorageDrawerRecipeHandler.class),
	;

	/* Annotations don't like Enums */
	public static final String IC2_MOD_ID = "ic2";
	public static final String COMPUTERCRAFT_MOD_ID = "computercraft";
	public static final String TESLA_MOD_ID = "tesla";
	public static final String GALACTICRAFT_MOD_ID = "Galacticraft API";
	public static final String WAILA_MOD_ID = "waila";
	public static final String BUILDCRAFT_MOD_ID = "buildcraft";


	private final Class<? extends IMekanismHook> integrationComponentClazz;
	private IMekanismHook integrationComponent;
	public final String MOD_ID;
	public boolean isLoaded;

	MekanismHooks(String modID){
		this(modID, null);
	}

	MekanismHooks(String modID, Class<? extends IMekanismHook> clazz){
		this.MOD_ID = modID;
		this.integrationComponentClazz = clazz;
	}

	/**
	 * Checks if mod is loaded, and creates the module if it is.
	 */
	public void create(){
		this.isLoaded = Loader.isModLoaded(this.MOD_ID);
		if (this.isLoaded && integrationComponentClazz != null){
			try {
				this.integrationComponent = this.integrationComponentClazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				Mekanism.logger.error("Integration module for "+this.MOD_ID+" threw an exception", e);
				integrationComponent = null;
			}
		} else {
			integrationComponent = null;
		}
	}

	public IMekanismHook getComponent(){
		return integrationComponent;
	}

	public Class<? extends IMekanismHook> getComponentClass(){
		return integrationComponentClazz;
	}

	public static void initialise(){
		for (MekanismHooks hook : MekanismHooks.values()){
			hook.create();
			if (hook.getComponent() != null) {
				try {
					hook.getComponent().init();
				} catch (Exception e){
					Mekanism.logger.error("Integration component "+hook.MOD_ID+" threw exception during Init.");
				}
			}
		}
	}

	public static void postInitialise(){
		for (MekanismHooks hook : MekanismHooks.values()){
			if (hook.getComponent() != null) {
				try {
					hook.getComponent().postInit();
				} catch (Exception e){
					Mekanism.logger.error("Integration component "+hook.MOD_ID+" threw exception during PostInit.");
				}
			}
		}
	}

	public void addPulverizerRecipe(ItemStack input, ItemStack output, int energy)
	{
		NBTTagCompound nbtTags = new NBTTagCompound();

		nbtTags.setInteger("energy", energy);
		nbtTags.setTag("input", input.writeToNBT(new NBTTagCompound()));
		nbtTags.setTag("primaryOutput", output.writeToNBT(new NBTTagCompound()));

		FMLInterModComms.sendMessage("mekanism", "PulverizerRecipe", nbtTags);
	}

	/*public void loadMetallurgy()
 	{
		try
		{
			String[] setNames = {"base", "precious", "nether", "fantasy", "ender", "utility"};

			for (String setName : setNames)
			{
				for (IOreInfo oreInfo : MetallurgyAPI.getMetalSet(setName).getOreList().values())
				{
					switch (oreInfo.getType())
					{
						case ALLOY:
							if (oreInfo.getIngot() != null && oreInfo.getDust() != null)
							{
								RecipeHandler.addCrusherRecipe(MekanismUtils.size(oreInfo.getIngot(), 1), MekanismUtils.size(oreInfo.getDust(), 1));
							}

							break;
						case DROP:
							ItemStack ore = oreInfo.getOre();
							ItemStack drop = oreInfo.getDrop();

							if (drop != null && ore != null)
							{
								RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), MekanismUtils.size(drop, 12));
							}

							break;
						default:
							ItemStack ore = oreInfo.getOre();
							ItemStack dust = oreInfo.getDust();
							ItemStack ingot = oreInfo.getIngot();

							if (ore != null && dust != null)
							{
								RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), MekanismUtils.size(dust, 2));
								RecipeHandler.addCombinerRecipe(MekanismUtils.size(dust, 8), MekanismUtils.size(ore, 1));
							}

							if (ingot != null && dust != null)
							{
								RecipeHandler.addCrusherRecipe(MekanismUtils.size(ingot, 1), MekanismUtils.size(dust, 1));
							}

							break;
					}
				}
			}
		}
		catch (Exception e)
		{
		}
	}*/
}
