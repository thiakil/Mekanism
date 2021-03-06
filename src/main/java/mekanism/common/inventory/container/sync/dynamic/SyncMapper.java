package mekanism.common.inventory.container.sync.dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.IClassSyncer;

public class SyncMapper {

    public static final SyncMapper INSTANCE = new SyncMapper();
    public static final String DEFAULT_TAG = "default";
    private final Map<Class<?>, IClassSyncer<?>> REGISTRY = new HashMap<>();

    private SyncMapper() {}

    /**
     * Copies the class mappings from the supplied map and then CLEARS the input map to avoid duplicates in memory
     *
     * @param newItems the items to add
     */
    public synchronized void addSyncItems(Map<Class<?>, IClassSyncer<?>> newItems) {
        REGISTRY.putAll(newItems);
        newItems.clear();
    }

    public void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier) {
        setup(container, holderClass, holderSupplier, DEFAULT_TAG);
    }

    public void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier, String tag) {
        @SuppressWarnings("unchecked")
        IClassSyncer<Object> syncer = (IClassSyncer<Object>) REGISTRY.get(holderClass);
        if (syncer != null) {
            syncer.register(holderSupplier, container::track, tag);
        }
    }
}