package mekanism.common.integration.computer.computercraft.detailhelpers;

import dan200.computercraft.api.detail.BasicItemDetailProvider;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleContainerDetail extends BasicItemDetailProvider<IModuleContainerItem> {
    ModuleContainerDetail() {
        super("mekanism", IModuleContainerItem.class);
    }

    @Override
    public void provideDetails(Map<? super String, Object> data, ItemStack stack, IModuleContainerItem item) {
        List<Module<?>> modules = item.getModules(stack);
        if (!modules.isEmpty()) {
            data.put("modules", modules.stream().collect(Collectors.toMap(ModuleContainerDetail::getModuleRegName, Module::getInstalledCount)));
        }
    }

    private static Object getModuleRegName(Module<?> module) {
        return CCDetails.CONVERSION_ONLY.convert(module.getData().getRegistryName());
    }
}
