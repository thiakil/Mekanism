package mekanism.patchouli.content

import mekanism.client.MekanismKeyHandler
import mekanism.common.content.gear.Modules
import mekanism.common.registries.MekanismBlocks.*
import mekanism.common.registries.MekanismGases.*
import mekanism.common.registries.MekanismItems.*
import mekanism.common.resource.OreType
import mekanism.common.resource.PrimaryResource
import mekanism.common.resource.ResourceType
import mekanism.patchouli.GuideCategory
import mekanism.patchouli.GuideEntry
import mekanism.patchouli.MekanismMainBookProvider
import mekanism.patchouli.dsl.PatchouliBook
import mekanism.patchouli.dsl.invoke
import mekanism.patchouli.dsl.link

fun PatchouliBook.itemCategory() {
    GuideCategory.ITEMS {
        name = "Items List"
        description = "A list of the items in Mekanism."
        icon = INFUSED_ALLOY
        sortNum = MekanismMainBookProvider.FORCED_ITEM_SORT_NUM

        GuideCategory.ITEMS_GEAR {
            name = "Gear"
            description = "Suit up, attack, or configure with these items."
            icon = ELECTRIC_BOW

            JETPACK("We here at Mekanism, Inc. are not responsible for any incidents involving fall damage.$(p)The Jetpack is an item that allows the player to fly, equippable in the chestplate slot. It uses ${link(HYDROGEN, "Hydrogen")} as a fuel, of which it can store up to 24,000 mB.") {
                text {
                    title = "Fueling"
                    text = "The Jetpack can be filled up wherever Hydrogen gas is outputted into a slot.$(br)" +
                            "Here are a few examples:" +
                            "$(li)It can be placed in the ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator's")} hydrogen slot after placing water in the machine's input slot" +
                            "$(li)It can be placed in a ${link(GuideEntry.TANKS_GAS, "Chemical Tank's")} output slot.$(br2)" +
                            "While worn, the Jetpack displays the Hydrogen remaining and the mode active in the bottom left corner."
                }
                text {
                    title = "Operation Modes"
                    text = "The Jetpack has three modes to choose from, which can be toggled by pressing the ${MekanismKeyHandler.chestModeSwitchKey()} key." +
                            "$(li)$(bold)Regular$() (default): Press $(k:jump) to increase your height and release to fall. Note that you will take fall damage unless you carefully lower yourself to the ground." +
                            "$(li)$(bold)Hover$(): Constant flight, without the need to level yourself like you do with Regular mode. Press $(k:jump) to increase altitude and press $(k:sneak) to decrease. "
                }
                +("Note that this mode constantly consumes Hydrogen, but at a reduced rate as compared to Regular mode." +
                        "$(li)$(bold)Disabled$(): The Jetpack is disabled.")
                text {
                    title = "Tips"
                    text = "\$(li)The Jetpack cannot be worn with chestplate armor, since it uses the same slot, consider upgrading it to the ${link(ARMORED_JETPACK, "Armored Jetpack")} if you want protection." +
                            "\$(li)The Jetpack emits fire particles; however, it will not set anything on fire." +
                            "\$(li)If you want to maintain your altitude, choose Hover mode. " +
                            "\$(li)If you want to ascend/descend rapidly, use Regular mode. "
                }
                text {
                    text = "\$(li)If you want to conserve fuel while trekking across hills, mountains, consider Disabled mode." +
                            "\$(li)The Jetpack can be paired with the Free Runners to protect against fall damage."
                }
            }
            MEKA_TOOL("The ultimate in personal equipment and weaponry.  Can serve a variety of tasks.  Mining speed can be configured by pressing \"Item Mode Switch\".  All other settings are configured using the \"Module Tweaker\" key.")
            ATOMIC_DISASSEMBLER("The Atomic Disassembler is Mekanism's an all-in-one tool, essentially the ultimate, electronic version of the Paxel (working at any mining level). Also functions as a Hoe & Scoop (Forestry)$(p)The Atomic Disassembler has multiple modes that can be cycled with $(k:sneak) + right click.") {
                text {
                    title = "Normal Mode"
                    text = "Base speed setting, single block, roughly equivalent to Efficiency II.$(p)Farmland tilling and Grass Path functions have been moved to the MekaTool ${link(Modules.FARMING_UNIT, "Farming Unit")}"
                }
                text {
                    title = "Slow Mode"
                    text = "Slower than Normal Mode, less power usage"
                }
                text {
                    title = "Fast Mode"
                    text = "Super mode, roughly equivalent to Efficiency V. Uses more energy to function."
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
                +"When you first put on the Scuba Tank, its oxygen supply will be turned off. In order to use it underwater you must turn it on by using the ${MekanismKeyHandler.chestModeSwitchKey()} button. Also, you must have the Gas Mask equipped in the helmet armor slot or you won't be able to breathe and will start to drown."
                text {
                    title = "Tips"
                    text = "$(li)Any potion effect will instantly stop once you equip the Gas Mask and turn on the oxygen supply." +
                            "$(li)Be sure to turn the oxygen supply OFF when you are above surface, otherwise you'll waste oxygen." +
                            "$(li)Leave the oxygen supply on while underwater. You can't cheat to conserve oxygen by turning it on and off, because the oxygen will be consumed faster to refill the breath meter." +
                            "$(li)The Scuba Tank will not work in Airless dimensions in some other mods, such as the outer space dimensions in Galacticraft (Moon, Mars, etc.)."
                }
            }
            SCUBA_MASK("The Scuba Mask is an utility head armor piece, used in conjunction with the ${link(SCUBA_TANK, "Scuba Tank")} to breathe underwater.$(p)It can be enchanted with respiration and water breathing for extended use.")
            CONFIGURATOR("The Configurator is a configuration tool for Mekanism machines & pipes.$(p)It comes with several different modes that you can switch between by sneaking and then pressing the Item Mode Switch Key (${MekanismKeyHandler.handModeSwitchKey()})") {
                text("Configurate") {
                    text = "Mousing over a Mekanism machine or factory will show the color for that side, using the the Configuration Color Scheme. Right clicking will print a message announcing both the color and input/output mode. $(k:sneak) + Right Clicking will cycle through the valid colors for the given sub-mode."
                }
                +("$(li)Grey is no connection (neither in nor out)." +
                        "\$(li)Dark Red is input (items, gasses)." +
                        "\$(li)Dark Blue is output (items, gasses)." +
                        "\$(li)Green is for Energy input (items, cable)." +
                        "\$(li)Purple is Infusion item input (for the Metallurgic Infuser)" +
                        "\$(li)Yellow is for fluids (for the Pressurized Reaction Chamber)")
                +("Additionally, you can interact with any of the cables, pipes, transporters, or tubes to set their connection type between machines/inventory and their redstone sensitivity. Right clicking on the center of the cable/pipe/transporter/tube will toggle sensitivity off/on (default is on). \$(k:sneak) + Right clicking on a segment between the center of the cable/etc. and machine will cycle between:" +
                        "$(li)Normal" +
                        "\$(li)Pull - try to take from the machine")
                +("\$(li)Push - try to insert only into the machine" +
                        "\$(li)None - no connection. Will not try to push or pull items from the machine.")
                text("Empty") {
                    text = "\$(k:sneak) + Right Clicking on the machine while in this mode will eject any and all items currently in the machine in random directions. It will not dump fluids or gasses."
                }
                text("Rotate") {
                    text = "Right clicking on a face will have that set as \"forward\" while \$(k:sneak) + Right clicking will have that set as \"back\" The Energy Cube can have its top and bottom faces designated as \"forward.\""
                }
                text("Wrench") {
                    text = "Behaves like a wrench from most other mods. Right click to rotate the machine clockwise on the ground, \$(k:sneak) + Right click to have the machine instantly pried loose as an item (works on cables and pipes, too!)"
                }
            }
            ELECTRIC_BOW("Arrows not included.") {
                +"Much like a normal bow, but uses energy instead of durability. Can also set arrows on fire (toggle with ${MekanismKeyHandler.handModeSwitchKey()})."
            }
            FLAMETHROWER("The Flamethrower is a ranged weapon which uses Hydrogen gas as its fuel. It is fairly effective against mobs as it deals damage when they are directly hit with the stream and sets them on fire. It is most effective on large groups of mobs, where the user can hose down the entire group with fuel at a short distance.") {
                text("Modes") {
                    text = "You can switch between three fire modes using \$(k:sneak) + ${MekanismKeyHandler.handModeSwitchKey()}. The modes are" +
                            "$(li)\$(bold)Combat\$() - The default mode. Damages mobs and sets them on fire. Destroys any items on the ground. Does not set fire to blocks nor damage them." +
                            "$(li)\$(bold)Heat\$() - Same as combat, but blocks/items that have a smelter recipe will be instantly converted into it. For example you can fire a short burst at iron ore block and a single ingot of iron"
                }
                +"will be dropped. \$(li)\$(bold)Inferno\$() - Same as combat, but blocks that the stream hits will be hit with blast damage (like with creepers, ghasts, TNT) and will usually be destroyed. Nearby blocks will be set on fire."
            }
            FREE_RUNNERS("Free Runners are an item that allows players to ascend 1-block inclines automatically, as well as preventing fall damage as long as they are charged. A fall will reduce the item's charge, depending on how far the fall was.$(p)Can be toggled with ${MekanismKeyHandler.feetModeSwitchKey()}")
            NETWORK_READER("Sends information about the targeted pipe network to chat.")
            PORTABLE_TELEPORTER("A player kept teleportation device. It can store power and like all Mekanism teleporters, energy drain increases with the distance the player teleports to.") {
                text {
                    title = "Usage"
                    text = "Right-clicking with this device in hand will open a GUI similar to that of the full Teleporter, allowing instant travel to any Teleporters that the player has set up. The Portable Teleporter is capable of multidimensional travel.$(p)Note that in order for the Portable Teleporter to be functional, the complete Teleporter Portal structure does $(bold)not$() need to be built; only the Teleporter block must be present (and supplied with power)."
                }
            }
            SEISMIC_READER("The Seismic Reader is used in conjunction with the ${link(SEISMIC_VIBRATOR, "Seismic Vibrator")} to analyze the ground immediately around the vibrator, informing you of the blocks, by level, all the way to bedrock level.")
            CANTEEN("The Canteen is used to store ${link(NUTRITIONAL_PASTE, "Nutritional Paste")} (total of 64 Buckets). When hungry, you can hold right click to drink some Nutritional Paste. Each hunger point (half a hunger bar) consumes 50mB of Nutritional Paste.")
            GAUGE_DROPPER("The Gauge Dropper is a really handy tool for managing the fluid/chemical inventories of machines/blocks.$(p)Open up the inventory of the machine, click on your Gauge Dropper to move it around the GUI. Hover over the substance you want to extract with the Gauge Dropper and left click. The substance should now be") {
                +"in the Gauge Dropper (it can hold up to 16,000mB).\$(p) You can hover over a fluid/gas gauge and right click to deposit the stored contents in your Gauge Dropper. \$(p)Shift left click on a gauge to dump all of the content in said gauge. \$(p)Shift right click on any block, while holding the Gauge Dropper, to dump all of the Gauge Dropper's contents."
            }
            //TODO check dictionary functions
            DICTIONARY("Don't worry, you won't have to read much.") {
                +"The Dictionary allows you to check the Tags (vanilla mechanic) of an item which can be used in a Tag Filter for things like the ${link(DIGITAL_MINER, "Digital Miner")} & ${link(LOGISTICAL_SORTER, "Logistical Sorter")}, or in the ${link(OREDICTIONIFICATOR, "Oredictionificator")}."
                text {
                    title = "Usage"
                    text = "Right click to open the GUI and insert an item into the slot. The tags will be listed.$(p)Right clicking a block in-world will tell you the Tags of that block."
                }
            }

            DOSIMETER("Measures your accumulated radiation dosage.\$(p)Use the ${link(GEIGER_COUNTER, "Geiger Counter")} to assess the level of radiation in an area.") {
                text {
                    title = "Usage"
                    text = "Right click in the air to show $(bold)your$() current radiation dose in the chat feed. NB: this will never be zero, as there is always some background radiation."
                }
            }
            GEIGER_COUNTER("Measures the radiation level around you.\$(p)Use the ${link(DOSIMETER, "Dosimeter")} to assess your existing radiation exposure."){
                text {
                    title = "Usage"
                    text = "Right click in the air to show the $(bold)surrounding$() radiation level in the chat feed. NB: this will never be zero, as there is always some background radiation."
                }
            }
            entry(HAZMAT_GOWN) {
                name = "Hazmat Suit"
                +"The hazmat suit will protect you from surrounding radiation. You have to wear all the pieces of the hazmat suit in order to receive full protection from radiation. Otherwise, your radiation dosage will increase.$(p)Damage from $(bold)prior$() exposure will continue."
                spotlight(HAZMAT_MASK, "For your face.")
                spotlight(HAZMAT_GOWN, "For your torso.")
                spotlight(HAZMAT_PANTS, "For your legs.")
                spotlight(HAZMAT_BOOTS, "For your feet.")
            }
            entry(MEKASUIT_BODYARMOR) {
                name = "MekaSuit"
                +"The ultimate in personal protection.  A high-powered energy suit that makes you nearly invincible.  Can be upgraded with even more functions and protections using modules"
                spotlight(MEKASUIT_HELMET, "Protect your head.")
                spotlight(MEKASUIT_BODYARMOR, "Protect your torso.")
                spotlight(MEKASUIT_PANTS, "Protect your legs.")
                spotlight(MEKASUIT_BOOTS, "Protect your feet.")

            }
        }
        GuideCategory.ITEMS_MODULES {
            MODULES[Modules.ATTACK_AMPLIFICATION_UNIT]?.invoke("ttack Amplification Units will boost your ${link(MEKA_TOOL, "MekaTool")}\'s attack damage.\$(p)Max units per Mekatool: 4.\$(br)Max damage: 32.")
            MODULES[Modules.CHARGE_DISTRIBUTION_UNIT]?.invoke("Charge Distribution Units will distribute gained energy evenly to all MekaSuit armor pieces.  Normally if you have installed Solar Recharging Units onto your MekaSuit Helmet, only the helmet will gain all the energy it's producing.") {
                text {
                    text = "With the Charge Distribution Unit installed, the produced energy will be evenly distributed throughout all MekaSuit armor pieces.  (I know this sounds like an ad, but trust me it's not.\$(p)Max units per bodyarmor: 1"
                }
            }
            MODULES[Modules.DOSIMETER_UNIT]?.invoke("Dosimeter Units will display your radiation dose in the HUD on the bottom right corner.\$(br)Max unit per bodyarmor: 1")
            MODULES[Modules.ELECTROLYTIC_BREATHING_UNIT]?.invoke("Electrolytic Breathing Units will allow you to breathe underwater.  In addition, if the Jetpack Unit is installed onto the MekaSuit Bodyarmor, it will automatically begin to refuel its hydrogen supply while underwater or out in the rain.\$(p)Max units per helmet: 4")
            MODULES[Modules.ENERGY_UNIT]?.invoke("Energy Units will increase the energy capacity of the selected equipment.  Max units per equipment: 8.\$(br)Max energy: 1.63 GFE.")
            MODULES[Modules.EXCAVATION_ESCALATION_UNIT]?.invoke("Increases the maximum mining speed of your ${link(MEKA_TOOL, "MekaTool")}.\$(br)Max units per mekatool: 4.\$(br)Max speed: 128.")
            MODULES[Modules.FARMING_UNIT]?.invoke("Farming Units allow you to tilt soil, strip logs, and flatten soil. Well, yeah.\$(br)Max units per Meka-Tool: 4")
            //TODO: Uncomment when/if this gets merged into 10.1
            /*MODULES[Modules.GEIGER_UNIT]?.invoke("Shows ambient radiation on the HUD.  Installed on ${link(MEKASUIT_BODYARMOR, "MekaSuit Bodyarmor")}")*/
            MODULES[Modules.GRAVITATIONAL_MODULATING_UNIT]?.invoke("Gravitational Modulating Units allows you to utilize creative flight.  If the MekaSuit bodyarmor also has a Jetpack Unit installed, the Jetpack Unit will be automatically disabled if the Gravitational Modulating Unit is enabled, and vice versa.\$(p)Max unit per bodyarmor: 1")
            MODULES[Modules.HYDRAULIC_PROPULSION_UNIT]?.invoke("Hydraulic Propulsion Units will allow you to climb over increased heights without jumping. It also allows you to jump higher when you hold \$(k:key.mekanism.key_boost) and press \$(k:key.jump). \$(p)Max units per boots: 4.\$(br)Max step height:2.\$(br)Max jump height:5.")
            MODULES[Modules.INHALATION_PURIFICATION_UNIT]?.invoke("Inhalation Purification Units will protect you from selected potion effects including beneficial, neutral, and harmful ones. Using its stored energy, it can nullify the potion effects.") {
                text {
                    text = "The potion may still look like it is in effect (e.g. black hearts and injuring noises when hit with a Potion of Decay), but in actuality the player will not receive any of its effects.\$(p)Max unit per helmet: 1"
                }
            }
            MODULES[Modules.JETPACK_UNIT]?.invoke("Jetpack Units will allow your MekaSuit Bodyarmor to have the functions of a ${link(JETPACK, "Jetpack")}. Once applied, the MekaSuit Bodyarmor will have an internal storage for hydrogen (48,000mb).") {
                text {
                    text = "The Jetpack Unit is best paired with the ${link(Modules.ELECTROLYTIC_BREATHING_UNIT, "Electrolytic Breathing Unit")}. Doing so allows the accumulation of hydrogen when underwater or in the rain. \$(br)Alternatively, hydrogen can manually be refueled using a ${link(GuideEntry.TANKS_GAS, "Chemical Tank")} or anything else that can output Hydrogen.\$(p)Max units per bodyarmor: 1."
                }
            }
            MODULES[Modules.LOCOMOTIVE_BOOSTING_UNIT]?.invoke("Locomotive Boosting Units will allow you to boost your sprinting speed. It can be configured from 0.0 sprint boost, to a 0.5 sprint boost.\$(br)Note that your hunger consumption rate would also increase as you run faster.\$(p)Max units per pants: 4.")
            MODULES[Modules.MAGNETIC_ATTRACTION_UNIT]?.invoke("Magnet Attraction Units allow you to attract dropped items on the ground. When an is item being pulled towards you, there will have a blue lightning looking line in between you and the item.\$(p)Max units per boots: 4.")
            MODULES[Modules.NUTRITIONAL_INJECTION_UNIT]?.invoke("Nutritional Injection Units can be installed on a MekaSuit Helmet to remove the need to manually eat. When installed, the MekaSuit helmet will have an internal storage for ${link(NUTRITIONAL_PASTE, "Nutritional Paste")} (128,000mb).\$(p)Max units per helmet: 1.")
            MODULES[Modules.RADIATION_SHIELDING_UNIT]?.invoke("Radiation Shielding Units will allow your MekaSuit armor piece to protect you from radiation, similar to the ${link(HAZMAT_GOWN, "Hazmat Gown")}.  \$(br)Note that once you have been exposed to deadly doses of radiation without protection and began taking damage, wearing MekaSuit armor pieces with Radiation Shielding Units installed will \$(bold)not\$() prevent you from taking damage. Instead, it will only prevent the increase of radiation dosage.\$(br)In addition, all of your MekaSuit armor pieces must have a Radiation Shielding Unit installed for full protection against radiation.\$(p)Max units per armor piece: 1.")
            MODULES[Modules.SILK_TOUCH_UNIT]?.invoke("Silk Touch Units, when installed and enabled, will \"enchant\" your Meka-Tool with Silk Touch. \$(br)If you are unfamiliar with what Silk Touch does, it basically makes the mined blocks drop themsleves instead of their mined counterparts (e.g. stone will drop stone instead of cobblestone when mined).")
            MODULES[Modules.SOLAR_RECHARGING_UNIT]?.invoke("Solar Recharging Units will charge your${link(MEKASUIT_BODYARMOR, "MekaSuit")} during direct exposure to sunlight. Blocks such as glass will not impede the charging process. \$(p)Max units per helmet: 8.")
            MODULES[Modules.TELEPORTATION_UNIT]?.invoke("Teleportation Units allow you to teleport similar to an ender pearl, except that it will be instant. There is a limited range and teleportation will consume power.\$(p)Max units per Meka-Tool: 1.")
            MODULES[Modules.VEIN_MINING_UNIT]?.invoke("Vein Mining Units will allow you to mine ore veins and cut down trees with usually just one break of a block.\$(br)Extended Mode will vein mine any blocks rather than just logs and ores.\$(p)Max units per Meka-Tool: 4.")
            MODULES[Modules.VISION_ENHANCEMENT_UNIT]?.invoke("Vision Enhancement Units will improve your night vision.  You can press \$(k:key.mekanism.head_mode) to toggle it on or off without going into the Module Tweaker.\$(p)Max units per helmet: 4.")
        }
        GuideCategory.ITEMS_METAL_AND_ORE {
            name = "Metals & Ores"
            description = "Ore/Metal processing based materials."
            icon = PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)

            entry(BRONZE_INGOT) {
                name = "Bronze"
                readByDefault = true
                +"Bronze is an alloy of Copper and Tin."
                spotlight(BRONZE_INGOT)
                spotlight(BRONZE_DUST)
                spotlight(BRONZE_NUGGET)
            }

            CHARCOAL_DUST("Crushed form of Charcoal")
            COAL_DUST("Crushed form of Coal")
            DIAMOND_DUST("Crushed form of Diamond")
            EMERALD_DUST("Crushed form of Emerald")
            OBSIDIAN_DUST("Crushed form of Obsidian")
            QUARTZ_DUST("Crushed form of Quartz")
            LAPIS_LAZULI_DUST("Crushed form of Lapis Lazuli")

            LITHIUM_DUST("Crystallized form of ${link(LITHIUM, "Lithium")}")

            entry(REFINED_GLOWSTONE_INGOT) {
                name = "Refined Glowstone"
                readByDefault = true
                +"Refined glowstone is a stronger form of Glowstone, reinforced with Osmium in the ${link(OSMIUM_COMPRESSOR, "Osmium Compressor")}."
                spotlight(REFINED_GLOWSTONE_INGOT)
                spotlight(REFINED_GLOWSTONE_NUGGET)
                spotlight(REFINED_GLOWSTONE_BLOCK)
            }

            entry(REFINED_OBSIDIAN_INGOT) {
                name = "Refined Obsidian"
                readByDefault = true
                +"Harder obsidian? Unpossible!$(p)Obsidian reinforced with Osmium in the ${link(OSMIUM_COMPRESSOR, "Osmium Compressor")}. Can be used to form a Nether Portal"

                spotlight(REFINED_OBSIDIAN_DUST)
                spotlight(REFINED_OBSIDIAN_INGOT)
                spotlight(REFINED_OBSIDIAN_NUGGET)
                spotlight(REFINED_OBSIDIAN_BLOCK)
            }

            entry(STEEL_INGOT) {
                name = "Steel"
                readByDefault = true
                +"Steel is a hardened metal used in most Mekanism constructions."
                spotlight(ENRICHED_IRON, "Intermediate step in Mekanism Steel production.")
                spotlight(STEEL_INGOT)
                spotlight(STEEL_DUST)
                spotlight(STEEL_NUGGET)
                spotlight(STEEL_BLOCK)
            }

            SULFUR_DUST("Solidified sulfur, can be used to make ${link(SULFURIC_ACID, "Sulfuric Acid")}.")

            entry(PROCESSED_RESOURCES.get(ResourceType.CLUMP, PrimaryResource.IRON)!!) {
                name = "Clumps"
                readByDefault = true
                +"Clumps are part of the ${link(GuideEntry.ORE_TRIPLING, "3x Ore Processing")} pipeline and above."
                PROCESSED_RESOURCES.row(ResourceType.CLUMP).values.forEach(this::spotlight)
            }

            entry(PROCESSED_RESOURCES.get(ResourceType.SHARD, PrimaryResource.IRON)!!) {
                name = "Crystals"
                readByDefault = true
                +"Crystals are part of the ${link(GuideEntry.ORE_QUADRUPLING, "4x Ore Processing")} pipeline and above."
                PROCESSED_RESOURCES.row(ResourceType.SHARD).values.forEach(this::spotlight)
            }

            entry(PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, PrimaryResource.IRON)!!) {
                name = "Crystals"
                readByDefault = true
                +"Crystals are part of the ${link(GuideEntry.ORE_QUINTUPLING, "5x Ore Processing")} pipeline and above."
                PROCESSED_RESOURCES.row(ResourceType.CRYSTAL).values.forEach(this::spotlight)
            }

            DIRTY_NETHERITE_SCRAP("Dirty Netherite Scraps are part of the ore processing of Ancient Debris.")

            entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)) {
                name = "Osmium"
                readByDefault = true
                +"Osmium is a hard, brittle, bluish-white transition metal in the platinum group that is found as a trace element in alloys, mostly in platinum ores.$(p)Osmium is the densest stable element; it is approximately twice as dense as lead and slightly denser than iridium."
                spotlight(ORES[OreType.OSMIUM]!!)
                PROCESSED_RESOURCES.column(PrimaryResource.OSMIUM).values.forEach(this::spotlight)
            }

            entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.COPPER)) {
                name = "Copper"
                readByDefault = true
                +"Copper is a soft, malleable, and ductile metal with very high thermal and electrical conductivity. A freshly exposed surface of pure copper has a pinkish-orange color."
                spotlight(ORES[OreType.COPPER]!!)
                PROCESSED_RESOURCES.column(PrimaryResource.COPPER).values.forEach(this::spotlight)
            }

            entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)) {
                name = "Tin"
                readByDefault = true
                +"Tin is a silvery metal that characteristically has a faint yellow hue. Tin, like indium, is soft enough to be cut without much force."
                spotlight(ORES[OreType.TIN]!!)
                PROCESSED_RESOURCES.column(PrimaryResource.TIN).values.forEach(this::spotlight)
            }

            entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)) {
                name = "Uranium"
                readByDefault = true
                +"Uranium is a silvery-grey metal in the actinide series of the periodic table. A uranium atom has 92 protons and 92 electrons, of which 6 are valence electrons. Uranium is weakly radioactive because all isotopes of uranium are unstable; the half-lives of its naturally occurring isotopes range between 159,200 years and 4.5 billion years."
                spotlight(ORES[OreType.URANIUM]!!)
                PROCESSED_RESOURCES.column(PrimaryResource.URANIUM).values.forEach(this::spotlight)
            }

            entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD)) {
                name = "Lead"
                readByDefault = true
                +"Lead is a heavy metal that is denser than most common materials. Lead is soft and malleable, and also has a relatively low melting point."
                spotlight(ORES[OreType.URANIUM]!!)
                PROCESSED_RESOURCES.column(PrimaryResource.URANIUM).values.forEach(this::spotlight)
            }

            entry(FLUORITE_GEM) {
                name = "Fluorite"
                readByDefault = true
                +"Fluorite (also called fluorspar) is the mineral form of calcium fluoride, CaF2. It belongs to the halide minerals group."
                spotlight(ORES[OreType.FLUORITE]!!)
                spotlight(FLUORITE_DUST)
                spotlight(FLUORITE_GEM)
            }

        }
        GuideCategory.ITEMS_UPGRADES {
            name = "Upgrades"
            description = "You gotta pump up them numbers, rookie. Increase various abilities of machines with these items.$(br)Insert via the machine's GUI, Upgrades tab."
            icon = SPEED_UPGRADE

            SPEED_UPGRADE("An upgrade to increate the running speed of a machine.") {
                +"Note that every speed upgrade makes the machine 33% faster, and the Power Usage increases with ~77% (33²%), which makes for an increase in power usage for each operation with 33%.$(p)$(bold)The machine must have enough buffer to run one operation or it will not run at all."
            }
            ENERGY_UPGRADE("Upgrades the energy buffer of a machine and reduces its per-operation consumption.$(p)The ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator")} only receives a buffer increase.")
            FILTER_UPGRADE("The Filter Upgrade is an upgrade that, when used in the ${link(ELECTRIC_PUMP, "Electric Pump")}, allows the Electric Pump to produce Heavy Water.")
            MUFFLING_UPGRADE("Reduces the sound produced by a machine.")
            GAS_UPGRADE("The gas upgrade allows you to increase the gas usage efficiency of a Mekanism machine which consumes gas.")
            ANCHOR_UPGRADE("The Anchor Upgrade is a machine upgrade which keeps the chunk of the machine to which it is applied loaded. This is helpful for machines like the Digital Miner and Teleporter which must be in loaded chunks to function properly.$(p)$(bold)Compatible machines$()$(li)${link(DIGITAL_MINER, "Digital Miner")}$(li)${link(QUANTUM_ENTANGLOPORTER, "Quantum Entangloporter")}$(li)${link(GuideEntry.TELEPORTER, "Teleporter")}")
        }

        GuideEntry.ALLOYS {
            name = "Alloys"
            icon = INFUSED_ALLOY
            +"Crafting components used to make tiered items. Can also be right clicked on Logistical Transporters, Mechanical Pipes, Pressurized Tubes, Thermodynamic Conductors, and Universal Cables to upgrade tiers in-world.$(p)Created in a ${link(METALLURGIC_INFUSER, "Metallurgic Infuser")}."
            spotlight(INFUSED_ALLOY, "Redstone infused")
            spotlight(REINFORCED_ALLOY, "Diamond infused")
            spotlight(ATOMIC_ALLOY, "Refined Obsidian infused")
        }

        GuideEntry.CIRCUITS {
            name = "Circuits"
            icon = BASIC_CONTROL_CIRCUIT
            +"Crafting components used to make tiered items. Created in the ${link(METALLURGIC_INFUSER, "Metallurgic Infuser")}."
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
        CONFIGURATION_CARD("An item used to copy configuration data from one machine to another.$(p)To copy data to the card, $(k:sneak) + right click on the source machine, then right click the destination machine. Chat messages will inform you of the success/failure") {
            +"Supported machines: \$(li)${link(DIGITAL_MINER, "Digital Miner")} \$(li)${link(GuideEntry.ENERGY_CUBES, "Energy Cubes")} \$(li)${link(FORMULAIC_ASSEMBLICATOR, "Formulaic Assemblicator")} \$(li)${link(LOGISTICAL_SORTER, "Logistical Sorter")} \$(li)${link(OREDICTIONIFICATOR, "Oredictionificator")} \$(li)and any machine with configurable sides."
        }
        CRAFTING_FORMULA("Used in the ${link(FORMULAIC_ASSEMBLICATOR, "Formulaic Assemblicator")} to encode a crafting recipe for automatic operation.") {
            text {
                title = "Usage"
                text = "Open up a Formualaic Assemblicator's GUI. Put in the crafting recipe of the item you want to craft. Insert a Crafting Formula into that blank spot to the left of the crafting grid and press Encode.$(p)Now the recipe has been encoded into the Crafting Formula, and if you insert the Crafting Formula into any Formulaic Assemblicator, "
            }
            +"the recipe will appear in the crafting grid. $(p)$(k:sneak) + $(k:use) the Crafting Formula in the air to clear the encoded recipe."
        }
        ELECTROLYTIC_CORE("Crafting ingredient used for electrolysis.")
        ENERGY_TABLET("The Energy Tablet is chiefly an a crafting component and a can be used directly as a battery. Charge will be retained when used as a crafting ingredient.")
        GuideEntry.ENRICHED_INFUSION {
            name = "Enriched Infusion"
            icon = ENRICHED_REDSTONE
            +"Infusion ingredients can be enriched in the ${link(ENRICHMENT_CHAMBER, "Enrichment Chamber")} to provide more infusion amount in the ${link(METALLURGIC_INFUSER, "Metallurgic Infuser")}."
            spotlight(ENRICHED_REDSTONE)
            spotlight(ENRICHED_DIAMOND)
            spotlight(ENRICHED_OBSIDIAN)
            spotlight(ENRICHED_CARBON)
            spotlight(ENRICHED_GOLD)
            spotlight(ENRICHED_TIN)
        }
        entry(HDPE_SHEET) {
            name = "HDPE"
            +"High Density PolyEthylene is used to make plastic."
            spotlight(HDPE_PELLET, "First stage of HDPE production")
            spotlight(HDPE_SHEET, "A sheet of plastic.")
            spotlight(HDPE_ROD, "A rod of plastic")
            spotlight(HDPE_STICK, "It's a stick.")
        }
    }// end items category
}