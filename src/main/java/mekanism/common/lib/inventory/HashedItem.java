package mekanism.common.lib.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IHashedItem;
import mekanism.common.Mekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.DataComponentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper of an ItemStack which tests equality and hashes based on item type and NBT data, ignoring stack size.
 *
 * @author aidancbrady
 */
@NothingNullByDefault
public class HashedItem implements IHashedItem, DataComponentHolder {

    //copy of ItemStack.ITEM_NON_AIR_CODEC but not Holder<>
    static final Codec<Item> ITEM_NON_AIR_CODEC = BuiltInRegistries.ITEM
          .byNameCodec()
          .validate(
                p_330100_ -> p_330100_ == Items.AIR
                             ? DataResult.error(() -> "Item must not be minecraft:air")
                             : DataResult.success(p_330100_)
          );
    /**
     * @implNote This codec does not copy any uuid information if the hashed item is a {@link UUIDAwareHashedItem}
     */
    public static final Codec<HashedItem> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance ->
          instance.group(
                ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(HashedItem::getItem),
                DataComponentPatch.CODEC
                      .optionalFieldOf("components", null)
                      .forGetter(HashedItem::getDataPatch)
          ).apply(instance, HashedItem::new)
    ));
    /**
     * @implNote This codec does not copy any uuid information if the hashed item is a {@link UUIDAwareHashedItem}
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, HashedItem> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.registry(Registries.ITEM), HashedItem::getItem, DataComponentPatch.STREAM_CODEC, HashedItem::getDataPatch, HashedItem::new);

    public static Optional<HashedItem> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(p_330102_ -> Mekanism.logger.error("Tried to load invalid item: '{}'", p_330102_));
    }

    private static @Nullable DataComponentPatch patchOrNull(ItemStack stack) {
        DataComponentPatch componentsPatch = stack.getComponentsPatch();
        return componentsPatch.isEmpty() ? null : componentsPatch;
    }

    public static HashedItem create(ItemStack stack) {
        return new HashedItem(stack.getItem(), patchOrNull(stack));
    }

    private static int hash(Item item, @Nullable DataComponentPatch patch) {
        int i = 31 + item.hashCode();
        return 31 * i + (patch != null ? patch.hashCode() : 0);
    }

    private final Item item;
    @Nullable //null when empty
    private final DataComponentPatch componentPatch;
    private final int hashCode;
    @Nullable
    private ItemStack cachedStack;

    protected HashedItem(Item item, @Nullable DataComponentPatch patch) {
        this(item, patch, hash(item, patch));
    }

    protected HashedItem(HashedItem other) {
        this(other.item, other.componentPatch, other.hashCode);
        this.cachedStack = other.cachedStack;
    }

    protected HashedItem(Item item, @Nullable DataComponentPatch patch, int hashCode) {
        this.item = item;
        this.componentPatch = patch;
        this.hashCode = hashCode;
    }

    @Override//todo cache the max size instead?
    public ItemStack getInternalStack() {
        if (cachedStack == null) {
            cachedStack = createStack(1);
        }
        return cachedStack;
    }

    @Override
    public Item getItem() {
        return this.item;
    }

    @Override
    public @Nullable DataComponentPatch getDataPatch() {
        return this.componentPatch;
    }

    public boolean isPatchEmpty() {
        return componentPatch == null;
    }

    @Override
    public ItemStack createStack(int size) {
        return size <= 0 ? ItemStack.EMPTY : new ItemStack(item, size, createComponentMap());
    }

    /** Helper method to convert back into a ComponentMap */
    private @NotNull PatchedDataComponentMap createComponentMap() {
        return PatchedDataComponentMap.fromPatch(item.components(), componentPatch == null ? DataComponentPatch.EMPTY : componentPatch);
    }

    /**
     * Helper to serialize the internal stack to nbt.
     */
    @NotNull
    public Tag internalToNBT(HolderLookup.Provider provider) {
        return DataComponentUtil.wrapEncodingExceptions(this, CODEC, provider);
    }

    @Override
    public boolean matches(ItemStack stack) {
        //noinspection DataFlowIssue: isPatchEmpty implies it is not null
        return stack.getItem() == this.item &&
               stack.isComponentsPatchEmpty() == this.isPatchEmpty() &&
               (this.isPatchEmpty() || doComponentsMatch(componentPatch, stack));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof IHashedItem other && this.item == other.getItem() && Objects.equals(componentPatch, other.getDataPatch());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public DataComponentMap getComponents() {
        //only for debugging failures (via save()), no need to cache
        return createComponentMap();
    }

    public static class UUIDAwareHashedItem extends HashedItem {

        @Nullable
        private final UUID uuid;
        private final int uuidBasedHash;
        private final boolean overrideHash;

        /**
         * @param uuid Should not be null unless something went wrong reading the packet.
         *
         * @apiNote For use on the client side, hash is taken into account for equals and hashCode
         */
        public UUIDAwareHashedItem(Item item, @Nullable DataComponentPatch patch, @Nullable UUID uuid) {
            super(item, patch);
            this.uuid = uuid;
            if (this.uuid == null) {
                this.overrideHash = false;
                this.uuidBasedHash = super.hashCode();
            } else {
                this.overrideHash = true;
                this.uuidBasedHash = Objects.hash(super.hashCode(), this.uuid);
            }
        }

        public UUIDAwareHashedItem(HashedItem other, @NotNull UUID uuid) {
            super(other);
            this.uuid = uuid;
            this.uuidBasedHash = super.hashCode();
            this.overrideHash = false;
        }

        @Nullable
        public UUID getUUID() {
            return uuid;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (overrideHash) {
                //Note: UUID cannot be null if overrideHash is true
                //noinspection DataFlowIssue
                return obj instanceof UUIDAwareHashedItem uuidAware && uuid.equals(uuidAware.uuid) && super.equals(obj);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return uuidBasedHash;
        }

        /**
         * Converts this to a raw HashedItem that doesn't care about UUID anymore.
         */
        public HashedItem asRawHashedItem() {
            return new HashedItem(this);
        }
    }

    private static final MethodHandle itemStackComponents;
    /** {@link DataComponentPatch#map} */
    @SuppressWarnings("JavadocReference")
    private static final MethodHandle dataComponentPatchMap;
    /** {@link PatchedDataComponentMap#patch} */
    @SuppressWarnings("JavadocReference")
    private static final MethodHandle patchedDataComponentMapPatch;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            itemStackComponents = makeGetter(lookup, ItemStack.class, "components");
            dataComponentPatchMap = makeGetter(lookup, DataComponentPatch.class, "map");
            patchedDataComponentMapPatch = makeGetter(lookup, PatchedDataComponentMap.class, "patch");

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandle makeGetter(MethodHandles.Lookup lookup, Class<?> clazz, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field componentsField = clazz.getDeclaredField(fieldName);
        componentsField.setAccessible(true);
        return lookup.unreflectGetter(componentsField);
    }

    /**
     * Check components match without creating new objects or checking {@link ItemStack#isEmpty()} again
     */
    @SuppressWarnings("unchecked")
    public static boolean doComponentsMatch(DataComponentPatch patch, ItemStack stack) {
        try {
            //get the map without another empty check
            PatchedDataComponentMap rawComponentMap = (PatchedDataComponentMap) itemStackComponents.invokeExact(stack);
            //get the underlying map
            Reference2ObjectMap<DataComponentType<?>, Optional<?>> stackPatchMap = (Reference2ObjectMap<DataComponentType<?>, Optional<?>>) patchedDataComponentMapPatch.invokeExact(rawComponentMap);
            //get the underlying map for the patch
            Reference2ObjectMap<DataComponentType<?>, Optional<?>> dcPatchMap = (Reference2ObjectMap<DataComponentType<?>, Optional<?>>) dataComponentPatchMap.invokeExact(patch);

            return Objects.equals(dcPatchMap, stackPatchMap);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}