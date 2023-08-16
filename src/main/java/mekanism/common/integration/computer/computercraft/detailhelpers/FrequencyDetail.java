package mekanism.common.integration.computer.computercraft.detailhelpers;

import dan200.computercraft.api.detail.BasicItemDetailProvider;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class FrequencyDetail extends BasicItemDetailProvider<IFrequencyItem> {
    public FrequencyDetail() {
        super("mekanism", IFrequencyItem.class);
    }

    @Override
    public void provideDetails(Map<? super String, Object> data, ItemStack stack, IFrequencyItem item) {
        Frequency frequency = item.getFrequency(stack);
        if (frequency != null) {
            data.put("frequency", CCDetails.CONVERSION_ONLY.convert(frequency));
        }
    }
}
