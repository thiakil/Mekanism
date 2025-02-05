package mekanism.common.lib.heat;

import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.longs.LongLongSortedPair;
import java.util.SortedSet;
import java.util.TreeSet;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.ApiStatus.Internal;

@EventBusSubscriber(modid = Mekanism.MODID)
public class HeatManager {
    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post event) {
        AttachmentType<HeatManager> HEAT = MekanismAttachmentTypes.HEAT_MANAGER.get();
        if (event.getLevel() instanceof ServerLevel level && level.hasData(HEAT)) {
            level.getData(HEAT).tick();
        }
    }

    private final SortedSet<BlockPos> heatSourcePositions = new TreeSet<>();
    private final Level level;

    public HeatManager(Level level) {
        this.level = level;
    }

    public void registerHeatTransfer(BlockPos pos) {
        heatSourcePositions.add(pos.immutable());
    }

    @Internal
    public void tick() {
        SortedSet<LongLongPair> handledTransfers = new TreeSet<>(LongLongPair.lexComparator());
        while (!heatSourcePositions.isEmpty()) {
            BlockPos pos1 = heatSourcePositions.removeFirst();
            long pos1Long = pos1.asLong();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                BlockPos pos2 = pos1.relative(direction);
                long pos2Long = pos2.asLong();
                if (!handledTransfers.add(LongLongSortedPair.of(pos1Long, pos2Long))) {
                    continue;
                }
                doHeatTransfer(pos1, pos2, direction);
            }
        }
    }

    /**
     * Attempt a heat transfer. Dedupe should have happened already.
     *
     * @param pos1 first position
     * @param pos2 second position
     * @param side side relative to pos1
     */
    private void doHeatTransfer(BlockPos pos1, BlockPos pos2, Direction side) {
        IHeatHandler pos1Cap = level.getCapability(Capabilities.HEAT, pos1, side);
        if (pos1Cap == null) {
            return;
        }
        IHeatHandler pos2Cap = level.getCapability(Capabilities.HEAT, pos2, side.getOpposite());
        if (pos2Cap == null) {
            return;
        }

        //todo swap these around based on which is hotter?

        double heatCapacity = pos1Cap.getTotalHeatCapacity();
        double invConduction = pos2Cap.getTotalInverseConduction() + pos1Cap.getTotalInverseConduction();
        double tempToTransfer = (pos1Cap.getTotalTemperature() - getAmbientTemperature(pos1)) / invConduction;
        //TODO - 1.18: Try and figure out how to do this properly/I believe the below is correct
        // but it seems to nerf the heat system quite a bit so needs more review than being able
        // to be done just before a release is made
        /*double temp = getTotalTemperature(side);
        double sinkTemp = sink.getTotalTemperature();
        if (temp <= sinkTemp) {
            //If our temperature is lower than the sink, we skip calculating what the adjacent loss to the sink
            // is as if the sink is able to have heat transferred away from it (which is a bit of a weird concept
            // in relation to thermodynamics, but makes some sense with our implementation), it will be handled by
            // the sink when the sink simulates adjacent heat transfers. This also prevents us from having heat
            // transfers effectively happen "twice" per tick rather than just once
            // Note: We also skip if our temp is equal to the sink's temperature so that we can short circuit
            // past the following logic
            continue;
        }
        double heatCapacity = getTotalHeatCapacity(side);
        double sinkHeatCapacity = sink.getTotalHeatCapacity();
        //Calculate the target temperature using calorimetry
        double finalTemp = (temp * heatCapacity + sinkTemp * sinkHeatCapacity) / (heatCapacity + sinkHeatCapacity);
        double invConduction = sink.getTotalInverseConduction() + getTotalInverseConductionCoefficient(side);
        double tempToTransfer = (temp - finalTemp) / invConduction;*/
        double heatToTransfer = tempToTransfer * heatCapacity;
        pos1Cap.handleHeat(-heatToTransfer);
        //Note: Our sinks in mek are "lazy" but they will update the next tick if needed
        pos2Cap.handleHeat(heatToTransfer);
        //TODO adjacentTransfer = incrementAdjacentTransfer(adjacentTransfer, tempToTransfer, side);
    }

    private double getAmbientTemperature(BlockPos pos) {
        return HeatAPI.getAmbientTemp(level, pos);//todo cache
    }
}
