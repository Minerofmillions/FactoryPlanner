package minerofmillions.recipe_factory.core

import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.ojalgo.scalar.RationalNumber

private data class FuelBase(
    val suffix: String,
    val fuelMain: String,
    val fuelBase: String,
    val leOutputs: List<ItemStack>,
    val heOutputs: List<ItemStack>,
)

fun main() {
    val calc = Calculator.Builder().recipes(sequence {
        listOf(
            FuelBase(
                "U-233",
                "Uranium-233",
                "Uranium-238",
                listOf("Plutonium-239" * 4, "Plutonium-241" * 4, "Plutonium-242" * 32, "Americium-243" * 24),
                listOf("Neptunium-236" * 32, "Neptunium-237" * 8, "Plutonium-242" * 16, "Americium-243" * 8)
            ),
            FuelBase(
                "U-235",
                "Uranium-235",
                "Uranium-238",
                listOf("Uranium-238" * 40, "Neptunium-237" * 8, "Plutonium-239" * 8, "Plutonium-241" * 8),
                listOf("Uranium-238" * 20, "Neptunium-237" * 16, "Plutonium-239" * 4, "Plutonium-242" * 24)
            ),
            FuelBase(
                "N-236",
                "Neptunium-236",
                "Neptunium-237",
                listOf("Neptunium-237" * 4, "Plutonium-242" * 32, "Americium-242" * 8, "Americium-243" * 20),
                listOf("Uranium-238" * 16, "Plutonium-238" * 8, "Plutonium-238" * 8, "Plutonium-242" * 32)
            ),
            FuelBase(
                "P-239",
                "Plutonium-239",
                "Plutonium-242",
                listOf("Plutonium-239" * 8, "Plutonium-242" * 24, "Curium-243" * 4, "Curium-246" * 28),
                listOf("Americium-241" * 8, "Americium-242" * 24, "Curium-245" * 8, "Curium-246" * 24)
            ),
            FuelBase(
                "P-241",
                "Plutonium-241",
                "Plutonium-242",
                listOf("Plutonium-242" * 4, "Americium-242" * 4, "Americium-243" * 8, "Curium-246" * 48),
                listOf("Americium-241" * 8, "Curium-245" * 8, "Curium-246" * 24, "Curium-247" * 24)
            ),
            FuelBase(
                "A-242",
                "Americium-242",
                "Americium-243",
                listOf("Curium-243" * 8, "Curium-245" * 8, "Curium-246" * 40, "Curium-247" * 8),
                listOf("Curium-245" * 16, "Curium-246" * 32, "Curium-247" * 8, "Berkelium-247" * 8)
            ),
            FuelBase(
                "Cm-243",
                "Curium-243",
                "Curium-246",
                listOf("Curium-246" * 32, "Berkelium-247" * 16, "Berkelium-248" * 8, "Californium-249" * 8),
                listOf("Curium-246" * 24, "Berkelium-247" * 24, "Berkelium-248" * 8, "Californium-249" * 8)
            ),
            FuelBase(
                "Cm-245",
                "Curium-245",
                "Curium-246",
                listOf("Berkelium-247" * 40, "Berkelium-248" * 8, "Californium-249" * 4, "Californium-252" * 12),
                listOf("Berkelium-247" * 48, "Berkelium-248" * 4, "Californium-249" * 4, "Californium-251" * 8)
            ),
            FuelBase(
                "Cm-247",
                "Curium-247",
                "Curium-246",
                listOf("Berkelium-247" * 20, "Berkelium-248" * 4, "Californium-251" * 8, "Californium-252" * 32),
                listOf("Berkelium-248" * 8, "Californium-249" * 8, "Californium-251" * 24, "Californium-252" * 24)
            ),
            FuelBase(
                "B-248",
                "Berkelium-248",
                "Berkelium-247",
                listOf("Californium-249" * 4, "Californium-251" * 4, "Californium-252" * 28, "Californium-252" * 28),
                listOf("Californium-249" * 8, "Californium-251" * 8, "Californium-252" * 24, "Californium-252" * 24)
            ),
            FuelBase(
                "Cf-249",
                "Californium-249",
                "Californium-252",
                listOf("Californium-250" * 16, "Californium-251" * 8, "Californium-252" * 20, "Californium-252" * 20),
                listOf("Californium-250" * 32, "Californium-251" * 16, "Californium-252" * 8, "Californium-252" * 8)
            ),
            FuelBase(
                "Cf-251",
                "Californium-251",
                "Californium-252",
                listOf("Californium-251" * 4, "Californium-252" * 60),
                listOf("Californium-251" * 16, "Californium-252" * 48),
            )
        ).forEach { fuel ->
            yield(
                Recipe(
                    "LE${fuel.suffix}",
                    listOf(fuel.fuelMain * 1, fuel.fuelBase * 8),
                    listOf("LE${fuel.suffix}" * 1)
                )
            )
            yield(
                Recipe(
                    "HE${fuel.suffix}",
                    listOf(fuel.fuelMain * 4, fuel.fuelBase * 5),
                    listOf("HE${fuel.suffix}" * 1)
                )
            )
            yield(
                Recipe(
                    "Reactor: LE${fuel.suffix}",
                    listOf("LE${fuel.suffix}" * 1),
                    fuel.leOutputs.map { it / 9 }
                )
            )
            yield(
                Recipe(
                    "Reactor: HE${fuel.suffix}",
                    listOf("HE${fuel.suffix}" * 1),
                    fuel.heOutputs.map { it / 9 }
                )
            )
        }
        yield(
            Recipe(
                "Reactor: TBU",
                listOf("TBU Fuel" * 1),
                listOf(
                    "Uranium-233" * RationalNumber.of(16, 9),
                    "Uranium-235" * RationalNumber.of(8, 9),
                    "Neptunium-236" * RationalNumber.of(8, 9),
                    "Neptunium-237" * RationalNumber.of(32, 9)
                )
            )
        )
        yield(Recipe("TBU", listOf("Thorium-232" * 9), listOf("TBU Fuel" * 1)))
        val oneNinth = RationalNumber.of(1, 9)
        yield(
            Recipe(
                "Separate: Thorium",
                listOf("Thorium Dust" * 1),
                listOf("Thorium-232" * 1, "Thorium-230" * oneNinth)
            )
        )
        yield(
            Recipe(
                "Separate: Uranium",
                listOf("Uranium Dust" * 1),
                listOf("Uranium-238" * 1, "Uranium-235" * oneNinth)
            )
        )
        yield(
            Recipe(
                "Separate: Plutonium",
                listOf("Plutonium Dust" * 1),
                listOf("Plutonium-239" * 1, "Plutonium-242" * oneNinth)
            )
        )
        mapOf(
            "Thorium-230" to "Lead",
            "Thorium-232" to "Lead",
            "Uranium-233" to "Bismuth",
            "Uranium-235" to "Lead",
            "Uranium-238" to "Thorium-230",
            "Neptunium-236" to "Thorium-232",
            "Neptunium-237" to "Uranium-233",
            "Plutonium-238" to "Thorium-230",
            "Plutonium-239" to "Uranium-235",
            "Plutonium-241" to "Neptunium-237",
            "Plutonium-242" to "Uranium-238",
            "Americium-241" to "Neptunium-237",
            "Americium-242" to "Thorium-230",
            "Americium-243" to "Plutonium-239",
            "Curium-243" to "Plutonium-239",
            "Curium-245" to "Plutonium-241",
            "Curium-246" to "Plutonium-242",
            "Curium-247" to "Americium-243",
            "Berkelium-247" to "Americium-243",
            "Berkelium-248" to "Thorium-232",
            "Californium-249" to "Curium-245",
            "Californium-250" to "Curium-246",
            "Californium-251" to "Curium-247",
            "Californium-252" to "Thorium-232"
        ).forEach { (input, output) ->
            yield(Recipe("Decay: $input", listOf(input * 1), listOf(output * 1)))
        }
    }).build(Calculator.CalculatorType.OJALGO_ALL_AT_ONCE)

    val solution = calc.solve("HECf-251" * 64)

    runBlocking {
        solution.last().print()
    }
}