package mekanism.common.integration.computer.computercraft.detailhelpers;

import dan200.computercraft.api.detail.DetailRegistry;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import mekanism.common.integration.computer.computercraft.CCComputerHelper;
import net.minecraft.world.item.ItemStack;

public class CCDetails {
    /** instance which will only work for conversion as arguments are null */
    static final CCComputerHelper CONVERSION_ONLY = new CCComputerHelper(null);

    public static void register() {
        DetailRegistry<ItemStack> stackDetailRegistry = VanillaDetailRegistries.ITEM_STACK;
        stackDetailRegistry.addProvider(new FrequencyDetail());
        stackDetailRegistry.addProvider(new ModuleContainerDetail());
    }
}
