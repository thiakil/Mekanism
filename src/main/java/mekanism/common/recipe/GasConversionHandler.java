package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismFluids;
import mekanism.common.recipe.ingredients.IMekanismIngredient;
import mekanism.common.recipe.ingredients.ItemStackMekIngredient;
import mekanism.common.recipe.ingredients.TagMekIngredient;
import mekanism.common.tier.GasTankTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class GasConversionHandler {

    //TODO: Show uses in JEI for fuels that can be turned to gas??
    private final static Map<Gas, List<IMekanismIngredient<ItemStack>>> gasToIngredients = new HashMap<>();
    private final static Map<IMekanismIngredient<ItemStack>, GasStack> ingredientToGas = new HashMap<>();

    public static void addDefaultGasMappings() {
        ItemTags.Wrapper sulfur = new ItemTags.Wrapper(new ResourceLocation("forge", "dusts/sulfur"));
        ItemTags.Wrapper salt = new ItemTags.Wrapper(new ResourceLocation("forge", "dusts/salt"));
        ItemTags.Wrapper osmiumIngot = new ItemTags.Wrapper(new ResourceLocation("forge", "ingots/osmium"));
        ItemTags.Wrapper osmiumBlock = new ItemTags.Wrapper(new ResourceLocation("forge", "storage_blocks/osmium"));
        addGasMapping(new ItemStack(Items.FLINT), MekanismFluids.Oxygen, 10);
        addGasMapping(sulfur, MekanismFluids.SulfuricAcid, 2);
        addGasMapping(salt, MekanismFluids.HydrogenChloride, 2);
        addGasMapping(osmiumIngot, MekanismFluids.LiquidOsmium, 200);
        addGasMapping(osmiumBlock, MekanismFluids.LiquidOsmium, 1_800);
    }

    public static boolean addGasMapping(@Nonnull ItemStack stack, @Nonnull Gas gas, int amount) {
        return addGasMapping(new ItemStackMekIngredient(stack), new GasStack(gas, amount));
    }

    public static boolean addGasMapping(@Nonnull Tag<Item> tag, @Nonnull Gas gas, int amount) {
        return addGasMapping(new TagMekIngredient(tag), new GasStack(gas, amount));
    }

    public static boolean addGasMapping(@Nonnull IMekanismIngredient<ItemStack> ingredient, @Nonnull GasStack gasStack) {
        Gas gas = gasStack.getGas();
        if (gas == null || gasStack.amount <= 0) {
            return false;
        }
        List<IMekanismIngredient<ItemStack>> ingredients = gasToIngredients.computeIfAbsent(gas, k -> new ArrayList<>());
        //TODO: Better checking at some point if the ingredient is already in there? Should partial checking happen as well
        ingredients.add(ingredient);
        return ingredientToGas.put(ingredient, gasStack) == null;
    }

    public static int removeGasMapping(@Nonnull IMekanismIngredient<ItemStack> ingredient, @Nonnull GasStack gasStack) {
        Gas gas = gasStack.getGas();
        if (gas != null && gasStack.amount > 0 && gasToIngredients.containsKey(gas)) {
            List<IMekanismIngredient<ItemStack>> ingredients = gasToIngredients.get(gas);
            List<IMekanismIngredient<ItemStack>> toRemove = new ArrayList<>();
            for (IMekanismIngredient<ItemStack> stored : ingredients) {
                if (stored.equals(ingredient)) {
                    //TODO: Better comparision??? Doesn't really matter until we have better duplication handling
                    // or have proper handling for if something is registered as an ore dict and as an item
                    toRemove.add(stored);
                    ingredientToGas.remove(stored);
                }
            }
            if (ingredients.size() == toRemove.size()) {
                //If we are removing all for that gas type then remove the list as well
                gasToIngredients.remove(gas);
            } else {
                ingredients.removeAll(toRemove);
            }
            return toRemove.size();
        }
        return 0;
    }

    public static void removeAllGasMappings() {
        gasToIngredients.clear();
        ingredientToGas.clear();
    }

    /**
     * Gets an item gas checking if it will be valid for a specific tank and if the type is also valid.
     */
    @Nullable
    public static GasStack getItemGas(ItemStack itemStack, GasTank gasTank, Predicate<Gas> isValidGas) {
        return getItemGas(itemStack, gasTank.getNeeded(), (gas, quantity) -> {
            if (gas != null && gasTank.canReceive(gas) && isValidGas.test(gas)) {
                return new GasStack(gas, quantity);
            }
            return null;
        });
    }

    /**
     * Gets the amount of ticks the declared itemstack can fuel this machine.
     *
     * @param itemStack - itemstack to check with.
     * @param needed    The max amount we need for use with IGasItem's so that we do not return a value that is too large, thus making it so it thinks there is no room.
     *
     * @return fuel ticks
     */
    @Nullable
    public static GasStack getItemGas(ItemStack itemStack, int needed, BiFunction<Gas, Integer, GasStack> getIfValid) {
        if (itemStack.getItem() instanceof IGasItem) {
            IGasItem item = (IGasItem) itemStack.getItem();
            GasStack gas = item.getGas(itemStack);
            //Check to make sure it can provide the gas it contains
            if (gas != null && item.canProvideGas(itemStack, gas.getGas())) {
                int amount = Math.min(needed, Math.min(gas.amount, item.getRate(itemStack)));
                if (amount > 0) {
                    GasStack gasStack = getIfValid.apply(gas.getGas(), amount);
                    if (gasStack != null) {
                        return gasStack;
                    }
                }
            }
        }
        for (Entry<IMekanismIngredient<ItemStack>, GasStack> entry : ingredientToGas.entrySet()) {
            if (entry.getKey().contains(itemStack)) {
                GasStack gasStack = getIfValid.apply(entry.getValue().getGas(), entry.getValue().amount);
                if (gasStack != null) {
                    return gasStack;
                }
            }
        }
        return null;
    }

    public static List<ItemStack> getStacksForGas(Gas type) {
        if (type == null) {
            return Collections.emptyList();
        }
        List<ItemStack> stacks = new ArrayList<>();
        //Always include the gas tank of the type
        stacks.add(MekanismUtils.getFullGasTank(GasTankTier.BASIC, type));
        //See if there are any gas to item mappings
        List<IMekanismIngredient<ItemStack>> ingredients = gasToIngredients.get(type);
        if (ingredients == null) {
            return stacks;
        }
        //TODO: Maybe check for duplicates if things are in oredict and not? For the most part things assume there are no duplication at the moment
        for (IMekanismIngredient<ItemStack> ingredient : ingredients) {
            stacks.addAll(ingredient.getMatching());
        }
        return stacks;
    }
}