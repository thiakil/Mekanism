package mekanism.patchouli

import mekanism.client.MekanismKeyHandler
import mekanism.common.Mekanism
import mekanism.common.MekanismLang
import mekanism.common.patchouli.GuideCategory
import mekanism.common.patchouli.GuideEntry
import mekanism.common.registries.MekanismBlocks
import mekanism.common.registries.MekanismBlocks.*
import mekanism.common.registries.MekanismItems.*
import mekanism.common.resource.OreType
import mekanism.common.resource.PrimaryResource
import mekanism.common.resource.ResourceType
import mekanism.patchouli.dsl.invoke
import mekanism.patchouli.dsl.link
import net.minecraft.data.DataGenerator
import net.minecraft.data.DirectoryCache

/**
 * Created by Thiakil on 16/09/2020.
 */
class MekanismMainBookProvider(generator: DataGenerator): BasePatchouliProvider(generator, Mekanism.MODID) {
    override fun act(output: DirectoryCache) {
        output("mekanism") {
            name = "Mekanism HandyGuide"
            locale = "en_us"
            landingText = "Here at Mekanism, Inc. we pride ourselves on our user-friendly creations, but sometimes a little nudge in the right direction is needed. Enter: the Mekanism HandyGuide - your handy dandy guide to the world of Mekanism."
            creativeTab = Mekanism.tabMekanism
            showProgress = false
            i18n = true//some item names etc
            subtitle = Mekanism.instance.versionNumber.toString()

            GuideCategory.ITEMS {
                name = "Items List"
                description = "A list of the items in Mekanism."
                icon = INFUSED_ALLOY
                sortNum = FORCED_ITEM_SORT_NUM

                GuideCategory.ITEMS_GEAR {
                    name = "Gear"
                    description = "Suit up, attack, or configure with these items."
                    icon = ELECTRIC_BOW

                    JETPACK("The Jetpack is an item that allows the player to fly, equippable in the chestplate slot. It uses Hydrogen gas as a fuel, of which it can store up to 24,000 mB.") {
                        text {
                            title = "Fueling"
                            text = "The Jetpack can be filled up wherever Hydrogen gas is outputted into a slot.$(br)" +
                                    "Here are a few examples:" +
                                    "$(li)It can be placed in the ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator's")} left output slot (where Hydrogen is outputted) after placing water in the machine's input slot" +
                                    "$(li)It can be placed in a ${link(GuideEntry.TANKS_GAS, "Chemical Tank's")} output slot.$(br2)" +
                                    "While worn, the Jetpack displays the Hydrogen remaining and the mode active in the bottom left corner."
                        }
                        text {
                            title = "Operation Modes"
                            text = "The Jetpack has three modes to choose from, which can be toggled by pressing the ${MekanismKeyHandler.chestModeSwitchKey()} key." +
                                    "$(li)$(bold)Regular$() (default): Press $(k:jump) to increase your height and release to fall. Note that you will take fall damage unless you carefully lower yourself to the ground." +
                                    "$(li)$(bold)Hover$(): Constant flight, without the need to level yourself like you do with Regular mode. Press $(k:jump) to increase altitude and press $(k:sneak) to decrease. Note that this mode constantly consumes Hydrogen, but at a reduced rate as compared to Regular mode." +
                                    "$(li)$(bold)Disabled$(): The Jetpack is disabled."
                        }
                        text {
                            title = "Tips"
                            text = "\$(li)The Jetpack cannot be worn with chestplate armor, since it uses the same slot, consider upgrading it to the ${link(ARMORED_JETPACK, "Armored Jetpack")} if you want protection." +
                                    "\$(li)The Jetpack emits fire particles; however, it will not set anything on fire."+
                                    "\$(li)If you want to maintain your altitude, choose Hover mode. If you want to ascend/descend rapidly, use Regular mode. If you want to conserve fuel while trekking across hills, mountains, consider Disabled mode."+
                                    "\$(li)The Jetpack can be paired with the Free Runners to protect against fall damage."
                        }
                    }
                    ATOMIC_DISASSEMBLER("The Atomic Disassembler is Mekanism's an all-in-one tool, essentially the ultimate, electronic version of the Paxel (working at any mining level). Also functions as a Hoe & Scoop (Forestry)$(p)The Atomic Disassembler has multiple modes that can be cycled with $(k:sneak) + right click.") {
                        text {
                            title = "Normal Mode"
                            text = "Base speed setting, single block.$(li)Roughly equivalent to Efficiency II.$(li)Right click Dirt to till a 3x3 area to Farmland$(li)Right click grass to make a 3x3 Grass Path, right click again to till"
                        }
                        text {
                            title = "Slow Mode"
                            text = "Slower than Normal Mode.$(li)Right click functions of Normal act on 1 block$(li)Less power usage"
                        }
                        text {
                            title = "Fast Mode"
                            text = "Super mode.$(li)Roughly equivalent to Efficiency V$(li)More power usage$(li)Right click functions of Normal act on 5x5"
                        }
                        text {
                            title = "Vein Mode"
                            text = "Like normal mode but will mine a vein of Ore or Log blocks (tagged with forge:ores or forge:logs) matching the start block."
                        }
                        text {
                            title = "Extended Vein Mining"
                            text = "Like Vein Mode, but works with any block."
                        }
                        text {
                            title = "Off"
                            text = "Functions as if out of power - no mining speed benefits or extended functionality."
                        }
                    }
                    ARMORED_JETPACK("The Armored Jetpack is an upgraded version of the ${link(JETPACK, "Jetpack")}. It is intended to provide 12 armor points, offering slightly better protection than a Diamond Chestplate with Protection IV. Numbers accurate as of Minecraft 1.7.10")
                    SCUBA_TANK("A piece of equipment worn in the chest armor slot that provides underwater respiration when a ${link(SCUBA_MASK, "Scuba Mask")} is worn. The Scuba Tank must be filled with Oxygen gas in order to function.") {
                        //todo equipment desc
                    }
                    SCUBA_MASK("The Scuba Mask is an utility head armor piece, used in conjunction with the ${link(SCUBA_TANK, "Scuba Tank")} to breathe underwater.") {
                        //todo equipment desc
                    }
                    CONFIGURATOR("The Configurator is a configuration tool for Mekanism machines & pipes.$(p)It comes with several different modes that you can switch between by sneaking and then pressing the Item Mode Switch Key (${MekanismKeyHandler.handModeSwitchKey()})") {
                        //todo equipment desc
                    }
                    ELECTRIC_BOW {
                        //todo equipment desc
                    }
                    FLAMETHROWER {
                        //todo equipment desc
                    }
                    FREE_RUNNERS {
                        //todo equipment desc
                    }
                    NETWORK_READER {
                        //todo equipment desc
                    }
                    PORTABLE_TELEPORTER {
                        //todo equipment desc
                    }
                    SEISMIC_READER {
                        //todo equipment desc
                    }
                }
                GuideCategory.ITEMS_METAL_AND_ORE {
                    name = "Metals & Ores"
                    description = "Ore/Metal processing based materials."
                    icon = PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)

                    BRONZE_DUST("")//todo
                    BRONZE_INGOT("")//todo
                    BRONZE_NUGGET("")//todo
                    CHARCOAL_DUST("")//todo
                    COAL_DUST("")//todo
                    DIAMOND_DUST("")//todo
                    EMERALD_DUST("")//todo
                    ENRICHED_IRON("")//todo
                    LAPIS_LAZULI_DUST("")//todo
                    LITHIUM_DUST("")//todo
                    OBSIDIAN_DUST("")//todo
                    QUARTZ_DUST("")//todo
                    REFINED_GLOWSTONE_INGOT("")//todo
                    REFINED_GLOWSTONE_NUGGET("")//todo
                    REFINED_OBSIDIAN_DUST("")//todo
                    REFINED_OBSIDIAN_INGOT("")//todo
                    REFINED_OBSIDIAN_NUGGET("")//todo
                    STEEL_DUST("")//todo
                    STEEL_INGOT("")//todo
                    STEEL_NUGGET("")//todo
                    SULFUR_DUST("")//todo
                }
                GuideCategory.ITEMS_UPGRADES {
                    name = "Upgrades"
                    description = "You gotta pump up them numbers, rookie. Increase various abilities of machines with these items."
                    icon = SPEED_UPGRADE

                    SPEED_UPGRADE("")//todo
                    ENERGY_UPGRADE("")//todo
                    FILTER_UPGRADE("")//todo
                    MUFFLING_UPGRADE("")//todo
                    GAS_UPGRADE("")//todo
                    ANCHOR_UPGRADE("The Anchor Upgrade is a machine upgrade which keeps the chunk of the machine to which it is applied loaded. This is helpful for machines like the Digital Miner and Teleporter which must be in loaded chunks to function properly.$(p)$(bold)Compatible machines$()$(li)${link(DIGITAL_MINER, "Digital Miner")}$(li)${link(QUANTUM_ENTANGLOPORTER, "Quantum Entangloporter")}$(li)${link(GuideEntry.TELEPORTER, "Teleporter")}")
                }

                GuideEntry.ALLOYS {
                    name = "Alloys"
                    icon = INFUSED_ALLOY
                    +"Crafting components used to make tiered items. Can also be right clicked on Logistical Transporters, Mechanical Pipes, Pressurized Tubes, Thermodynamic Conductors, and Universal Cables to upgrade tiers in-world.$(p)Created in a Metallurgic Infuser."
                    spotlight(INFUSED_ALLOY, "Redstone infused")
                    spotlight(REINFORCED_ALLOY, "Diamond infused")
                    spotlight(ATOMIC_ALLOY, "Refined Obsidian infused")
                }

                GuideEntry.CIRCUITS {
                    name = "Circuits"
                    icon = BASIC_CONTROL_CIRCUIT
                    +"Crafting components used to make tiered items."
                    spotlight(BASIC_CONTROL_CIRCUIT, "Osmium based.")
                    spotlight(ADVANCED_CONTROL_CIRCUIT, "Infused Alloy based.")
                    spotlight(ELITE_CONTROL_CIRCUIT, "Reinforced Alloy based.")
                    spotlight(ULTIMATE_CONTROL_CIRCUIT, "Atomic Alloy based.")
                }

                GuideEntry.INSTALLERS {
                    name = "Installers"
                    icon = BASIC_TIER_INSTALLER
                    +"Upgrade the tier of a block in world, without needing to put it in a crafting grid.$(p)Can upgrade factory machines, Bins, and Energy Cubes"
                    spotlight(BASIC_TIER_INSTALLER, "Upgrades block to basic tier. Used to turn machines into their factory variant.")
                    spotlight(ADVANCED_TIER_INSTALLER, "Upgrades block to Advanced tier. Requires block to be Basic tier.")
                    spotlight(ELITE_TIER_INSTALLER, "Upgrades block to Elite tier. Requires block to be Advanced tier.")
                    spotlight(ULTIMATE_TIER_INSTALLER, "Upgrades block to Ultimate tier. Requires block to be Elite tier.")
                }

                BIO_FUEL("A fuel made from plant material in a Crusher.$(p)Used in a Biofuel Generator (Mekanism Generators required) for power or ${link(PRESSURIZED_REACTION_CHAMBER, "Pressurized Reaction Chamber")} to produce Ethylene.") {
                    //todo override in mek generators' book generator?
                }
                CONFIGURATION_CARD("An item used to copy configuration data from one machine to another.$(p)To copy data to the card, $(k:sneak) + right click on the source machine, then right click the destination machine. Chat messages will inform you of the success/failure.$(p)Supported machines: ${link(DIGITAL_MINER, "Digital Miner")}, ${link(GuideEntry.ENERGY_CUBES, "Energy Cubes")}, ${link(FORMULAIC_ASSEMBLICATOR, "Formulaic Assemblicator")}, ${link(LOGISTICAL_SORTER, "Logistical Sorter")}, ${link(OREDICTIONIFICATOR, "Oredictionificator")}, and any machine with configurable sides.")
                CRAFTING_FORMULA("Used in the ${link(FORMULAIC_ASSEMBLICATOR, "Formulaic Assemblicator")} to encode a crafting recipe for automatic operation.")
            }// end items category

            GuideCategory.BLOCKS {
                name = "Blocks List"
                description = "A list of the blocks in Mekanism."
                icon = ULTIMATE_ENERGY_CUBE
                sortNum = FORCED_BLOCK_SORT_NUM

                GuideEntry.PIPES_LOGISTICAL {
                    name = "Logistical Transporters"
                    icon = BASIC_LOGISTICAL_TRANSPORTER
                    +"The Logistical Transporter is the basic item transport pipe for Mekanism logistics.$(br)With the Configurator the player can choose to \"paint\" the pipe with colors can can be detected by the pipe's color sorter by $(k:sneak) + right-clicking the center of a transporter with a Configurator.$(br2)It has 2 other cousins called the Diversion Transporter and the Restrictive Transporter."
                    spotlight(BASIC_LOGISTICAL_TRANSPORTER)
                    spotlight(ADVANCED_LOGISTICAL_TRANSPORTER)
                    spotlight(ELITE_LOGISTICAL_TRANSPORTER)
                    spotlight(ULTIMATE_LOGISTICAL_TRANSPORTER)
                }

                GuideEntry.PIPES_MECHANICAL {
                    name = "Mechanical Pipes"
                    icon = BASIC_MECHANICAL_PIPE
                    +"Mechanical Pipe is the fluid pipe for Mekanism logistics. It can be used to connect to any blocks with Fluid Handlers in them."
                    spotlight(BASIC_MECHANICAL_PIPE)
                    spotlight(ADVANCED_MECHANICAL_PIPE)
                    spotlight(ELITE_MECHANICAL_PIPE)
                    spotlight(ULTIMATE_MECHANICAL_PIPE)
                }

                GuideEntry.PIPES_GAS {
                    name = "Pressurized Tubes"
                    icon = BASIC_PRESSURIZED_TUBE
                    +"Pressurized Tubes are used to transport Gases. Similar to their mechanical counterparts, they can be upgraded to higher tiers to increase flow rate and capacity of pumped gases."
                    spotlight(BASIC_PRESSURIZED_TUBE)
                    spotlight(ADVANCED_PRESSURIZED_TUBE)
                    spotlight(ELITE_PRESSURIZED_TUBE)
                    spotlight(ULTIMATE_PRESSURIZED_TUBE)
                }

                GuideEntry.PIPES_HEAT {
                    name = "Thermodynamic Conductors"
                    icon = BASIC_THERMODYNAMIC_CONDUCTOR
                    +"Similar to the Universal Cable, the Thermodynamic Conductor is Mekanism's way of transferring power in the form of heat (essentially a heat pipe).$(2br)Transfer is lossy, depending on the biome the conductor is in. Warmer biomes have a higher transfer efficiency (less heat is lost), while colder biomes are lower (more heat is lost)."
                    spotlight(BASIC_THERMODYNAMIC_CONDUCTOR)
                    spotlight(ADVANCED_THERMODYNAMIC_CONDUCTOR)
                    spotlight(ELITE_THERMODYNAMIC_CONDUCTOR)
                    spotlight(ULTIMATE_THERMODYNAMIC_CONDUCTOR)
                }

                GuideEntry.PIPES_POWER {
                    name = "Universal Cables"
                    icon = BASIC_UNIVERSAL_CABLE
                    +"Universal Cables are Mekanism's way to transfer power. They are capable of transferring Mekanism's power Joules (J), as well as a variety of other power types such as Forge Energy (FE), Thermal Expansion's Redstone Flux (RF), Buildcraft's Minecraftjoules (MJ - display only), and Industrialcraft Energy Unit (EU). This flexibility allows players to mix power generation from different mods while still only using one type of cabling."
                    spotlight(BASIC_UNIVERSAL_CABLE)
                    spotlight(ADVANCED_UNIVERSAL_CABLE)
                    spotlight(ELITE_UNIVERSAL_CABLE)
                    spotlight(ULTIMATE_UNIVERSAL_CABLE)
                }

                GuideEntry.TANKS_LIQUID {
                    name = "Fluid Tanks"
                    icon = BASIC_FLUID_TANK
                    +"Tanks which store fluids. They can be placed as a block or used in Bucket mode ($(k:sneak) + $(k:${MekanismLang.KEY_HAND_MODE.translationKey}) to toggle)"
                    spotlight(BASIC_FLUID_TANK)
                    spotlight(ADVANCED_FLUID_TANK)
                    spotlight(ELITE_FLUID_TANK)
                    spotlight(ULTIMATE_FLUID_TANK)
                    spotlight(CREATIVE_FLUID_TANK)
                }

                GuideEntry.TANKS_GAS {
                    name = "Chemical Tanks"
                    icon = BASIC_CHEMICAL_TANK
                    +"Gas Tanks are Mekanism's batteries for storing Gases. They can be placed as a block and interact with Pressurized Tubes. They come in four tiers, each increasing the storage capacity and output rate."
                    spotlight(BASIC_CHEMICAL_TANK)
                    spotlight(ADVANCED_CHEMICAL_TANK)
                    spotlight(ELITE_CHEMICAL_TANK)
                    spotlight(ULTIMATE_CHEMICAL_TANK)
                    spotlight(CREATIVE_CHEMICAL_TANK)
                }

                GuideEntry.ENERGY_CUBES {
                    name = "Energy Cubes"
                    icon = BASIC_ENERGY_CUBE
                    +"An Energy Cube is an advanced type of battery that is compatible with multiple energy systems. The Input/Output mode of the side can be configured in the GUI"
                    spotlight(BASIC_ENERGY_CUBE)
                    spotlight(ADVANCED_ENERGY_CUBE)
                    spotlight(ELITE_ENERGY_CUBE)
                    spotlight(ULTIMATE_ENERGY_CUBE)
                    spotlight(CREATIVE_ENERGY_CUBE)
                }

                GuideEntry.INDUCTION_CELL {
                    name = "Induction Cells"
                    icon = BASIC_INDUCTION_CELL
                    +"Induction Cells are components in the Induction Matrix. Each cell increases the total energy storage of a Matrix. Note that this does not increase transfer rate; look to the Induction Providers for that."
                    spotlight(BASIC_INDUCTION_CELL)
                    spotlight(ADVANCED_INDUCTION_CELL)
                    spotlight(ELITE_INDUCTION_CELL)
                    spotlight(ULTIMATE_INDUCTION_CELL)
                }

                GuideEntry.INDUCTION_PROVIDER {
                    name = "Induction Providers"
                    icon = BASIC_INDUCTION_PROVIDER
                    +"The Induction Providers are used in the Induction Matrix to determine how fast it is able to output energy through the Induction Port.$(2br)Using multiple Induction Providers in the same Induction Matrix will add extra output capacity, by adding their values together.$(2br)The total output value is for the entire multi-block structure, and not on a \"per port\" basis."
                    spotlight(BASIC_INDUCTION_PROVIDER)
                    spotlight(ADVANCED_INDUCTION_PROVIDER)
                    spotlight(ELITE_INDUCTION_PROVIDER)
                    spotlight(ULTIMATE_INDUCTION_PROVIDER)
                }

                GuideEntry.BINS{ name = "Bins"
                    icon = BASIC_BIN
                    +"Bins are storage blocks which can hold large amounts of a single item. It will retain its inventory when broken. Each tier increases the storage capacity.$(p)To store something in a Bin right-click any side while holding an item or stack. This will store what's in your hand."
                    +"Double right-click to put the complete amount of an item in your inventory to the bin.$(p)Left-click on the front of the bin to extract a stack. $(k:sneak)-click to extract a single item.$(p)Items can be piped into the bin from the top, and piped out from the bottom. NB: other mods' item handlers are not restricted in this manner."
                    +"If you $(k:sneak)-click the bin with a Configurator it will be placed into auto-eject mode. This is indicated by green accents on the front, top, and bottom. In this mode it will pump items out of the bottom automatically."
                    spotlight(BASIC_BIN, "Holds 4,096 items.")
                    spotlight(ADVANCED_BIN, "Holds 8,192 items.")
                    spotlight(ELITE_BIN, "Holds 32,768 items.")
                    spotlight(ULTIMATE_BIN, "Holds 262,144 items.")
                    spotlight(CREATIVE_BIN, "Holds an infinite amount, does not deplete when withdrawing items.")

                }
            }

            GuideCategory.MULTIBLOCKS {
                name = "Multiblocks"
                description = "Structures formed using multiple blocks."
                icon = THERMAL_EVAPORATION_CONTROLLER

                GuideEntry.THERMAL_EVAP {
                    name = "Thermal Evaporation Plant"
                    icon = THERMAL_EVAPORATION_CONTROLLER
                    +"The Thermal Evaporation Plant is a 4x4 base multiblock for producing one liquid from another by way of heat energy. Minimum height is 3, maximum is 18.$(p)Heat can be supplied passively, actively by solar, or externally supplied."
                }
                GuideEntry.DYNAMIC_TANK {
                    name = "Dynamic Tank"
                    icon = DYNAMIC_VALVE
                    +"The blocks Dynamic Tank, Dynamic Glass, and Dynamic Valve make up the multi-block that is the Dynamic Tank, a fluid storage structure that can hold a large amount of a single type of fluid.$(p)Dynamic Tanks can be made in any size from 3x3x3 to 18x18x18, and does not need to be a cube."
                    +"A valid Dynamic Tank structure will flash with \"active redstone\" particles upon completion.$(p)Notes:$(li)All of the Dynamic Tank's borders must be made out of Dynamic Tank (not glass or valve)$(li)The tank's length, width, and height can be any number within the size limits - e.g. 3x4x5"
                }
                GuideEntry.TELEPORTER {
                    name = "Teleporter"
                    icon = TELEPORTER
                }
                GuideEntry.INDUCTION {
                    name = "Induction Matrix"
                    icon = BASIC_INDUCTION_CELL
                }
                GuideEntry.BOILER {
                    name = MekanismLang.BOILER.translationKey
                    icon = BOILER_VALVE
                }
            }

            GuideCategory.ORE_PROCESSING {
                name = "Ore Processing"
                description = "Get more ingots from your ore with these machine combinations."
                icon = ORES[OreType.OSMIUM]!!

                GuideEntry.ORE_DOUBLING {
                    name = "2x - Ore Doubling"
                    icon = ENRICHMENT_CHAMBER
                }
                GuideEntry.ORE_TRIPLING {
                    name = "3x - Ore Tripling"
                    icon = PURIFICATION_CHAMBER
                }
                GuideEntry.ORE_QUADRUPLING {
                    name = "4x - Ore Quadrupling"
                    icon = CHEMICAL_INJECTION_CHAMBER
                }
                GuideEntry.ORE_QUINTUPLING {
                    name = "5x - Ore Quintupling"
                    icon = CHEMICAL_DISSOLUTION_CHAMBER
                }
            }
        }
    }

    companion object {
        private const val FORCED_ITEM_SORT_NUM = 98
        private const val FORCED_BLOCK_SORT_NUM = 99
    }
}