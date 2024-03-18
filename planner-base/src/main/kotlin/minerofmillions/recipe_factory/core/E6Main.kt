package minerofmillions.recipe_factory.core

import kotlinx.coroutines.flow.last
import org.ojalgo.scalar.RationalNumber
import org.ojalgo.scalar.RationalNumber.of

data class Honeycomb(
    val name: String,
    val primaryResult: ItemStack,
    val secondaryResult: ItemStack,
    val honeyType: String,
    val honeyChance: RationalNumber,
) {
    val recipe = Recipe(
        "centrifuge/combs/$name",
        listOf("resourcefulbees:${name}_honeycomb" * 1, "minecraft:glass_bottle" * honeyChance),
        listOf(primaryResult, secondaryResult, honeyType * honeyChance)
    )
}

suspend fun main() {
    val hasCrushing = false
    val hasMilling = false
    val calculator = Calculator.Builder().recipes(sequence {
        val metalByproducts = mapOf(
            "iron" to "nickel",
            "nickel" to "iron",
            "gold" to "zinc",
            "copper" to "gold",
            "aluminum" to "iron",
            "lead" to "silver",
            "silver" to "lead",
            "uranium" to "lead",
            "osmium" to "tin",
            "tin" to "osmium",
            "zinc" to "gold",
            "nebu" to "uranium",
            "cobalt" to "iron",
            "cloggrum" to "froststeel",
            "froststeel" to "cobalt",
            "regalium" to "utherium",
            "utherium" to "regalium",
            "thallasium" to "thallasium",
            "iesnium" to "silver",
            "elementium" to "elementium"
        )
        val combs = listOf(
            Honeycomb(
                "catnip",
                "minecraft:cat_spawn_egg" * of(1, 100),
                "resourcefulbees:wax" * of(1, 2),
                "resourcefulbees:catnip_honey_bottle",
                of(1, 10)
            ),
            Honeycomb(
                "brass",
                "#forge:dusts/brass" * of(1, 2),
                "#forge:nuggets/copper" * of(3, 10),
                "resourcefulbees:brass_honey_bottle",
                of(1, 10)
            ),
            Honeycomb(
                "bronze",
                "#forge:dusts/bronze" * of(1, 2),
                "#forge:nuggets/tin" * of(3, 10),
                "resourcefulbees:bronze_honey_bottle",
                of(1, 100)
            ),
            Honeycomb(
                "constantan",
                "#forge:dusts/constantan" * of(3, 10),
                "#forge:nuggets/copper" * of(3, 10),
                "resourcefulbees:constantan_honey_bottle",
                of(1, 100)
            ),
            Honeycomb(
                "electrum",
                "#forge:dusts/electrum" * of(1, 2),
                "#forge:nuggets/silver" * of(3, 10),
                "resourcefulbees:electrum_honey_bottle",
                of(1, 100)
            ),
            Honeycomb(
                "enderium",
                "#forge:dusts/enderium" * of(3, 10),
                "#forge:dusts/diamond" * of(1, 20),
                "resourcefulbees:enderium_honey_bottle",
                of(1, 50)
            ),
            Honeycomb(
                "invar",
                "#forge:dusts/invar" * of(1, 2),
                "#forge:nuggets/nickel" * of(3, 10),
                "resourcefulbees:invar_honey_bottle",
                of(1, 50)
            ),
            Honeycomb(
                "starry",
                "resourcefulbees:starry_honey" * of(1, 2000),
                "resourcefulbees:wax" * of(1, 2),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "diamond",
                "#forge:ores/diamond" * of(1, 5),
                "#forge:ores/mana" * of(1, 10),
                "resourcefulbees:diamond_honey_bottle",
                of(1, 100)
            ),
            Honeycomb(
                "emerald",
                "#forge:ores/emerald" * of(1, 2),
                "minecraft:prismarine_shard" * of(3, 10),
                "resourcefulbees:emerald_honey_bottle",
                of(1, 100)
            ),
            Honeycomb(
                "lapis",
                "#forge:ores/lapis" * of(1, 2),
                "#forge:ores/apatite" * of(1, 4),
                "resourcefulbees:lapis_honey_bottle",
                of(1, 50)
            ),
            Honeycomb(
                "redstone",
                "#forge:ores/redstone" * of(1, 2),
                "#forge:ores/cinnabar" * of(3, 10),
                "resourcefulbees:redstone_honey_bottle",
                of(1, 50)
            ),
            Honeycomb(
                "bloody",
                "bloodmagic:slate_ampoule" * of(1, 10),
                "bloodmagic:blankslate" * of(3, 100),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "clay", "minecraft:clay" * 1, "minecraft:terracotta" * of(1, 10), "minecraft:honey_bottle", of(1, 4)
            ),
            Honeycomb(
                "enderslime",
                "tconstruct:ender_slime_ball" * of(12, 5),
                "refinedstorage:processor_binding" * of(1, 50),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "gravel",
                "minecraft:gravel" * of(9, 5),
                "minecraft:flint" * of(3, 10),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "ichor",
                "tconstruct:ichor_slime_ball" * of(12, 5),
                "refinedstorage:processor_binding" * of(1, 50),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "shepherd",
                "minecraft:mutton" * of(1, 2),
                "resourcefulbees:wax" * of(3, 2),
                "minecraft:honey_bottle",
                of(1, 2)
            ),
            Honeycomb(
                "skyslime",
                "tconstruct:sky_slime_ball" * of(12, 5),
                "refinedstorage:processor_binding" * of(1, 50),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "aluminum",
                "#forge:ores/aluminum" * of(1, 2),
                "#forge:nuggets/iron" * of(3, 10),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "cobalt",
                "#forge:ores/cobalt" * of(1, 2),
                "#forge:nuggets/manyullyn" * of(3, 10),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "copper",
                "#forge:ores/copper" * of(1, 2),
                "#forge:nuggets/gold" * of(3, 10),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "frosty",
                "#forge:ores/froststeel" * of(1, 2),
                "thermal:blizz_powder" * of(1, 5),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "gold",
                "#forge:ores/gold" * of(1, 2),
                "#forge:nuggets/copper" * of(3, 10),
                "resourcefulbees:gold_honey_bottle",
                of(1, 50)
            ),
            Honeycomb(
                "iron",
                "#forge:ores/iron" * of(1, 2),
                "#forge:nuggets/nickel" * of(3, 10),
                "resourcefulbees:iron_honey_bottle",
                of(1, 50)
            ),
            Honeycomb(
                "lead",
                "#forge:ores/lead" * of(1, 2),
                "#forge:nuggets/silver" * of(3, 10),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "nickel",
                "#forge:ores/nickel" * of(1, 2),
                "#forge:nuggets/iron" * of(3, 10),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "osmium",
                "#forge:ores/osmium" * of(1, 2),
                "#forge:nuggets/tin" * of(3, 10),
                "minecraft:honey_bottle",
                of(1, 4)
            ),
            Honeycomb(
                "regal",
                "#forge:ores/regalium" * of(2, 5),
                "#forge:ores/utherium" * of(3, 5),
                "minecraft:honey_bottle",
                of(2, 5)
            ),
        )
        val metals = listOf(
            "iron",
            "gold",
            "copper",
            "aluminum",
            "silver",
            "lead",
            "nickel",
            "uranium",
            "osmium",
            "tin",
            "zinc",
            "cobalt",
            "cloggrum",
            "froststeel",
            "utherium",
            "regalium",
            "iesnium",
            "thallasium",
            "terminite",
            "nebu",
            "neptunium",
            "manasteel",
            "terrasteel",
            "elementium",
            "refined_obsidian",
            "refined_glowstone",
            "bronze",
            "brass",
            "constantan",
            "electrum",
            "steel",
            "invar",
            "signalum",
            "lumium",
            "enderium",
            "alfsteel",
            "forgotten",
            "pewter",
            "arcane_gold",
            "slimesteel",
            "tinkers_bronze",
            "rose_gold",
            "pig_iron",
            "queens_slime",
            "manyullyn",
            "hepatizon",
            "fairy"
        )
        metals.forEach {
            yield(
                Recipe(
                    "crafting/ingots/${it}_from_nuggets",
                    listOf("#forge:nuggets/$it" * 9),
                    listOf("#forge:ingots/$it" * 1)
                )
            )
            yield(
                Recipe("crafting/nuggets/$it", listOf("#forge:ingots/$it" * 1), listOf("#forge:nuggets/$it" * 9))
            )
            yield(
                Recipe(
                    "crafting/storage_blocks/$it",
                    listOf("#forge:ingots/$it" * 9),
                    listOf("#forge:storage_blocks/$it" * 1)
                )
            )
            yield(
                Recipe(
                    "crafting/ingots/${it}_from_block",
                    listOf("#forge:storage_blocks/$it" * 1),
                    listOf("#forge:ingots/$it" * 9)
                )
            )
        }
        metalByproducts.forEach { (base, byproduct) ->
            yield(Recipe("smelting/ores/${base}", listOf("#forge:ores/$base" * 1), listOf("#forge:ingots/$base" * 1)))
            if (hasCrushing || hasMilling) yield(
                Recipe(
                    "smelting/crushed_ores/${base}",
                    listOf("#forge:crushed_ores/$base" * 1),
                    listOf("#forge:ingots/$base" * 1)
                )
            )
            if (hasCrushing) yield(
                Recipe(
                    "crushing/ores/${base}", listOf("#forge:ores/$base" * 1), listOf(
                        "#forge:crushed_ores/$base" * 1,
                        "#forge:crushed_ores/$base" * of(12, 10),
                        "#forge:crushed_ores/$byproduct" * of(1, 5),
                        "minecraft:cobblestone" * of(1, 8)
                    )
                )
            )
            if (hasMilling) {
                yield(
                    Recipe(
                        "milling/ores/${base}", listOf("#forge:ores/$base" * 1), listOf(
                            "#forge:crushed_ores/$base" * 1,
                            "#forge:crushed_ores/$base" * of(1, 2),
                            "#forge:crushed_ores/$byproduct" * of(1, 10)
                        )
                    )
                )
                yield(
                    Recipe(
                        "washing/crushed_ores/$base",
                        listOf("#forge:crushed_ores/$base" * 1),
                        listOf("#forge:nuggets/$base" * 10, "#forge:nuggets/$base" * of(5, 2))
                    )
                )
            }
        }
//        yieldAll(combs.map(Honeycomb::recipe))
        val typesToMerge = listOf("storage_blocks", "ingots", "nuggets", "gears", "dusts", "plates", "rods")
        mapOf(
            "gold" to listOf("invar", "brass", "copper", "bronze", "tin", "diamond", "silver"),
            "iron" to listOf("lead", "copper", "tin", "aluminum", "osmium", "brass", "invar"),
        ).forEach { (first, seconds) ->
            seconds.forEach { second ->
                val mergedMaterial = "${first}_$second"
                yieldAll(typesToMerge.map { type ->
                    Recipe(
                        "$type/$mergedMaterial: $first",
                        listOf("#forge:$type/$first" * 1),
                        listOf("#forge:$type/$mergedMaterial" * 1)
                    )
                })
                yieldAll(typesToMerge.map { type ->
                    Recipe(
                        "$type/$mergedMaterial: $second",
                        listOf("#forge:$type/$second" * 1),
                        listOf("#forge:$type/$mergedMaterial" * 1)
                    )
                })
            }
        }
        yield(
            Recipe(
                "refinedstorage:quartz_enriched_iron",
                listOf("#forge:ingots/iron" * 3, "#forge:gems/quartz" * 1),
                listOf("refinedstorage:quartz_enriched_iron" * 4)
            )
        )
        yield(
            Recipe(
                "washing/gravel",
                listOf("minecraft:gravel" * 1),
                listOf("minecraft:flint" * of(1, 4), "#forge:nuggets/iron" * of(1, 8))
            )
        )
        yield(
            Recipe(
                "milling/cobblestone", listOf("minecraft:cobblestone" * 1), listOf("minecraft:gravel" * 1)
            )
        )
        yield(
            Recipe(
                "Pressure Chamber 3", listOf(
                    "pneumaticcraft:pressure_chamber_wall" * 23,
                    "pneumaticcraft:pressure_chamber_valve" * 1,
                    "pneumaticcraft:pressure_chamber_interface" * 2
                ), listOf("Pressure Chamber 3" * 1)
            )
        )
        yield(
            Recipe(
                "pneumaticcraft:pressure_chamber_valve_x1",
                listOf("pneumaticcraft:pressure_tube" * 1, "pneumaticcraft:pressure_chamber_wall" * 1),
                listOf("pneumaticcraft:pressure_chamber_valve" * 1)
            )
        )
        yield(
            Recipe(
                "pneumaticcraft:pressure_chamber_valve_x4",
                listOf("pneumaticcraft:pressure_tube" * 1, "pneumaticcraft:pressure_chamber_wall" * 4),
                listOf("pneumaticcraft:pressure_chamber_valve" * 4)
            )
        )
        yield(
            Recipe(
                "pneumaticcraft:pressure_chamber_valve",
                listOf("pneumaticcraft:pressure_tube" * 1, "#forge:ingots/compressed_iron" * 8),
                listOf("pneumaticcraft:pressure_chamber_valve" * 16)
            )
        )
        yield(
            Recipe(
                "pneumaticcraft:pressure_chamber_interface",
                listOf("minecraft:hopper" * 1, "pneumaticcraft:pressure_chamber_wall" * 2),
                listOf("pneumaticcraft:pressure_chamber_interface" * 2)
            )
        )
        yield(
            Recipe(
                "pneumaticcraft:pressure_chamber_wall",
                listOf("pneumaticcraft:reinforced_bricks" * 8),
                listOf("pneumaticcraft:pressure_chamber_wall" * 16)
            )
        )
        yield(
            Recipe(
                "pneumaticcraft:reinforced_bricks",
                listOf("pneumaticcraft:reinforced_stone" * 4),
                listOf("pneumaticcraft:reinforced_bricks" * 4)
            )
        )
        yield(
            Recipe(
                "pneumaticcraft:reinforced_stone",
                listOf("#forge:stone" * 8, "#forge:ingots/compressed_iron" * 1),
                listOf("pneumaticcraft:reinforced_stone" * 8)
            )
        )
        yield(
            Recipe(
                "pneumaticcraft:compressed_iron",
                listOf("#forge:ingots/iron" * 1),
                listOf("#forge:ingots/compressed_iron" * of(4, 5))
            )
        )
        yield(
            Recipe(
                "pneumaticcraft:pressure_tube",
                listOf("#forge:ingots/compressed_iron" * 2, "#forge:glass" * 1),
                listOf("pneumaticcraft:pressure_tube" * 8)
            )
        )
    }).build(Calculator.CalculatorType.OJALGO_ALL_AT_ONCE)

    calculator.solve("Pressure Chamber 3" * 1, isOneoff = true, startingProducts = listOf("#forge:ingots/iron" * 61)).last().print()
}