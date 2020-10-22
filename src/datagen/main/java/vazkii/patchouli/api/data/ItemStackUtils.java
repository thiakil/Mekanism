package vazkii.patchouli.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTDynamicOps;

/**
 * Created by Thiakil on 9/07/2020.
 */
public class ItemStackUtils {
    private static final Gson GSON = new GsonBuilder().create();

    public static String serializeStack(ItemStack stack) {
        StringBuilder builder = new StringBuilder();
        builder.append(stack.getItem().getRegistryName().toString());

        int count = stack.getCount();
        if (count > 1) {
            builder.append("#");
            builder.append(count);
        }

        if (stack.hasTag()) {
            Dynamic<?> dyn = new Dynamic<>(NBTDynamicOps.INSTANCE, stack.getTag());
            JsonElement j = dyn.convert(JsonOps.INSTANCE).getValue();
            builder.append(GSON.toJson(j));
        }

        return builder.toString();
    }

}
