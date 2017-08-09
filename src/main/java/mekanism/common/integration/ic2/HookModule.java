package mekanism.common.integration.ic2;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.Recipes;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.Resource;
import mekanism.common.integration.IMekanismHook;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.util.MekanismUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.List;

/**
 * Created by Xander.V on 9/08/2017.
 */
public class HookModule implements IMekanismHook {
    @Override
    public void postInit() {
        hookIC2Recipes();
        Mekanism.logger.info("Hooked into IC2 successfully.");
    }

    public void hookIC2Recipes()
    {
        for(MachineRecipe<IRecipeInput, Collection<ItemStack>> entry : Recipes.macerator.getRecipes())
        {
            if(!entry.getInput().getInputs().isEmpty())
            {
                List<String> names = MekanismUtils.getOreDictName(entry.getInput().getInputs().get(0));

                for(String name : names)
                {
                    boolean did = false;

                    if(name.startsWith("ingot"))
                    {
                        RecipeHandler.addCrusherRecipe(entry.getInput().getInputs().get(0), entry.getOutput().iterator().next());
                        did = true;
                    }

                    if(did)
                    {
                        break;
                    }
                }
            }
        }

        try {
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("oreOsmium"), null, false, new ItemStack(MekanismItems.Dust, 2, Resource.OSMIUM.ordinal()));
        } catch(Exception e) {}

        try {
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotOsmium"), null, false, new ItemStack(MekanismItems.Dust, 1, Resource.OSMIUM.ordinal()));
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotRefinedObsidian"), null, false, new ItemStack(MekanismItems.OtherDust, 1, 5));
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotRefinedGlowstone"), null, false, new ItemStack(Items.GLOWSTONE_DUST));
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotSteel"), null, false, new ItemStack(MekanismItems.OtherDust, 1, 1));
        } catch(Exception e) {}

        try {
            for(Resource resource : Resource.values())
            {
                Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("clump" + resource.getName()), null, false, new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
            }
        } catch(Exception e) {}
    }
}
