package mekanism.common.content.filter;

import mekanism.api.math.MathUtils;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.integration.computer.BaseComputerHelper;

import java.util.function.BiFunction;

public enum FilterType {
    MINER_ITEMSTACK_FILTER          (FilterHolderType.MINER, FilterCategory.ITEM),
    MINER_MODID_FILTER              (FilterHolderType.MINER, FilterCategory.MODID),
    MINER_TAG_FILTER                (FilterHolderType.MINER, FilterCategory.TAG),
    SORTER_ITEMSTACK_FILTER         (FilterHolderType.SORTER, FilterCategory.ITEM),
    SORTER_MODID_FILTER             (FilterHolderType.SORTER, FilterCategory.MODID),
    SORTER_TAG_FILTER               (FilterHolderType.SORTER, FilterCategory.TAG),
    OREDICTIONIFICATOR_ITEM_FILTER  (FilterHolderType.OREDICTIONIFICATOR, FilterCategory.OREDICTIONIFICATOR),
    QIO_ITEMSTACK_FILTER            (FilterHolderType.QIO, FilterCategory.ITEM),
    QIO_MODID_FILTER                (FilterHolderType.QIO, FilterCategory.MODID),
    QIO_TAG_FILTER                  (FilterHolderType.QIO, FilterCategory.TAG);

    public final FilterHolderType<?> holderType;
    public final FilterCategory category;

    private static final FilterType[] FILTERS = values();

    FilterType(FilterHolderType<?> holderType, FilterCategory category) {
        this.holderType = holderType;
        this.category = category;
    }

    public static FilterType byIndexStatic(int index) {
        return MathUtils.getByIndexMod(FILTERS, index);
    }

    public static class FilterHolderType<FILTER extends IFilter<?>> {
        public static final FilterHolderType<MinerFilter<?>> MINER = new FilterHolderType<>(MinerFilter.class, BaseComputerHelper::convert);
        public static final FilterHolderType<SorterFilter<?>> SORTER = new FilterHolderType<>(SorterFilter.class, BaseComputerHelper::convert);
        public static final FilterHolderType<OredictionificatorItemFilter> OREDICTIONIFICATOR = new FilterHolderType<>(OredictionificatorItemFilter.class, BaseComputerHelper::convert);
        public static final FilterHolderType<QIOFilter<?>> QIO = new FilterHolderType<>(QIOFilter.class, BaseComputerHelper::convert);

        public final Class<FILTER> parentClass;
        public final BiFunction<BaseComputerHelper, FILTER, Object> computerConverter;

        FilterHolderType(Class<?> parentClass, BiFunction<BaseComputerHelper, FILTER, Object> computerConverter) {
            //noinspection rawtypes,unchecked
            this.parentClass = (Class)parentClass;
            this.computerConverter = computerConverter;
        }
    }

    public enum FilterCategory {
        /** implements IItemStackFilter */
        ITEM,
        /** implements IModIDFilter */
        MODID,
        /** implements ITagFilter */
        TAG,
        /* OREDICTIONIFICATOR specific */
        OREDICTIONIFICATOR
    }
}