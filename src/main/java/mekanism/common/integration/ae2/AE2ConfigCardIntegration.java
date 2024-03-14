package mekanism.common.integration.ae2;

import appeng.block.AEBaseEntityBlock;
import appeng.block.networking.CableBusBlock;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class AE2ConfigCardIntegration {

    public static void register(RegisterCapabilitiesEvent event) {
        BuiltInRegistries.BLOCK.forEach(block -> {
            if (block instanceof CableBusBlock) {
                event.registerBlock(Capabilities.CONFIG_CARD, AE2ConfigCardCompatCableBus::getBusCapability, block);
            } else if (block instanceof AEBaseEntityBlock<?>) {
                event.registerBlock(Capabilities.CONFIG_CARD, AE2ConfigCardCompatBlock::getBlockCapability, block);
            }
        });
    }
}
