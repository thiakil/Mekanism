package mekanism.common.patchouli;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.prefab.BlockFactoryMachine;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.OreType;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vazkii.patchouli.api.data.BookBuilder;
import vazkii.patchouli.api.data.CategoryBuilder;
import vazkii.patchouli.api.data.EntryBuilder;
import vazkii.patchouli.api.data.PatchouliBookProvider;
import vazkii.patchouli.api.data.page.SpotlightPageBuilder;

import static mekanism.common.registries.MekanismItems.*;
import static mekanism.common.registries.MekanismBlocks.*;

/**
 * Created by Thiakil on 18/05/2020.
 */
@SuppressWarnings("CodeBlock2Expr")
public class MekanismBookProvider extends PatchouliBookProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<IItemProvider, String> SPOTLIGHT_SUMMARIES = new IdentityHashMap<>();
    private static final int FORCED_ITEM_SORT_NUM = 98;
    private static final int FORCED_BLOCK_SORT_NUM = 99;

    static {

    }

    private final Set<IItemProvider> itemsAdded = Collections.newSetFromMap(new IdentityHashMap<>());

    public MekanismBookProvider(DataGenerator gen) {
        super(gen, Mekanism.MODID, "en_us");
        //add items/blocks we dont want auto entries for
        itemsAdded.add(BOUNDING_BLOCK);
        itemsAdded.add(ADVANCED_BOUNDING_BLOCK);
    }

    @Override
    public MekanismBookBuilder createBookBuilder(String id, String name, String landingText) {
        return new MekanismBookBuilder(Mekanism.MODID, id, name, landingText);
    }

    @Override
    protected void addBooks(Consumer<BookBuilder> consumer) {
        MekanismBookBuilder book = createBookBuilder("mekanism", "Mekanism HandyGuide", "Here at Mekanism, Inc. we pride ourselves on our user-friendly creations, but sometimes a little nudge in the right direction is needed. Enter: the Mekanism HandyGuide - your handy dandy guide to the world of Mekanism.");
        book.setCreativeTab(Mekanism.tabMekanism.getPath());
        book.setShowProgress(false);
        book.setI18n(true);
        book.setSubtitle(Mekanism.instance.versionNumber.toString());
        //todo setFillerTexture

        book.addCategory(GuideCategory.ITEMS, "Items List", "A list of the items in Mekanism.", MekanismItems.INFUSED_ALLOY, category -> {
            category.setSortnum(FORCED_ITEM_SORT_NUM);

            category.addSubCategory(GuideCategory.ITEMS_GEAR, "Gear", "Suit up, attack, or configure with these items.", MekanismItems.ELECTRIC_BOW, gearCategory -> {
                addItemEntry(gearCategory, JETPACK, jpEntry->{
                    jpEntry.addTextPage("The Jetpack can be filled up wherever Hydrogen gas is outputted into a slot. Here are a few examples:$(li)It can be placed in the "+pageLink(ELECTROLYTIC_SEPARATOR)+"Electrolytic Separator's$(/l) left output slot (where Hydrogen is outputted) after placing water in the machine's input slot$(li)It can be placed in a "+pageLink(GuideEntry.TANKS_GAS)+"Chemical Tank's$(/l) output slot.$(br2)While worn, the Jetpack displays the Hydrogen remaining and the mode active in the bottom left corner.", "Fueling");
                    jpEntry.addTextPage("The Jetpack has three modes to choose from, which can be toggled by pressing the $(k:" + MekanismLang.KEY_CHEST_MODE.getTranslationKey() + ") key.$(li)$(bold)Regular$() (default): Press $(k:jump) to increase your height and release to fall. Note that you will take fall damage unless you carefully lower yourself to the ground.$(li)"
                                        + "$(bold)Hover$(): Constant flight, without the need to level yourself like you do with Regular mode. Press $(k:jump) to increase altitude and press $(k:sneak) to decrease. Note that this mode constantly consumes Hydrogen, but at a reduced rate as compared to Regular mode.$(li)"
                                        + "$(bold)Disabled$(): The Jetpack is disabled.", "Operation Modes");
                    jpEntry.addTextPage("The Jetpack cannot be worn with chestplate armor, since it uses the same slot, consider upgrading it to the "+pageLink(ARMORED_JETPACK)+"Armored Jetpack$(/l) if you want protection.$(li)"
                                        + "The Jetpack emits fire particles; however, it will not set anything on fire.$(li)"
                                        + "If you want to maintain your altitude, choose Hover mode. If you want to ascend/descend rapidly, use Regular mode. If you want to conserve fuel while trekking across hills, mountains, consider Disabled mode.$(li)"
                                        + "The Jetpack can be paired with the Free Runners to protect against fall damage.", "Tips");
                });
                addItemEntry(gearCategory, ARMORED_JETPACK);
                addItemEntry(gearCategory, SCUBA_TANK);
                addItemEntry(gearCategory, SCUBA_MASK);
                addItemEntry(gearCategory, CONFIGURATOR);
                addItemEntry(gearCategory, ELECTRIC_BOW);
                addItemEntry(gearCategory, FLAMETHROWER);
                addItemEntry(gearCategory, FREE_RUNNERS);
                addItemEntry(gearCategory, NETWORK_READER);
                addItemEntry(gearCategory, PORTABLE_TELEPORTER);
                addItemEntry(gearCategory, SEISMIC_READER);
                EntryBuilder atomicDisassembler = addItemEntry(gearCategory, ATOMIC_DISASSEMBLER);
                atomicDisassembler.addTextPage("Base speed setting, single block.$(li)Roughly equivalent to Efficiency II.$(li)Right click Dirt to till a 3x3 area to Farmland$(li)Right click grass to make a 3x3 Grass Path, right click again to till", "Normal Mode");
                atomicDisassembler.addTextPage("Slower than Normal Mode.$(li)Right click functions of Normal act on 1 block$(li)Less power usage", "Slow Mode");
                atomicDisassembler.addTextPage("Super mode.$(li)Roughly equivalent to Efficiency V$(li)More power usage$(li)Right click functions of Normal act on 5x5", "Fast Mode");
                atomicDisassembler.addTextPage("Like normal mode but will mine a vein of Ore or Log blocks (tagged with forge:ores or forge:logs) matching the start block.", "Vein Mode");
                atomicDisassembler.addTextPage("Like Vein Mode, but works with any block.", "Extended Vein Mining");
                atomicDisassembler.addTextPage("Functions as if out of power - no mining speed benefits or extended functionality.", "Off");
            });
            category.addSubCategory(GuideCategory.ITEMS_METAL_AND_ORE, "Metals & Ores", "Ore/Metal processing based materials.", MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM), subcat->{
                addItemEntry(subcat, BRONZE_DUST);
                addItemEntry(subcat, LAPIS_LAZULI_DUST);
                addItemEntry(subcat, COAL_DUST);
                addItemEntry(subcat, CHARCOAL_DUST);
                addItemEntry(subcat, QUARTZ_DUST);
                addItemEntry(subcat, EMERALD_DUST);
                addItemEntry(subcat, DIAMOND_DUST);
                addItemEntry(subcat, STEEL_DUST);
                addItemEntry(subcat, SULFUR_DUST);
                addItemEntry(subcat, LITHIUM_DUST);
                addItemEntry(subcat, REFINED_OBSIDIAN_DUST);
                addItemEntry(subcat, OBSIDIAN_DUST);
                addItemEntry(subcat, REFINED_OBSIDIAN_INGOT);
                addItemEntry(subcat, BRONZE_INGOT);
                addItemEntry(subcat, REFINED_GLOWSTONE_INGOT);
                addItemEntry(subcat, STEEL_INGOT);
                addItemEntry(subcat, REFINED_OBSIDIAN_NUGGET);
                addItemEntry(subcat, BRONZE_NUGGET);
                addItemEntry(subcat, REFINED_GLOWSTONE_NUGGET);
                addItemEntry(subcat, STEEL_NUGGET);
                addItemEntry(subcat, ENRICHED_IRON);
                for (ItemRegistryObject<Item> item : MekanismItems.PROCESSED_RESOURCES.values()) {
                    addItemEntry(subcat, item);
                }
            });

            category.addSubCategory(GuideCategory.ITEMS_UPGRADES, "Upgrades", "You gotta pump up them numbers, rookie. Increase various abilities of machines with these items.", MekanismItems.SPEED_UPGRADE, subcat->{
                addItemEntry(subcat, SPEED_UPGRADE);
                addItemEntry(subcat, ENERGY_UPGRADE);
                addItemEntry(subcat, FILTER_UPGRADE);
                addItemEntry(subcat, MUFFLING_UPGRADE);
                addItemEntry(subcat, GAS_UPGRADE);
                addItemEntry(subcat, ANCHOR_UPGRADE);
            });

            addMultiItemEntry(category, GuideEntry.ALLOYS, "Alloys",  entry->{
                entry.addTextPage("Crafting components used to make tiered items. Can also be right clicked on Logistical Transporters, Mechanical Pipes, Pressurized Tubes, Thermodynamic Conductors, and Universal Cables to upgrade tiers in-world.$(p)Created in a Metallurgic Infuser.");
            }, INFUSED_ALLOY, REINFORCED_ALLOY, ATOMIC_ALLOY);

            addMultiItemEntry(category, GuideEntry.CIRCUITS, "Circuits", entry ->{
                entry.addTextPage("Crafting components used to make tiered items.");
            }, BASIC_CONTROL_CIRCUIT, ADVANCED_CONTROL_CIRCUIT, ELITE_CONTROL_CIRCUIT, ULTIMATE_CONTROL_CIRCUIT);

            addMultiItemEntry(category, GuideEntry.INSTALLERS, "Installers", entry->{
                entry.addTextPage("Upgrade the tier of a block in world, without needing to put it in a crafting grid.$(p)Can upgrade factory machines, Bins, and Energy Cubes");
            }, BASIC_TIER_INSTALLER, ADVANCED_TIER_INSTALLER, ELITE_TIER_INSTALLER, ULTIMATE_TIER_INSTALLER);

            //Add any non manually added entries to the generic list
            MekanismItems.ITEMS.getAllItems().stream()
                  .filter(it->!this.itemsAdded.contains(it))
                  .forEach(itemProvider -> addItemEntry(category, itemProvider));
        });


        book.addCategory(GuideCategory.BLOCKS, "Blocks List", "A list of the blocks in Mekanism.", MekanismBlocks.ULTIMATE_ENERGY_CUBE, category -> {
            category.setSortnum(FORCED_BLOCK_SORT_NUM);

            addMultiItemEntry(category, GuideEntry.PIPES_LOGISTICAL, "Logistical Transporters", entry -> {
                entry.addTextPage("The Logistical Transporter is the basic item transport pipe for Mekanism logistics.$(br)With the Configurator the player can choose to \"paint\" the pipe with colors can can be detected by the pipe's color sorter by $(k:sneak) + right-clicking the center of a transporter with a Configurator.$(br2)It has 2 other cousins called the Diversion Transporter and the Restrictive Transporter.");
            }, BASIC_LOGISTICAL_TRANSPORTER, ADVANCED_LOGISTICAL_TRANSPORTER, ELITE_LOGISTICAL_TRANSPORTER, ULTIMATE_LOGISTICAL_TRANSPORTER);

            addMultiItemEntry(category, GuideEntry.PIPES_MECHANICAL, "Mechanical Pipes", entryBuilder -> {
                entryBuilder.addTextPage("Mechanical Pipe is the fluid pipe for Mekanism logistics. It can be used to connect to any blocks with Fluid Handlers in them.");
            }, BASIC_MECHANICAL_PIPE, ADVANCED_MECHANICAL_PIPE, ELITE_MECHANICAL_PIPE, ULTIMATE_MECHANICAL_PIPE);

            addMultiItemEntry(category, GuideEntry.PIPES_GAS, "Pressurized Tubes", entryBuilder -> {
                entryBuilder.addTextPage("Pressurized Tubes are used to transport Gases. Similar to their mechanical counterparts, they can be upgraded to higher tiers to increase flow rate and capacity of pumped gases.");
            }, BASIC_PRESSURIZED_TUBE, ADVANCED_PRESSURIZED_TUBE, ELITE_PRESSURIZED_TUBE, ULTIMATE_PRESSURIZED_TUBE);

            addMultiItemEntry(category, GuideEntry.PIPES_HEAT, "Thermodynamic Conductors", entryBuilder -> {
                entryBuilder.addTextPage("Similar to the Universal Cable, the Thermodynamic Conductor is Mekanism's way of transferring power in the form of heat (essentially a heat pipe).$(2br)Transfer is lossy, depending on the biome the conductor is in. Warmer biomes have a higher transfer efficiency (less heat is lost), while colder biomes are lower (more heat is lost).");
            }, BASIC_THERMODYNAMIC_CONDUCTOR, ADVANCED_THERMODYNAMIC_CONDUCTOR, ELITE_THERMODYNAMIC_CONDUCTOR, ULTIMATE_THERMODYNAMIC_CONDUCTOR);

            addMultiItemEntry(category, GuideEntry.PIPES_POWER, "Universal Cables", entryBuilder -> {
                entryBuilder.addTextPage("Universal Cables are Mekanism's way to transfer power. They are capable of transferring Mekanism's power Joules (J), as well as a variety of other power types such as Forge Energy (FE), Thermal Expansion's Redstone Flux (RF), Buildcraft's Minecraftjoules (MJ - display only), and Industrialcraft Energy Unit (EU). This flexibility allows players to mix power generation from different mods while still only using one type of cabling.");
            }, BASIC_UNIVERSAL_CABLE, ADVANCED_UNIVERSAL_CABLE, ELITE_UNIVERSAL_CABLE, ULTIMATE_UNIVERSAL_CABLE);

            addMultiItemEntry(category, GuideEntry.TANKS_LIQUID, "Fluid Tanks", entryBuilder -> {
                entryBuilder.addTextPage("Tanks which store fluids. They can be placed as a block or used in Bucket mode ($(k:sneak) + $(k:" + MekanismLang.KEY_HAND_MODE.getTranslationKey() + ") to toggle)");
            }, BASIC_FLUID_TANK, ADVANCED_FLUID_TANK, ELITE_FLUID_TANK, ULTIMATE_FLUID_TANK, CREATIVE_FLUID_TANK);

            addMultiItemEntry(category, GuideEntry.TANKS_GAS, "Chemical Tanks", entryBuilder -> {
                entryBuilder.addTextPage("Gas Tanks are Mekanism's batteries for storing Gases. They can be placed as a block and interact with Pressurized Tubes. They come in four tiers, each increasing the storage capacity and output rate.");
            }, BASIC_CHEMICAL_TANK, ADVANCED_CHEMICAL_TANK, ELITE_CHEMICAL_TANK, ULTIMATE_CHEMICAL_TANK, CREATIVE_CHEMICAL_TANK);

            addMultiItemEntry(category, GuideEntry.BINS, "Bins", entryBuilder -> {
                entryBuilder.addTextPage("Bins are storage blocks which can hold large amounts of a single item. It will retain its inventory when broken. Each tier increases the storage capacity.$(p)To store something in a Bin right-click any side while holding an item or stack. This will store what's in your hand.");
                entryBuilder.addTextPage("Double right-click to put the complete amount of an item in your inventory to the bin.$(p)Left-click on the front of the bin to extract a stack. $(k:sneak)-click to extract a single item.$(p)Items can be piped into the bin from the top, and piped out from the bottom. NB: other mods' item handlers are not restricted in this manner.");
                entryBuilder.addTextPage("If you $(k:sneak)-click the bin with a Configurator it will be placed into auto-eject mode. This is indicated by green accents on the front, top, and bottom. In this mode it will pump items out of the bottom automatically.");
            }, BASIC_BIN, ADVANCED_BIN, ELITE_BIN, ULTIMATE_BIN, CREATIVE_BIN);

            addMultiItemEntry(category, GuideEntry.ENERGY_CUBES, "Energy Cubes", entryBuilder -> {
                entryBuilder.addTextPage("An Energy Cube is an advanced type of battery that is compatible with multiple energy systems. The Input/Output mode of the side can be configured in the GUI");
            }, BASIC_ENERGY_CUBE, ADVANCED_ENERGY_CUBE, ELITE_ENERGY_CUBE, ULTIMATE_ENERGY_CUBE, CREATIVE_ENERGY_CUBE);

            addMultiItemEntry(category, GuideEntry.INDUCTION_CELL, "Induction Cells", entryBuilder -> {
                entryBuilder.addTextPage("Induction Cells are components in the Induction Matrix. Each cell increases the total energy storage of a Matrix. Note that this does not increase transfer rate; look to the Induction Providers for that.");
            }, BASIC_INDUCTION_CELL, ADVANCED_INDUCTION_CELL, ELITE_INDUCTION_CELL, ULTIMATE_INDUCTION_CELL);

            addMultiItemEntry(category, GuideEntry.INDUCTION_PROVIDER, "Induction Providers", entryBuilder -> {
                entryBuilder.addTextPage("The Induction Providers are used in the Induction Matrix to determine how fast it is able to output energy through the Induction Port.$(2br)Using multiple Induction Providers in the same Induction Matrix will add extra output capacity, by adding their values together.$(2br)The total output value is for the entire multi-block structure, and not on a \"per port\" basis.");
            }, BASIC_INDUCTION_PROVIDER, ADVANCED_INDUCTION_PROVIDER, ELITE_INDUCTION_PROVIDER, ULTIMATE_INDUCTION_PROVIDER);

            //Add any non manually added entries to the generic list, excluding factories
            MekanismBlocks.BLOCKS.getAllBlocks().stream()
                  .filter(it->!(this.itemsAdded.contains(it) || it.getBlock() instanceof BlockFactoryMachine.BlockFactory))
                  .forEach(iBlockProvider -> addItemEntry(category, iBlockProvider));
        });

        book.addCategory(GuideCategory.MULTIBLOCKS, "Multiblocks", "Structures formed using multiple blocks.", MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, category-> {
            category.setSortnum(1);
            category.addEntry(GuideEntry.THERMAL_EVAP, "Thermal Evaporation Plant", THERMAL_EVAPORATION_CONTROLLER, entry->{
                entry.addTextPage("The Thermal Evaporation Plant is a 4x4 base multiblock for producing one liquid from another by way of heat energy. Minimum height is 3, maximum is 18.$(p)Heat can be supplied passively, actively by solar, or externally supplied.");
            });
            category.addEntry(GuideEntry.DYNAMIC_TANK, "Dynamic Tank", DYNAMIC_VALVE, entry->{
                entry.addTextPage("The blocks Dynamic Tank, Dynamic Glass, and Dynamic Valve make up the multi-block that is the Dynamic Tank, a fluid storage structure that can hold a large amount of a single type of fluid.$(p)Dynamic Tanks can be made in any size from 3x3x3 to 18x18x18, and does not need to be a cube.");
                entry.addTextPage("A valid Dynamic Tank structure will flash with \"active redstone\" particles upon completion.$(p)Notes:$(li)All of the Dynamic Tank's borders must be made out of Dynamic Tank (not glass or valve)$(li)The tank's length, width, and height can be any number within the size limits - e.g. 3x4x5");
            });
            category.addEntry(GuideEntry.TELEPORTER, "Teleporter", TELEPORTER, entry->{
                entry.addTextPage("TODO");//todo
            });
            category.addEntry(GuideEntry.INDUCTION, "Induction Matrix", BASIC_INDUCTION_CELL, entry->{
                entry.addTextPage("TODO");//todo
            });
            category.addEntry(GuideEntry.BOILER, MekanismLang.BOILER.getTranslationKey(), BOILER_VALVE, entry->{
                entry.addTextPage("TODO");//todo
            });
        });

        book.addCategory(GuideCategory.ORE_PROCESSING, "Ore Processing", "Get more ingots from your ore with these machine combinations.", MekanismBlocks.ORES.get(OreType.OSMIUM), category -> {
            category.setSortnum(2);
            category.addEntry(GuideEntry.ORE_DOUBLING, "2x - Ore Doubling", ENRICHMENT_CHAMBER, entry->{
                entry.addTextPage("TODO");//todo
            });
            category.addEntry(GuideEntry.ORE_TRIPLING, "3x - Ore Tripling", PURIFICATION_CHAMBER, entry->{
                entry.addTextPage("TODO");//todo
            });
            category.addEntry(GuideEntry.ORE_QUADRUPLING, "4x - Ore Quadrupling", CHEMICAL_INJECTION_CHAMBER, entry->{
                entry.addTextPage("TODO");//todo
            });
            category.addEntry(GuideEntry.ORE_QUINTUPLING, "5x - Ore Quintupling", CHEMICAL_DISSOLUTION_CHAMBER, entry->{
                entry.addTextPage("TODO");//todo
            });
        });

        book.build(consumer);
    }

    private EntryBuilder addItemEntry(CategoryBuilder category, IItemProvider itemProvider) {
        this.itemsAdded.add(itemProvider);
        String translationKey = itemProvider.getTranslationKey();
        EntryBuilder entry = category.addEntry(id(itemProvider), translationKey, itemProvider.getItemStack());
        entry.setReadByDefault(true);
        addSpotlightEntry(itemProvider, entry);
        return entry;
    }

    @Nonnull
    private static String id(IItemProvider itemProvider) {
        String type = itemProvider instanceof IBlockProvider ? "block" : "item";
        return type + "/" + itemProvider.getRegistryName().getPath();
    }

    private static String pageLink(IItemProvider itemProvider) {
        return "$(l:"+id(itemProvider)+")";
    }

    private static String pageLink(GuideEntry guideEntry) {
        return "$(l:"+guideEntry.getEntryId()+")";
    }

    private void addItemEntry(CategoryBuilder category, IItemProvider itemProvider, Consumer<EntryBuilder> builder) {
        builder.accept(this.addItemEntry(category, itemProvider));
    }

    private void addSpotlightEntry(IItemProvider itemProvider, EntryBuilder entry) {
        if (!SPOTLIGHT_SUMMARIES.containsKey(itemProvider)) {
            LOGGER.warn("No Spotlight summary for {}", itemProvider.getRegistryName());
        }
        entry.addSpotlightPage(itemProvider.getItemStack()).setLinkRecipe(true).setText(SPOTLIGHT_SUMMARIES.getOrDefault(itemProvider, "No entry written :("));
    }

    private void addMultiItemEntry(MekanismCategoryBuilder category,  GuideEntry guideEntry, String title, Consumer<EntryBuilder> pageGenerator, IItemProvider... items) {
        EntryBuilder entry = category.addEntry(guideEntry, title, items[0]);
        entry.setReadByDefault(true);
        pageGenerator.accept(entry);
        for (IItemProvider item : items) {
            this.itemsAdded.add(item);
            addSpotlightEntry(item, entry);
        }
    }

}
