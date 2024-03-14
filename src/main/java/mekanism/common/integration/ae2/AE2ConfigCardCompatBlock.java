package mekanism.common.integration.ae2;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.SettingsFrom;
import mekanism.api.IConfigCardAccess;
import mekanism.api.NBTConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AE2ConfigCardCompatBlock implements IConfigCardAccess {
    protected static final String KEY_AEDATA = "ae2data";

    public static AE2ConfigCardCompatBlock getBlockCapability(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, UseOnContext context) {
        return new AE2ConfigCardCompatBlock(state, blockEntity);
    }

    private final Block aeBlock;
    private final AEBaseBlockEntity blockEntity;

    public AE2ConfigCardCompatBlock(BlockState state, @Nullable BlockEntity blockEntity) {
        this.aeBlock = state.getBlock();
        this.blockEntity = (AEBaseBlockEntity) blockEntity;
    }

    @Override
    public String getConfigCardName() {
        return aeBlock.getDescriptionId();
    }

    @Override
    public ResourceLocation getConfigurationDataType() {
        return new ResourceLocation("ae2", getConfigCardName());
    }

    @Override
    public CompoundTag getConfigurationData(Player player) {
        CompoundTag data = new CompoundTag();
        if (blockEntity == null) {
            return data;//empty
        }
        blockEntity.exportSettings(SettingsFrom.MEMORY_CARD, data, player);
        CompoundTag wrapped = new CompoundTag();
        wrapped.put(KEY_AEDATA, data);
        return wrapped;
    }

    @Override
    public void setConfigurationData(Player player, CompoundTag rawData) {
        if (blockEntity == null) {
            return;
        }
        String ae2type = rawData.getString(NBTConstants.DATA_NAME);
        if (ae2type.isEmpty()) {
            return;
        }
        CompoundTag aedata = rawData.getCompound(KEY_AEDATA);
        if (aeBlock.getDescriptionId().equals(ae2type)) {
            blockEntity.importSettings(SettingsFrom.MEMORY_CARD, aedata, player);
        }
    }

    @Override
    public void configurationDataSet() {

    }
}
