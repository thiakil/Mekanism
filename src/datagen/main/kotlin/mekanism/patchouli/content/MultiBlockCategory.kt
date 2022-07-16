package mekanism.patchouli.content

import mekanism.common.MekanismLang
import mekanism.common.registries.MekanismBlocks.*
import mekanism.common.registries.MekanismGases.*
import mekanism.patchouli.GuideCategory
import mekanism.patchouli.GuideEntry
import mekanism.patchouli.dsl.PatchouliBook
import mekanism.patchouli.dsl.link
import net.minecraft.core.Direction

fun PatchouliBook.multiblocks() {
    GuideCategory.MULTIBLOCKS {
        name = "Multiblocks"
        description = "Structures formed using multiple blocks."
        icon = THERMAL_EVAPORATION_CONTROLLER

        GuideEntry.THERMAL_EVAP {
            name = "Thermal Evaporation Plant"
            icon = THERMAL_EVAPORATION_CONTROLLER
            +"The Thermal Evaporation Plant is a 4x4 base multiblock for producing one liquid from another by way of heat energy. Minimum height is 3, maximum is 18.$(p)Heat can be supplied passively, actively by solar, or externally supplied."

            multiblock {
                name = "Minimal Plant"
                definition {
                    layer {
                        row { space();  +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; space(); }
                        row { +THERMAL_EVAPORATION_BLOCK; space();                    space();                    +THERMAL_EVAPORATION_BLOCK }
                        row { +THERMAL_EVAPORATION_BLOCK; space();                    space();                    +THERMAL_EVAPORATION_BLOCK }
                        row { space();  +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; space(); }
                    }
                    layer {
                        row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                        row { +THERMAL_EVAPORATION_BLOCK; space();                    space();                    +THERMAL_EVAPORATION_BLOCK }
                        row { +THERMAL_EVAPORATION_BLOCK; space();                    space();                    THERMAL_EVAPORATION_CONTROLLER facing Direction.EAST }
                        row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                    }
                    layer {
                        row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                        row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                        row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; center(THERMAL_EVAPORATION_BLOCK); +THERMAL_EVAPORATION_BLOCK }
                        row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                    }
                }
            }
            +THERMAL_EVAPORATION_CONTROLLER
            +THERMAL_EVAPORATION_VALVE
            +THERMAL_EVAPORATION_BLOCK
        }
        GuideEntry.DYNAMIC_TANK {
            name = "Dynamic Tank"
            icon = DYNAMIC_VALVE
            +"The blocks Dynamic Tank, Dynamic Glass, and Dynamic Valve make up the multi-block that is the Dynamic Tank, a fluid storage structure that can hold a large amount of a single type of fluid.$(p)Dynamic Tanks can be made in any size from 3x3x3 to 18x18x18, and does not need to be a cube."
            +"A valid Dynamic Tank structure will flash with \"active redstone\" particles upon completion.$(p)Notes:$(li)All of the Dynamic Tank's borders must be made out of Dynamic Tank (not glass or valve)$(li)The tank's length, width, and height can be any number within the size limits - e.g. 3x4x5"
            multiblock {
                name = "A 3x3x4 Dynamic Tank"
                definition {
                    layer {
                        row { +DYNAMIC_TANK; +DYNAMIC_TANK; +DYNAMIC_TANK; }
                        row { +DYNAMIC_TANK; +DYNAMIC_TANK; +DYNAMIC_TANK; }
                        row { +DYNAMIC_TANK; +DYNAMIC_TANK; +DYNAMIC_TANK; }
                    }
                    layer {
                        row { +DYNAMIC_TANK; +DYNAMIC_TANK; +DYNAMIC_TANK; }
                        row { +DYNAMIC_TANK; space(); +DYNAMIC_TANK; }
                        row { +DYNAMIC_TANK; +DYNAMIC_TANK; +DYNAMIC_TANK; }
                    }
                    layer {
                        row { +DYNAMIC_TANK; +DYNAMIC_VALVE; +DYNAMIC_TANK; }
                        row { +DYNAMIC_TANK; space(); +DYNAMIC_TANK; }
                        row { +DYNAMIC_TANK; +DYNAMIC_VALVE; +DYNAMIC_TANK; }
                    }
                    layer {
                        row { +DYNAMIC_TANK; +DYNAMIC_TANK; +DYNAMIC_TANK; }
                        row { +DYNAMIC_TANK; +DYNAMIC_TANK; +DYNAMIC_TANK; }
                        row { +DYNAMIC_TANK; center(DYNAMIC_TANK); +DYNAMIC_TANK; }
                    }
                }
            }
            multiblock {
                name = "A 10x10x10 Dynamic Tank, with some Structural Glass"
                definition {
                    layer {//10 rows of 10 tank casing
                        for (row in 1..10){
                            row {
                                for (col in 1..10) {
                                    +DYNAMIC_TANK
                                }
                            }
                        }
                    }
                    for (layer in 1..8){
                        layer {
                            row {
                                +DYNAMIC_TANK
                                for (glass in 1..8) { +STRUCTURAL_GLASS }
                                +DYNAMIC_TANK
                            }
                            for (row in 1..8) {
                                row {
                                    +STRUCTURAL_GLASS
                                    for (blank in 1..8) { space() }
                                    +STRUCTURAL_GLASS
                                }
                            }
                            row {
                                +DYNAMIC_TANK
                                for (glass in 1..8) { +STRUCTURAL_GLASS }
                                +DYNAMIC_TANK
                            }
                        }
                    }
                    layer {//9 rows of 10 tank casing
                        for (row in 1..9){
                            row {
                                for (col in 1..10) {
                                    +DYNAMIC_TANK
                                }
                            }
                        }
                        row {
                            for (col in 1..9) {
                                +DYNAMIC_TANK
                            }
                            center(DYNAMIC_TANK)
                        }
                    }
                }
            }
            +DYNAMIC_TANK
            +DYNAMIC_VALVE
        }
        GuideEntry.TELEPORTER {
            name = "Teleporter"
            icon = TELEPORTER
            +TELEPORTER_FRAME
        }
        GuideEntry.INDUCTION {
            name = "Induction Matrix"
            icon = BASIC_INDUCTION_CELL
            +INDUCTION_CASING
            +INDUCTION_PORT
            +BASIC_INDUCTION_CELL
            +ADVANCED_INDUCTION_CELL
            +ELITE_INDUCTION_CELL
            +ULTIMATE_INDUCTION_CELL
            +BASIC_INDUCTION_PROVIDER
            +ADVANCED_INDUCTION_PROVIDER
            +ELITE_INDUCTION_PROVIDER
            +ULTIMATE_INDUCTION_PROVIDER

            +"The induction matrix is a multiblock used to store potentially huge amounts of power.  It can be any size from 3x3x3 to 18x18x18.  The edges and corners must be made of ${link(INDUCTION_CASING, "Induction Casing")}, while the sides can be made of casing and or ${link(STRUCTURAL_GLASS, "structural glass")}."
            +"The inside of the matrix is filled with ${link(GuideEntry.INDUCTION_CELL, "Induction Cells")} and ${link(GuideEntry.INDUCTION_PROVIDER, "Induction Providers")}, which provide power storage and transfer, respectively.  Note that not every block in the matrix must be filled, so you can build a matrix bigger than you currently need and upgrade it as required.  ${link(INDUCTION_PORT, "induction ports")} are used for input and output, and can be placed anywhere on any side."

            multiblock {
                name = "A basic 5x5x5 induction matrix, with all glass sides"
                definition {
                    //Structural glass ceiling + floor
                    layer {
                        row {
                            for (casing in 1..5) { +INDUCTION_CASING }
                        }
                        for (glassRow in 1..3) {
                            row {
                                +INDUCTION_CASING
                                +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                                +INDUCTION_CASING
                            }
                        }
                        row {
                            for (casing in 1..5) { +INDUCTION_CASING }
                        }
                    }
                    //Layer of providers
                    layer {
                        row {
                            +INDUCTION_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +INDUCTION_CASING
                        }
                        for (providerRow in 1..3) {
                            row {
                                +STRUCTURAL_GLASS
                                +BASIC_INDUCTION_PROVIDER; +BASIC_INDUCTION_PROVIDER; +BASIC_INDUCTION_PROVIDER
                                +STRUCTURAL_GLASS
                            }
                        }
                        row {
                            +INDUCTION_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +INDUCTION_CASING
                        }
                    }
                    //First layer of cells - with ports in the sides
                    layer {
                        row {
                            +INDUCTION_CASING
                            +STRUCTURAL_GLASS
                            +INDUCTION_PORT
                            +STRUCTURAL_GLASS
                            +INDUCTION_CASING
                        }
                        for (cellRow in 1..3) {
                            row {
                                +STRUCTURAL_GLASS
                                +BASIC_INDUCTION_CELL; +BASIC_INDUCTION_CELL; +BASIC_INDUCTION_CELL
                                +STRUCTURAL_GLASS
                            }
                        }
                        row {
                            +INDUCTION_CASING
                            +STRUCTURAL_GLASS
                            +INDUCTION_PORT
                            +STRUCTURAL_GLASS
                            +INDUCTION_CASING
                        }
                    }
                    //Second layer of cells
                    layer {
                        row {
                            +INDUCTION_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +INDUCTION_CASING
                        }
                        for (cellRow in 1..3) {
                            row {
                                +STRUCTURAL_GLASS
                                +BASIC_INDUCTION_CELL; +BASIC_INDUCTION_CELL; +BASIC_INDUCTION_CELL
                                +STRUCTURAL_GLASS
                            }
                        }
                        row {
                            +INDUCTION_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +INDUCTION_CASING
                        }
                    }
                    layer {
                        row {
                            for (casing in 1..5) { +INDUCTION_CASING }
                        }
                        for (glassRow in 1..3) {
                            row {
                                +INDUCTION_CASING
                                +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                                +INDUCTION_CASING
                            }
                        }
                        row {
                            for (casing in 1..4) { +INDUCTION_CASING }
                            center(INDUCTION_CASING)
                        }
                    }
                }
            }
        }
        GuideEntry.BOILER {
            name = MekanismLang.BOILER.translationKey
            icon = BOILER_VALVE
            +SUPERHEATING_ELEMENT
            +PRESSURE_DISPERSER
            +BOILER_CASING
            +BOILER_VALVE
        }
        GuideEntry.SPS {
            name = "Supercritical Phase Shifter (SPS)"
            icon = SPS_CASING

            +"The SPS is a late-game multiblock that uses large amounts of energy to convert ${link(POLONIUM, "polonium")} into ${link(ANTIMATTER, "antimatter")}."
            +"The SPS is made of ${link(SPS_CASING, "SPS Casing")}, ${link(STRUCTURAL_GLASS, "Structural Glass (technically optional)")}, ${link(SPS_PORT, "SPS Port")}, and ${link(SUPERCHARGED_COIL, "Supercharged Coil")}.  It has a unique rounded cuboid-like structure.  The faces can be made of casing and/or glass, but using as much glass as possible is recommended since casing is expensive."
            +"Energy is inputted into ports with coils placed behind them, inside the multiblock.  Follow this template, and then add the two extra ports for polonium input and antimatter output (on any face), as well as more energy port/coils as needed."

            multiblock {
                name = "Basic SPS with one port and coil"
                definition {
                    layer {
                        row { +SPS_CASING; +SPS_CASING; +SPS_CASING }
                        row {
                            +SPS_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_CASING
                        }
                        for (fullRow in 1..3) {
                            row {
                                +SPS_CASING
                                for (glass in 1..5) { +STRUCTURAL_GLASS }
                                +SPS_CASING
                            }
                        }
                        row {
                            +SPS_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_CASING
                        }
                        row { +SPS_CASING; +SPS_CASING; +SPS_CASING }
                    }
                    layer {
                        row {
                            +SPS_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_CASING
                        }
                        row {
                            +SPS_CASING
                            for (space in 1..5) { space() }
                            +SPS_CASING
                        }
                        for (glassRow in 1..3) {
                            row {
                                +STRUCTURAL_GLASS
                                for (space in 1..5) { space() }
                                +STRUCTURAL_GLASS
                            }
                        }
                        row {
                            +SPS_CASING
                            for (space in 1..5) { space() }
                            +SPS_CASING
                        }
                        row {
                            +SPS_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_CASING
                        }
                    }
                    layer {
                        row {
                            +SPS_CASING
                            for (glass in 1..5) { +STRUCTURAL_GLASS }
                            +SPS_CASING
                        }
                        for (innerRow in 1..5) {
                            row {
                                +STRUCTURAL_GLASS
                                for (space in 1..5) { space() }
                                +STRUCTURAL_GLASS
                            }
                        }
                        row {
                            +SPS_CASING
                            for (glass in 1..5) { +STRUCTURAL_GLASS }
                            +SPS_CASING
                        }
                    }
                    //Middle, with port/coil
                    layer {
                        row {
                            +SPS_CASING
                            for (glass in 1..5) { +STRUCTURAL_GLASS }
                            +SPS_CASING
                        }
                        for (innerRow in 1..4) {
                            row {
                                +STRUCTURAL_GLASS
                                for (space in 1..5) { space() }
                                +STRUCTURAL_GLASS
                            }
                        }
                        row {
                            +STRUCTURAL_GLASS
                            space(); space()
                            +SUPERCHARGED_COIL
                            space(); space()
                            +STRUCTURAL_GLASS
                        }
                        row {
                            +SPS_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_PORT
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_CASING
                        }
                    }
                    layer {
                        row {
                            +SPS_CASING
                            for (glass in 1..5) { +STRUCTURAL_GLASS }
                            +SPS_CASING
                        }
                        for (innerRow in 1..5) {
                            row {
                                +STRUCTURAL_GLASS
                                for (space in 1..5) { space() }
                                +STRUCTURAL_GLASS
                            }
                        }
                        row {
                            +SPS_CASING
                            for (glass in 1..5) { +STRUCTURAL_GLASS }
                            +SPS_CASING
                        }
                    }
                    layer {
                        row {
                            +SPS_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_CASING
                        }
                        row {
                            +SPS_CASING
                            for (space in 1..5) { space() }
                            +SPS_CASING
                        }
                        for (glassRow in 1..3) {
                            row {
                                +STRUCTURAL_GLASS
                                for (space in 1..5) { space() }
                                +STRUCTURAL_GLASS
                            }
                        }
                        row {
                            +SPS_CASING
                            for (space in 1..5) { space() }
                            +SPS_CASING
                        }
                        row {
                            +SPS_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_CASING
                        }
                    }
                    layer {
                        row { +SPS_CASING; +SPS_CASING; +SPS_CASING }
                        row {
                            +SPS_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_CASING
                        }
                        for (fullRow in 1..3) {
                            row {
                                +SPS_CASING
                                for (glass in 1..5) { +STRUCTURAL_GLASS }
                                +SPS_CASING
                            }
                        }
                        row {
                            +SPS_CASING
                            +STRUCTURAL_GLASS; +STRUCTURAL_GLASS; +STRUCTURAL_GLASS
                            +SPS_CASING
                        }
                        row { +SPS_CASING; +SPS_CASING; +SPS_CASING }
                    }
                }
            }
        }
    }
}