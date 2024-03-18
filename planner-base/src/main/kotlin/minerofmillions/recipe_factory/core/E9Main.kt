package minerofmillions.recipe_factory.core

import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.ojalgo.scalar.RationalNumber

fun main(): Unit = runBlocking {
    val calc = Calculator.Builder().recipes(
        Recipe(
            "#forge:gasoline -> immersivepetroleum:gasoline",
            listOf("immersivepetroleum:gasoline" * 1),
            listOf("#forge:gasoline" * 1)
        ), Recipe(
            "#forge:gasoline -> pneumaticcraft:gasoline",
            listOf("pneumaticcraft:gasoline" * 1),
            listOf("#forge:gasoline" * 1)
        ), Recipe(
            "immersivepetroleum:refinery/gasoline",
            listOf("immersivepetroleum:naphtha" * 7, "immersivepetroleum:gasoline_additives" * 3),
            listOf("immersivepetroleum:gasoline" * 10)
        ), Recipe(
            "immersivepetroleum:distillationtower/kerosene", listOf("#forge:kerosene" * 10), listOf(
                "immersivepetroleum:diesel_sulfur" * 5,
                "immersivepetroleum:gasoline_additives" * 3,
                "immersivepetroleum:naphtha" * 2
            )
        ), Recipe(
            "immersivepetroleum:distillationtower/crude_oil", listOf("#forge:crude_oil" * 50), listOf(
                "immersivepetroleum:lubricant" * 12,
                "immersivepetroleum:diesel_sulfur" * 30,
                "immersivepetroleum:kerosene" * 18,
                "immersivepetroleum:naphtha" * 17,
                "thermal:bitumen" * RationalNumber.of(7, 100)
            )
        ), Recipe(
            "pneumaticcraft:thermo_plant/kerosene",
            listOf("#forge:diesel" * 100),
            listOf("pneumaticcraft:kerosene" * 80)
        ), Recipe(
            "pneumaticcraft:refinery/oil_4", listOf("#forge:crude_oil" * 10), listOf(
                "pneumaticcraft:diesel" * 2,
                "pneumaticcraft:kerosene" * 3,
                "pneumaticcraft:gasoline" * 3,
                "pneumaticcraft:lpg" * 2
            )
        ), Recipe(
            "pneumaticcraft:refinery/oil_3",
            listOf("#forge:crude_oil" * 10),
            listOf("pneumaticcraft:diesel" * 2, "pneumaticcraft:kerosene" * 3, "pneumaticcraft:lpg" * 2)
        ), Recipe(
            "pneumaticcraft:refinery/oil_2",
            listOf("#forge:crude_oil" * 10),
            listOf("pneumaticcraft:diesel" * 4, "pneumaticcraft:lpg" * 2)
        ), Recipe(
            "immersivepetroleum:hydrotreater/sulfur_recovery",
            listOf("water" * 5, "immersivepetroleum:diesel_sulfur" * 10),
            listOf("immersivepetroleum:diesel" * 10, "immersiveengineering:dust_sulfur" * RationalNumber.of(1, 20))
        ), Recipe(
            "#forge:kerosene -> pneumaticcraft:kerosene",
            listOf("pneumaticcraft:kerosene" * 1),
            listOf("#forge:kerosene" * 1)
        ), Recipe(
            "#forge:kerosene -> immersivepetroleum:kerosene",
            listOf("immersivepetroleum:kerosene" * 1),
            listOf("#forge:kerosene" * 1)
        ), Recipe(
            "#forge:diesel -> pneumaticcraft:diesel", listOf("pneumaticcraft:diesel" * 1), listOf("#forge:diesel" * 1)
        ), Recipe(
            "#forge:diesel -> immersivepetroleum:diesel",
            listOf("immersivepetroleum:diesel" * 1),
            listOf("#forge:diesel" * 1)
        ), Recipe(
            "#forge:lubricant -> pneumaticcraft:lubricant",
            listOf("pneumaticcraft:lubricant" * 1),
            listOf("#forge:lubricant" * 1)
        ), Recipe(
            "#forge:lubricant -> immersivepetroleum:lubricant",
            listOf("immersivepetroleum:lubricant" * 1),
            listOf("#forge:lubricant" * 1)
        ), Recipe(
            "immersivepetroleum:distillationtower/lubricant_cracking",
            listOf("immersivepetroleum:lubricant_cracked" * 12),
            listOf("immersivepetroleum:diesel_sulfur" * 10, "immersivepetroleum:kerosene" * 6)
        ), Recipe(
            "immersivepetroleum:hydrotreater/lubricant_cracking",
            listOf("water" * 5, "#forge:lubricant" * 24),
            listOf("immersivepetroleum:lubricant_cracked" * 24, "productivebees:wax" * RationalNumber.of(1, 50))
        ), Recipe("Blaze Burning: Diesel", listOf("#forge:diesel" * 1000), listOf("Superheat" * 40)),
        Recipe("Blaze Burning: Gasoline", listOf("#forge:gasoline" * 1000), listOf("Superheat" * 40)),
        Recipe(
            "immersivepetroleum:coking/petcoke",
            listOf("water" * 125, "#forge:bitumen" * 2),
            listOf("immersivepetroleum:diesel_sulfur" * 27, "immersivepetroleum:petcoke" * 2)
        ), Recipe(
            "#forge:bitumen -> thermal:bitumen",
            listOf("thermal:bitumen" * 1),
            listOf("#forge:bitumen" * 1)
        )
    ).build(Calculator.CalculatorType.OJALGO_ALL_AT_ONCE)

    calc.solve(listOf("Superheat" * 1200)).last().print()
}