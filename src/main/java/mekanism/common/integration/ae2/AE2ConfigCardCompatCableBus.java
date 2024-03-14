package mekanism.common.integration.ae2;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.util.SettingsFrom;
import mekanism.api.IConfigCardAccess;
import mekanism.api.NBTConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AE2ConfigCardCompatCableBus extends AE2ConfigCardCompatBlock {

    static IConfigCardAccess getBusCapability(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, UseOnContext context) {
        if (blockEntity instanceof IPartHost partHost) {
            IPart part = partHost.selectPartWorld(context.getClickLocation()).part;
            if (part != null) {
                return new AE2ConfigCardCompatCableBus(level, pos, state, context, part);
            }
        }
        return null;
    }

    private final IPart part;

    public AE2ConfigCardCompatCableBus(Level level, BlockPos pos, BlockState state, UseOnContext context, IPart part) {
        super(state, null);
        this.part = part;
    }

    @Override
    public String getConfigCardName() {
        Item partItem = part.getPartItem().asItem();

        // Blocks and parts share the same soul!
        if (AEParts.INTERFACE.asItem() == partItem) {
            partItem = AEBlocks.INTERFACE.asItem();
        } else if (AEParts.PATTERN_PROVIDER.asItem() == partItem) {
            partItem = AEBlocks.PATTERN_PROVIDER.asItem();
        }

        return partItem.getDescriptionId();
    }

    @Override
    public ResourceLocation getConfigurationDataType() {
        return new ResourceLocation("ae2", getConfigCardName());
    }

    @Override
    public CompoundTag getConfigurationData(Player player) {
        CompoundTag data = new CompoundTag();

        part.exportSettings(SettingsFrom.MEMORY_CARD, data);
        CompoundTag wrapped = new CompoundTag();
        wrapped.put(KEY_AEDATA, data);
        return wrapped;
    }

    @Override
    public void setConfigurationData(Player player, CompoundTag rawData) {
        String ae2type = rawData.getString(NBTConstants.DATA_NAME);
        if (ae2type.isEmpty()) {
            return;
        }
        CompoundTag aedata = rawData.getCompound(KEY_AEDATA);
        if (getConfigCardName().equals(ae2type)) {
            part.importSettings(SettingsFrom.MEMORY_CARD, aedata, player);
        }
    }
}
