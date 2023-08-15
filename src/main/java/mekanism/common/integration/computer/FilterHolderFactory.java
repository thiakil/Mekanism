package mekanism.common.integration.computer;

import mekanism.common.content.filter.*;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("rawtypes")
public class FilterHolderFactory extends ComputerMethodFactory<ITileFilterHolder> {

    public static final String[] ITEM_ARG_NAME = {"item"};
    @SuppressWarnings("rawtypes")
    public static final Class[] ITEM_ARGUMENT_CLASS = {Item.class};
    public static final String[] MOD_ID_ARG_NAME = {"modId"};
    @SuppressWarnings("rawtypes")
    public static final Class[] STRING_ARG = {String.class};
    public static final String[] TAG_ARG_NAME = {"tag"};

    @SuppressWarnings({"unchecked"})
    @Override
    void bindTo(@Nullable ITileFilterHolder subject, @NotNull BoundMethodHolder holder) {
        if (subject == null) {
            return;
        }
        FilterType.FilterHolderType<?> holderType = subject.getFilterManager().getFilterHolderType();
        for (FilterType filterType : FilterType.values()) {
            if (filterType.holderType == holderType) {
                if (filterType.category == FilterType.FilterCategory.ITEM) {
                    holder.register("createItemFilter",true, ITEM_ARG_NAME, ITEM_ARGUMENT_CLASS, holderType.parentClass,null, (o, helper) -> {
                        IItemStackFilter<?> filter = (IItemStackFilter<?>) BaseFilter.fromType(filterType);
                        filter.setItemStack(new ItemStack(helper.getItem(0)));
                        return castResult((FilterType.FilterHolderType) holderType, filter, helper);
                    });
                } else if (filterType.category == FilterType.FilterCategory.MODID) {
                    holder.register("createModIdFilter",true, MOD_ID_ARG_NAME, STRING_ARG, holderType.parentClass,null, (o, helper) -> {
                        IModIDFilter<?> filter = (IModIDFilter<?>) BaseFilter.fromType(filterType);
                        filter.setModID(helper.getString(0));
                        return castResult((FilterType.FilterHolderType) holderType, filter, helper);
                    });
                }else if (filterType.category == FilterType.FilterCategory.TAG) {
                    holder.register("createTagFilter",true, TAG_ARG_NAME, STRING_ARG, holderType.parentClass,null, (o, helper) -> {
                        ITagFilter<?> filter = (ITagFilter<?>) BaseFilter.fromType(filterType);
                        filter.setTagName(helper.getString(0));
                        return castResult((FilterType.FilterHolderType) holderType, filter, helper);
                    });
                }
            }
        }
    }

    private static <FILTER extends IFilter<FILTER>> Object castResult(FilterType.FilterHolderType<FILTER> holderType, IFilter<?> filter, BaseComputerHelper helper) {
        return holderType.computerConverter.apply(helper, holderType.parentClass.cast(filter));
    }
}
