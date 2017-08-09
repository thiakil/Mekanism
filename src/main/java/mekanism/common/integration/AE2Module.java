package mekanism.common.integration;

import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.states.BlockStateTransmitter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class AE2Module implements IMekanismHook {
    public void init()
    {
        for(BlockStateTransmitter.TransmitterType type : BlockStateTransmitter.TransmitterType.values())
        {
            if(type.getTransmission().equals(TransmissionType.ITEM))
            {
                FMLInterModComms.sendMessage(MekanismHooks.APPLIED_ENERGISTICS_2.MOD_ID, "add-p2p-attunement-item", new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
                continue;
            }

            if(type.getTransmission().equals(TransmissionType.FLUID))
            {
                FMLInterModComms.sendMessage(MekanismHooks.APPLIED_ENERGISTICS_2.MOD_ID, "add-p2p-attunement-fluid", new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
                continue;
            }

            if(type.getTransmission().equals(TransmissionType.ENERGY))
            {
                FMLInterModComms.sendMessage(MekanismHooks.APPLIED_ENERGISTICS_2.MOD_ID, "add-p2p-attunement-forge-power", new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
                continue;
            }

        }
    }
}
