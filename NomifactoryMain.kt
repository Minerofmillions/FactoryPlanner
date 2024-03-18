package minerofmillions.recipe_factory.core

import kotlinx.coroutines.flow.last
import org.ojalgo.scalar.RationalNumber.of
import kotlin.math.pow

data class Material(
    val name: String,
    val isIngot: Boolean,
    val hasWire: Boolean = false,
    val cableCoatings: List<List<ItemStack>> = emptyList(),
    val hasFineWire: Boolean = false,
    val hasPlate: Boolean = false,
    val hasFoil: Boolean = false,
    val hasRod: Boolean = false,
    val hasBolt: Boolean = false,
    val hasFluid: Boolean = false,
) {
    val base = if (isIngot) "ingot_$name" else "gem_$name"
    val fineWire = "wire_fine_$name"
    val plate = "plate_$name"
    val rod = "rod_$name"
    val bolt = "bolt_$name"
    val screw = "screw_$name"
    val foil = "foil_$name"
    val fluid = "fluid_$name"

    fun wire(index: Int) =
        if (index in 0..4) "wire_${name}_${2.0.pow(index).toInt()}" else error("Invalid wire index: $index")

    fun cable(index: Int) =
        if (index in 0..4) "cable_${name}_${2.0.pow(index).toInt()}" else error("Invalid cable index: $index")

    fun cableCoatings(index: Int): List<List<ItemStack>> = when (index) {
        0, 1 -> 1
        2 -> 2
        3 -> 3
        4 -> 5
        else -> error("Invalid cable index: $index")
    }.let { multiplier ->
        cableCoatings.map {
            it.map {
                it * multiplier
            }
        }
    }
}

suspend fun main() {
    val materials = listOf(
        Material(
            "aluminium", isIngot = true, hasWire = true, cableCoatings = listOf(
                listOf("fluid_rubber" * of(144, 1000), "foil_polyvinyl_chloride" * 1),
                listOf("fluid_silicone_rubber" * of(72, 1000), "foil_polyvinyl_chloride" * 1),
                listOf("fluid_styrene_butadiene_rubber" * of(36, 1000), "foil_polyvinyl_chloride" * 1)
            ), hasFineWire = true, hasPlate = true, hasFoil = true, hasRod = true, hasBolt = true, hasFluid = true
        ), Material(
            "copper", isIngot = true, hasWire = true, cableCoatings = listOf(
                listOf("fluid_rubber" * of(144, 1000)),
                listOf("fluid_silicone_rubber" * of(72, 1000)),
                listOf("fluid_styrene_butadiene_rubber" * of(36, 1000))
            ), hasFineWire = true, hasPlate = true, hasFoil = true, hasRod = true, hasFluid = true
        ), Material(
            "red_alloy", isIngot = true, hasWire = true, cableCoatings = listOf(
                listOf("fluid_rubber" * of(144, 1000)),
                listOf("fluid_silicone_rubber" * of(72, 1000)),
                listOf("fluid_styrene_butadiene_rubber" * of(36, 1000))
            ), hasFineWire = true, hasPlate = true, hasFoil = true, hasRod = true, hasFluid = true, hasBolt = true
        ), Material(
            "soldering_alloy", isIngot = true, hasFluid = true
        ), Material(
            "annealed_copper", isIngot = true, hasFluid = true, hasWire = true, cableCoatings = listOf(
                listOf("fluid_rubber" * of(144, 1000)),
                listOf("fluid_silicone_rubber" * of(72, 1000)),
                listOf("fluid_styrene_butadiene_rubber" * of(36, 1000))
            ), hasFineWire = true, hasPlate = true, hasRod = true, hasBolt = true, hasFoil = true
        ), Material(
            "steel", isIngot = true, hasWire = true, cableCoatings = listOf(
                listOf("fluid_rubber" * of(144, 1000), "foil_polyvinyl_chloride" * 1),
                listOf("fluid_silicone_rubber" * of(72, 1000), "foil_polyvinyl_chloride" * 1),
                listOf("fluid_styrene_butadiene_rubber" * of(36, 1000), "foil_polyvinyl_chloride" * 1)
            ), hasFineWire = true, hasPlate = true, hasFoil = true, hasRod = true, hasBolt = true, hasFluid = true
        ), Material(
            "silver", isIngot = true, hasWire = true, cableCoatings = listOf(
                listOf("fluid_rubber" * of(144, 1000)),
                listOf("fluid_silicone_rubber" * of(72, 1000)),
                listOf("fluid_styrene_butadiene_rubber" * of(36, 1000))
            ), hasFineWire = true, hasPlate = true, hasFoil = true, hasRod = true, hasBolt = true, hasFluid = true
        )
    )
    val oreDict = mapOf(
        "circuitUlv" to listOf("Vacuum Tube"/*, "NAND Chip"*/),
        "circuitLv" to listOf("Electronic Circuit"/*, "Integrated Logic Circuit", "Microprocessor"*/),
        "circuitMv" to listOf("Good Electronic Circuit"/*, "Good Integrated Circuit", "Integrated Processor"*/),
        "componentDiode" to listOf("Diode" /*"SMD Diode"*/),
        "componentResistor" to listOf("Resistor" /*"SMD Resistor"*/),
        "plankWood" to listOf("Wood Plank", "Rubber Wood Planks"),
        "logWood" to listOf("Rubber Wood")
    )
    val calculator = Calculator.Builder().recipes(materials.asSequence().flatMap { material ->
        sequence {
            if (material.hasWire) {
                yield(
                    Recipe(
                        "wiremill:1:${material.base}", listOf(material.base * 1), listOf(material.wire(0) * 2)
                    )
                )
                yield(
                    Recipe(
                        "wiremill:2:${material.base}", listOf(material.base * 1), listOf(material.wire(1) * 1)
                    )
                )
                yield(
                    Recipe(
                        "wiremill:4:${material.base}", listOf(material.base * 2), listOf(material.wire(2) * 1)
                    )
                )
                yield(
                    Recipe(
                        "wiremill:8:${material.base}", listOf(material.base * 4), listOf(material.wire(3) * 1)
                    )
                )
                yield(
                    Recipe(
                        "wiremill:16:${material.base}", listOf(material.base * 8), listOf(material.wire(4) * 1)
                    )
                )
                (0..4).forEach { index ->
                    material.cableCoatings(index).forEach {
                        yield(
                            Recipe(
                                "assembler:${material.wire(index)}",
                                it + material.wire(index) * 1,
                                listOf(material.cable(index) * 1)
                            )
                        )
                    }
                }
            }
            if (material.hasFineWire) {
                yield(
                    Recipe(
                        "wiremill:1:${material.wire(0)}", listOf(material.wire(0) * 1), listOf(material.fineWire * 4)
                    )
                )
                yield(
                    Recipe(
                        "wiremill:3:${material.base}", listOf(material.base * 1), listOf(material.fineWire * 8)
                    )
                )
            }
            if (material.hasPlate) yield(
                Recipe(
                    "metal_bender:1:${material.base}", listOf(material.base * 1), listOf(material.plate * 1)
                )
            )
            if (material.hasFoil) {
                yield(
                    Recipe(
                        "metal_bender:10:${material.base}", listOf(material.base * 1), listOf(material.foil * 4)
                    )
                )
                yield(Recipe("metal_bender:1:${material.plate}", listOf(material.plate * 1), listOf(material.foil * 4)))
            }
            if (material.hasFluid) yield(
                Recipe(
                    "extractor:${material.base}", listOf(material.base * 1), listOf(material.fluid * of(144, 1000))
                )
            )
            if (material.hasRod) yield(
                Recipe(
                    "lathe:${material.base}", listOf(material.base * 1), listOf(material.rod * 2)
                )
            )
            if (material.hasBolt) {
                yield(
                    Recipe(
                        "cutting_saw:${material.rod}", listOf(material.rod * 1), listOf(material.bolt * 4)
                    )
                )
                yield(
                    Recipe(
                        "lathe:${material.bolt}", listOf(material.bolt * 1), listOf(material.screw * 1)
                    )
                )
            }
        }
    } + listOf(
        Recipe(
            "Electronic Circuit", listOf(
                "fluid_soldering_alloy" * of(72, 1000),
                "Circuit Board" * 1,
                "#componentResistor" * 2,
                "wire_red_alloy_1" * 2,
                "#circuitUlv" * 2
            ), listOf("Electronic Circuit" * 2)
        ), Recipe(
            "Vacuum Tube", listOf(
                "fluid_red_alloy" * of(18, 1000), "Glass Tube" * 1, "bolt_steel" * 1, "wire_annealed_copper_1" * 2
            ), listOf("Vacuum Tube" * 4)
        ), Recipe(
            "Good Electronic Circuit", listOf(
                "fluid_soldering_alloy" * of(72, 1000),
                "Good Circuit Board" * 1,
                "#circuitLv" * 2,
                "#componentDiode" * 2,
                "wire_copper_1" * 2
            ), listOf("Good Electronic Circuit" * 1)
        ), Recipe(
            "Diode (AsGa Glass)",
            listOf("fluid_glass" * of(144, 1000), "wire_fine_copper" * 4, "dust_small_gallium_arsenide" * 1),
            listOf("Diode" * 1)
        ), Recipe(
            "Diode (Annealed AsGa Glass)",
            listOf("fluid_glass" * of(144, 1000), "wire_fine_annealed_copper" * 4, "dust_small_gallium_arsenide" * 1),
            listOf("Diode" * 2)
        ), /*Recipe(
            "Diode (AsGa PE)",
            listOf("fluid_polyethylene" * of(144, 1000), "wire_fine_copper" * 4, "dust_small_gallium_arsenide" * 1),
            listOf("Diode" * 2)
        ), Recipe(
            "Diode (Annealed AsGa PE)",
            listOf("fluid_polyethylene" * of(144, 1000), "wire_fine_annealed_copper" * 4, "dust_small_gallium_arsenide" * 1),
            listOf("Diode" * 4)
        ), Recipe(
            "Diode (Wafer PE)",
            listOf("fluid_polyethylene" * of(144, 1000), "wire_fine_copper" * 4, "Silicon Wafer" * 1),
            listOf("Diode" * 2)
        ), Recipe(
            "Diode (Annealed Wafer PE)",
            listOf("fluid_polyethylene" * of(144, 1000), "wire_fine_annealed_copper" * 4, "Silicon Wafer" * 1),
            listOf("Diode" * 4)
        ), */
        Recipe(
            "Good Circuit Board",
            listOf("wire_silver_1" * 8, "Phenolic Substrate" * 1),
            listOf("Good Circuit Board" * 1)
        ), Recipe(
            "Good Circuit Board (Na2S2O8)",
            listOf("fluid_sodium_persulfate" * of(1, 5), "foil_silver" * 4, "Phenolic Substrate" * 1),
            listOf("Good Circuit Board" * 1)
        ), Recipe(
            "Good Circuit Board (FeCl3)",
            listOf("fluid_iron_iii_chloride" * of(1, 10), "foil_silver" * 4, "Phenolic Substrate" * 1),
            listOf("Good Circuit Board" * 1)
        ), Recipe(
            "chemical_reactor:1:hydrochloric_acid:dust_iron",
            listOf("fluid_hydrochloric_acid" * 3, "dust_iron" * 1),
            listOf("fluid_iron_iii_chloride" * 1, "fluid_hydrogen" * 3)
        ), Recipe(
            "chemical_reactor:1:sulfuric_acid:dust_salt",
            listOf("fluid_sulfuric_acid" * 1, "dust_salt" * 2),
            listOf("fluid_hydrochloric_acid" * 1, "dust_sodium_bisulfate" * 7)
        ), Recipe(
            "electrolyzer:dust_sodium_bisulfate",
            listOf("dust_sodium_bisulfate" * 7),
            listOf("fluid_sodium_persulfate" * of(1, 2), "fluid_hydrogen" * 1)
        ), Recipe(
            "arc_furnace:ingot_copper",
            listOf("ingot_copper" * 1, "fluid_oxygen" * of(63, 1000)),
            listOf("ingot_annealed_copper" * 1)
        ), Recipe(
            "fluid_solidifier:ball:fluid_glass",
            listOf("fluid_glass" * of(144, 1000)),
            listOf("Glass Tube" * 1)
        ), Recipe(
            "extractor:glass_block",
            listOf("Glass" * 1),
            listOf("fluid_glass" * of(144, 1000))
        ), Recipe(
            "chemical_reactor:phenolic_substrate",
            listOf("fluid_phenol" * of(1, 10), "Coated Circuit Board" * 1),
            listOf("Phenolic Substrate" * 1)
        ), Recipe(
            "assembler:circuit_board",
            listOf("fluid_glue" * of(1, 10), "foil_copper" * 4, "Wood Plank" * 1),
            listOf("Circuit Board" * 1)
        ), Recipe(
            "Coated Circuit Board",
            listOf("Sticky Resin" * 6, "#plankWood" * 3),
            listOf("Coated Circuit Board" * 3)
        ), Recipe(
            "compressor:dust_wood",
            listOf("dust_wood" * 1),
            listOf("Wood Plank" * 1)
        ), Recipe(
            "pulverizer:log_wood",
            listOf("#logWood" * 1),
            listOf("dust_wood" * 6)
        ), Recipe(
            "cutting_saw:water:rubber_wood",
            listOf("fluid_water" * of(4, 1000), "Rubber Wood" * 1),
            listOf("Rubber Wood Planks" * 6, "dust_wood" * 2)
        ), Recipe(
            "pulverizer:plankWood",
            listOf("#plankWood" * 1),
            listOf("dust_wood" * 1)
        ), Recipe(
            "centrifuge:sticky_resin",
            listOf("Sticky Resin" * 1),
            listOf("fluid_glue" * of(1, 10), "dust_raw_rubber" * 3, "Plant Ball" * of(1, 10))
        )
    ) + oreDict.flatMap { (entry, items) ->
        items.map {
            Recipe(
                "#$entry: $it",
                listOf(it * 1),
                listOf("#$entry" * 1)
            )
        }
    }).build(Calculator.CalculatorType.OJALGO_ALL_AT_ONCE)

    calculator.solve(
        "#circuitUlv" * 128,
        "#circuitLv" * 128,
        "#circuitMv" * 128,
        isOneoff = true,
        startingProducts = listOf(
            "ingot_red_alloy" * 60,
            "Glass Tube" * 2,
            "Phenolic Substrate" * 31,
            "Vacuum Tube" * 29,
            "fluid_soldering_alloy" * of(1296, 1000)
        )
    ).last().print()
}