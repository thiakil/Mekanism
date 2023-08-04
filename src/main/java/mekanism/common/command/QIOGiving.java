package mekanism.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import java.util.Collections;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.item.ItemQIODrive;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.TileComponentFrequency;
import mekanism.common.registries.MekanismItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Created by Thiakil on 4/08/2023.
 */
class QIOGiving {

    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("qio_give")
              .requires(MekanismPermissions.COMMAND_QIO_GIVE.and(cs -> cs.getEntity() instanceof ServerPlayer))
              .executes(ctx -> {
                  ServerPlayer player = ctx.getSource().getPlayerOrException();
                  ItemStack qioDrive = MekanismItems.TIME_DILATING_QIO_DRIVE.getItemStack();
                  ItemQIODrive drive = (ItemQIODrive) qioDrive.getItem();
                  FakeHolder holder = new FakeHolder();
                  holder.slot.setStack(qioDrive);
                  QIODriveData.QIODriveKey key = new QIODriveKey(holder, 0);
                  FrequencyManager<QIOFrequency> manager = FrequencyType.QIO.getManager(player.getUUID());
                  String FREQ_NAME = "__mek_qio_give";
                  QIOFrequency freq = manager.getOrCreateFrequency(new FrequencyIdentity(FREQ_NAME, false), player.getUUID());
                  freq.addDrive(key);
                  while (freq.getTotalItemTypes(false) < Math.min(ForgeRegistries.ITEMS.getValues().size(), freq.getTotalItemTypeCapacity()) && freq.getTotalItemCount() < freq.getTotalItemCountCapacity()) {
                      ForgeRegistries.ITEMS.getValues().stream().limit(freq.getTotalItemTypeCapacity()).forEach(item->freq.addItem(new ItemStack(item, 1)));
                  }
                  freq.saveAll();
                  freq.removeDrive(key, true);
                  manager.remove(FREQ_NAME, player.getUUID());
                  player.addItem(holder.slot.getStack());
                  ctx.getSource().sendSuccess(()-> Component.literal("Success"), true);
                  return 0;
              });
    }

    static class FakeHolder implements IQIODriveHolder {
        private BasicInventorySlot slot = BasicInventorySlot.at(null, 0,0);
        @Override
        public List<IInventorySlot> getDriveSlots() {
            return Collections.singletonList(slot);
        }

        @Override
        public void onDataUpdate() {

        }

        @Override
        public TileComponentFrequency getFrequencyComponent() {
            return null;
        }

        @Override
        public BlockPos getTilePos() {
            return new BlockPos(0,0,0);
        }

        @Override
        public Level getTileWorld() {
            return null;
        }
    }
}
