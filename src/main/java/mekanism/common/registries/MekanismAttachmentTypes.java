package mekanism.common.registries;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.lib.heat.HeatManager;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@NothingNullByDefault
public class MekanismAttachmentTypes {

    private MekanismAttachmentTypes() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Mekanism.MODID);

    //Note: We do not specify copy on death as we want radiation to reset to baseline on death
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Double>> RADIATION = ATTACHMENT_TYPES.register("radiation",
          () -> AttachmentType.builder(() -> RadiationManager.BASELINE)
                .serialize(Codec.doubleRange(RadiationManager.BASELINE, Double.MAX_VALUE), radiation -> radiation != RadiationManager.BASELINE)
                .copyHandler((radiation, holder, provider) -> radiation == RadiationManager.BASELINE ? null : radiation)
                .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FlamethrowerMode>> FLAMETHROWER_MODE = ATTACHMENT_TYPES.register("flamethrower_mode", () ->
          AttachmentType.builder(() -> FlamethrowerMode.COMBAT)
                .serialize(FlamethrowerMode.CODEC, mode -> mode != FlamethrowerMode.COMBAT)
                .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HeatManager>> HEAT_MANAGER = ATTACHMENT_TYPES.register("heat_manager", () -> AttachmentType.builder(holder -> {
        Preconditions.checkArgument(holder instanceof Level, "Must be attached to a Level");
        return new HeatManager((Level) holder);
    }).build());
}