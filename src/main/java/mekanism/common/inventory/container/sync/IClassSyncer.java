package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IClassSyncer<T> {

    /**
     * Registers the ISyncableData instances for the class {@param obj}
     * @param target a supplier of object we're syncing
     * @param consumer stores the resulting data syncers.
     * @param tag the sync tag to register for
     */
    void register(Supplier<T> target, Consumer<ISyncableData> consumer, String tag);
}
