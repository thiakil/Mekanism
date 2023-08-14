package mekanism.common.integration.computer;

import dan200.computercraft.api.detail.ForgeDetailRegistries;
import mekanism.api.fluid.IMekanismFluidHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CC: Tweaked compatible tanks() method
 */
public class FluidTankFactory extends ComputerMethodFactory<IMekanismFluidHandler> {
    FluidTankFactory() {
        register("tanks", MethodRestriction.FLUID, NO_STRINGS, false, NO_STRINGS, NO_CLASSES, List.class, FluidTankFactory::getTanks);
    }

    public static Object getTanks(IMekanismFluidHandler handler, BaseComputerHelper helper) {
        Map<Integer, Map<String, ?>> result = new HashMap<>();
        var size = handler.getTanks();
        for (var i = 0; i < size; i++) {
            var stack = handler.getFluidInTank(i);
            if (!stack.isEmpty()) result.put(i + 1, ForgeDetailRegistries.FLUID_STACK.getBasicDetails(stack));
        }

        return result;
    }
}
