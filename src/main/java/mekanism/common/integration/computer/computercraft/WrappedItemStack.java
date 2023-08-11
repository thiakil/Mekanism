package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.IDynamicLuaObject;
import mekanism.common.integration.computer.Convertable;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class WrappedItemStack extends CCMethodCallerV2 implements IDynamicLuaObject {
    final ItemStack stack;

    public WrappedItemStack(ItemStack stack) {
        this.stack = stack;
        FactoryRegistry.bindTo(this, this);
    }

    @ComputerMethod
    public Convertable<Map<String, Object>> getBasicDetails() {
        return convert(VanillaDetailRegistries.ITEM_STACK.getBasicDetails(this.stack));
    }

    @ComputerMethod
    public Convertable<Map<String, Object>> getDetails() {
        return convert(VanillaDetailRegistries.ITEM_STACK.getDetails(this.stack));
    }

    //no conversion is actually necessary, as we use CC's methods
    private static Convertable<Map<String,Object>> convert(Map<String, Object> map) {
        return Convertable.of(map, (baseComputerHelper, stringObjectMap) -> stringObjectMap);
    }
}
