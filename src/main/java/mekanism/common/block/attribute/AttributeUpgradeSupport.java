package mekanism.common.block.attribute;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import mekanism.api.Upgrade;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.UpgradesConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AttributeUpgradeSupport(@NotNull Set<Upgrade> supportedUpgrades, @Nullable UpgradesConfig.MachineUpgradeConfig config) implements Attribute {

    public static final Function<String, AttributeUpgradeSupport> DEFAULT_MACHINE_UPGRADES = blockId -> AttributeUpgradeSupport.create(blockId, Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
    public static final Function<String, AttributeUpgradeSupport> DEFAULT_ADVANCED_MACHINE_UPGRADES = blockId -> AttributeUpgradeSupport.create(blockId, Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.CHEMICAL);
    public static final Function<String, AttributeUpgradeSupport> SPEED_ENERGY = blockId -> AttributeUpgradeSupport.create(blockId, Upgrade.SPEED, Upgrade.ENERGY);
    public static final Function<String, AttributeUpgradeSupport> MUFFLING_ONLY = blockId -> AttributeUpgradeSupport.create(blockId, Upgrade.MUFFLING);
    public static final Function<String, AttributeUpgradeSupport> ENERGY_ONLY = blockId -> AttributeUpgradeSupport.create(blockId, Upgrade.ENERGY);
    public static final Function<String, AttributeUpgradeSupport> SPEED_ONLY = blockId -> AttributeUpgradeSupport.create(blockId, Upgrade.SPEED);
    public static final Function<String, AttributeUpgradeSupport> ANCHOR_ONLY = blockId -> AttributeUpgradeSupport.create(blockId, Upgrade.ANCHOR);

    public static AttributeUpgradeSupport create(String blockId, Upgrade... supportedUpgrades) {
        if (supportedUpgrades.length == 0) {
            throw new IllegalArgumentException("There must be at least one upgrade that is supported");
        }
        Set<Upgrade> upgrades;
        if (supportedUpgrades.length == 1) {
            upgrades = Set.of(supportedUpgrades[0]);
        } else if (supportedUpgrades.length == 2) {
            upgrades = Set.of(supportedUpgrades[0], supportedUpgrades[1]);
        } else {
            upgrades = EnumSet.noneOf(Upgrade.class);
            Collections.addAll(upgrades, supportedUpgrades);
            upgrades = Collections.unmodifiableSet(upgrades);
        }
        UpgradesConfig.MachineUpgradeConfig config = null;
        for (Upgrade upgrade : supportedUpgrades) {
            if (upgrade.hasModifier()) {
                config = MekanismConfig.upgrades.register(Arrays.stream(supportedUpgrades).filter(Upgrade::hasModifier), blockId);
                break;
            }
        }
        return new AttributeUpgradeSupport(upgrades, config);
    }
}
