package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntityHighPressurePump extends TileEntityMekanism {

    private int mbPerTick = calculatePerTick(0);

    private static int calculatePerTick(int numUpgrades) {
        //y=ax^{2}+c
        //a = 50_000, c = 10_000, x = num_speed+1
        return MathUtils.clampToInt(50_000 * Math.round(Math.pow(numUpgrades + 1, 2)) + 10_000);
    }

    /**
     * Dummy tank to allow an external block to see us as a fluid handler.
     */
    public BasicFluidTank fluidTank;

    private MachineEnergyContainer<TileEntityHighPressurePump> energyContainer;
    private EnergyInventorySlot energySlot;

    public TileEntityHighPressurePump() {
        super(MekanismBlocks.HIGH_PRESSURE_PUMP);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(fluidTank = BasicFluidTank.create(0, BasicFluidTank.alwaysFalse, BasicFluidTank.alwaysFalse, this), RelativeSide.FRONT, RelativeSide.BACK);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this), RelativeSide.TOP);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 143, 35), RelativeSide.TOP);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        if (MekanismUtils.canFunction(this)) {
            FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);

                TileEntity teFront = WorldUtils.getTileEntity(world, getTilePos().offset(getDirection()));
                TileEntity teBack = WorldUtils.getTileEntity(world, getTilePos().offset(getOppositeDirection()));
                if (teFront != null && teBack != null) {
                    Optional<IFluidHandler> frontCap = teFront.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getOppositeDirection()).resolve();
                    Optional<IFluidHandler> backCap = teBack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getDirection()).resolve();
                    if (frontCap.isPresent() && backCap.isPresent()) {
                        IFluidHandler frontHandler = frontCap.get();
                        IFluidHandler backHandler = backCap.get();
                        FluidStack canDrain = frontHandler.drain(mbPerTick, FluidAction.SIMULATE);
                        if (!canDrain.isEmpty()) {
                            int canFill = backHandler.fill(canDrain, FluidAction.SIMULATE);
                            if (canFill > 0) {
                                FluidStack actuallyDrained = frontHandler.drain(canFill, FluidAction.EXECUTE);
                                int actuallyFilled = backHandler.fill(actuallyDrained, FluidAction.EXECUTE);
                                if (actuallyDrained.getAmount() - actuallyFilled > 0) {
                                    Mekanism.logger.warn("Fluid handler didn't fill as much as it said it could!");
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /*@Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);

        return nbtTags;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);

    }*/

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            mbPerTick = calculatePerTick(upgradeComponent.getUpgrades(Upgrade.SPEED));
        }
    }

    @Override
    public List<ITextComponent> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    public MachineEnergyContainer<TileEntityHighPressurePump> getEnergyContainer() {
        return energyContainer;
    }
}