package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;

public interface IClassSyncer<T> {

    /**
     * Registers the ISyncableData instances for the class {@param obj}
     * @param obj The value object we're syncing
     * @param consumer stores the resulting data syncers. TODO
     */
    void register(T obj, Consumer<ISyncableData> consumer);
}
