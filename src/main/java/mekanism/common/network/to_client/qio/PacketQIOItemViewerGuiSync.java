package mekanism.common.network.to_client.qio;

import com.mojang.datafixers.util.Function3;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Optional;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//TODO - 1.19: Split implementations of this packet as it is possible for it to technically become too large and cause a crash
// Also ideally we only would sync the hashed item for types we haven't sent a given client yet so that then
// we can also send a smaller packet to each client until they disconnect and then we clear what packets they know
public abstract class PacketQIOItemViewerGuiSync implements IMekanismPacket {

    private static final StreamCodec<RegistryFriendlyByteBuf, Object2LongMap<UUIDAwareHashedItem>> ITEM_MAP_CODEC = ByteBufCodecs.map(Object2LongOpenHashMap::new,
          StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM), HashedItem::getItem, DataComponentPatch.STREAM_CODEC, HashedItem::getDataPatch,
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), item -> Optional.ofNullable(item.getUUID()),
                (item, components, uuid) -> new UUIDAwareHashedItem(item, components, uuid.orElse(null))
          ), ByteBufCodecs.VAR_LONG
    );

    protected static <PKT extends PacketQIOItemViewerGuiSync> StreamCodec<RegistryFriendlyByteBuf, PKT> streamCodec(
          Function3<Long, Integer, Object2LongMap<UUIDAwareHashedItem>, PKT> constructor) {
        return StreamCodec.composite(
              ByteBufCodecs.VAR_LONG, pkt -> pkt.countCapacity,
              ByteBufCodecs.VAR_INT, pkt -> pkt.typeCapacity,
              ITEM_MAP_CODEC, pkt -> pkt.itemMap,
              constructor
        );
    }

    protected final Object2LongMap<UUIDAwareHashedItem> itemMap;
    protected final long countCapacity;
    protected final int typeCapacity;

    protected PacketQIOItemViewerGuiSync(long countCapacity, int typeCapacity, Object2LongMap<UUIDAwareHashedItem> itemMap) {
        this.itemMap = itemMap;
        this.countCapacity = countCapacity;
        this.typeCapacity = typeCapacity;
    }
}
