package mekanism.common.config;

import com.google.common.base.Preconditions;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.Upgrade;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.config.value.CachedIntValue;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

public class UpgradesConfig extends BaseMekanismConfig {

    @Nullable
    private ModConfigSpec.Builder builder = new ModConfigSpec.Builder();;
    @Nullable
    private ModConfigSpec configSpec;

    @Override
    public String getFileName() {
        return "upgrades";
    }

    @Override
    public String getTranslation() {
        return "Upgrades Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return Objects.requireNonNull(configSpec, "not baked!");
    }

    UpgradesConfig bake() {
        configSpec = Objects.requireNonNull(builder, "Config already built!").build();
        builder = null;
        return this;
    }

    @Override
    public boolean isLoaded() {
        return this.configSpec != null && super.isLoaded();
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }

    public MachineUpgradeConfig register(Stream<Upgrade> upgrades, String blockId) {
        Preconditions.checkState(configSpec == null, "Config already built");
        String blockTranslationKey = Util.makeDescriptionId("block", ResourceLocation.fromNamespaceAndPath(Mekanism.MODID, blockId));
        return new MachineUpgradeConfig(Objects.requireNonNull(builder), this, upgrades, blockTranslationKey, blockId);
    }

    public static class MachineUpgradeConfig {

        private final Map<Upgrade, CachedIntValue> countSupported = new EnumMap<>(Upgrade.class);
        private final Map<Upgrade, CachedIntValue> maxUpgradeMultiplier = new EnumMap<>(Upgrade.class);

        public MachineUpgradeConfig(ModConfigSpec.Builder builder, UpgradesConfig parent, Stream<Upgrade> upgrades, String blockTranslationKey, String blockName) {
            builder.translation(blockTranslationKey).push(blockName);
            upgrades
                  .forEach(upgrade -> {
                      Preconditions.checkArgument(upgrade.hasModifier(), "Upgrades list not checked for multiplier");
                      builder.translation(upgrade.getTranslationKey()).push(upgrade.getSerializedName());
                      countSupported.put(upgrade, CachedIntValue.wrap(parent, MekanismConfigTranslations.GENERAL_UPGRADE_COUNT.applyToBuilder(builder)
                            .defineInRange("maxCount", upgrade.getMax(), 0, MathUtils.PRETTY_MAX_INT)));
                      //todo: split translation key
                      maxUpgradeMultiplier.put(upgrade, CachedIntValue.wrap(parent, MekanismConfigTranslations.GENERAL_UPGRADE_MULTIPLIER.applyToBuilder(builder)
                            .defineInRange("maxMultiplier", 10, 1, MathUtils.PRETTY_MAX_INT)));
                      builder.pop();
                  });
            builder.pop();
            Preconditions.checkArgument(!countSupported.isEmpty(), "Can't build empty upgrades");
        }

        public int getMaxCount(Upgrade upgrade) {
            return getDefaulted(upgrade, countSupported);
        }

        public int getMaxModifier(Upgrade upgrade) {
            return getDefaulted(upgrade, maxUpgradeMultiplier);
        }

        private int getDefaulted(Upgrade upgrade, Map<Upgrade, CachedIntValue> map) {
            CachedIntValue cachedIntValue = map.get(upgrade);
            return cachedIntValue != null ? cachedIntValue.getOrDefault() : 0;
        }
    }
}
