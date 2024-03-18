package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

private val mapper = ObjectMapper(
    JsonFactoryBuilder().enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS).build()
        .enable(JsonParser.Feature.ALLOW_COMMENTS)
).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

fun JsonGenerator.writeFurnaceRecipes(furnace: JsonNode, validItems: Set<String>) {
    val furnaceName = furnace["objectName"].asText()
    val requiredPower = furnace["isn_requiredPower"].asInt()
    val durationMultiplier = 1.0 / furnace["fu_timer"].asDouble()
    val extraProductionChance = furnace["fu_extraProductionChance"]?.asDouble() ?: 0.5
    val inputsToOutputs = furnace["inputsToOutputs"].fields().asSequence().associate { (k, v) -> k to v.asText() }
    val bonusOutputs = furnace["bonusOutputs"].fields().asSequence().associate { (k, v) ->
        k to v.fields().asSequence()
            .map { (output, chance) ->
                Item(
                    output,
                    extraProductionChance * durationMultiplier * chance.asInt() / 100.0
                )
            }.toList()
    }

    ((inputsToOutputs.keys union bonusOutputs.keys) intersect validItems).forEach { item ->
        val recipeName = "$furnaceName\\$item"
        val inputs = listOf(Item("Watt", requiredPower), Item(item, 2 * durationMultiplier))
        val normalOutput = inputsToOutputs[item]?.let { Item(it, durationMultiplier) }
        val bonusOutput = bonusOutputs[item]
        val outputs = if (normalOutput == null) {
            bonusOutput!!.map { Item(it.item, (it.count + 1) * durationMultiplier) }
        } else bonusOutput?.let { it.map { it * durationMultiplier } + normalOutput } ?: listOf(normalOutput)

        writeObjectFieldStart(recipeName)

        writeArrayFieldStart("input")
        inputs.forEach(::writeObject)
        writeEndArray()

        writeArrayFieldStart("output")
        outputs.forEach(::writeObject)
        writeEndArray()

        writeArrayFieldStart("groups")
        writeString(furnaceName)
        writeEndArray()

        writeEndObject()
    }
}

fun JsonGenerator.writeLiquidMixerRecipes(liquidMixer: JsonNode) {
    for (recipe in liquidMixer) {
        val inputs = mapper.convertValue<Map<String, Int>>(
            recipe["inputs"],
            mapper.typeFactory.constructMapType(
                Map::class.java,
                String::class.java,
                Int::class.java
            )
        )
            .map { (item, count) -> Item(item, count * 6) }
            .takeIf { it.isNotEmpty() } ?: continue

        val outputs = mapper.convertValue<Map<String, Int>>(
            recipe["outputs"],
            mapper.typeFactory.constructMapType(
                Map::class.java,
                String::class.java,
                Int::class.java
            )
        )
            .map { (item, count) -> Item(item, count * 6) }
            .takeIf { it.isNotEmpty() } ?: continue

        writeObjectFieldStart("fu_liquidmixer\\${inputs.joinToString(transform = Item::item)}")

        writeArrayFieldStart("input")
        inputs.forEach(::writeObject)
        writeObject(Item("Watt", 120))
        writeEndArray()

        writeArrayFieldStart("output")
        outputs.forEach(::writeObject)
        writeEndArray()

        writeArrayFieldStart("groups")
        writeString("fu_liquidmixer")
        writeEndArray()

        writeEndObject()
    }
}

fun JsonGenerator.writeCentrifugeRecipes(centrifuge: JsonNode) {
    val itemMaps = centrifuge["recipeTypes"].flatMapTo(sortedSetOf()) { it.map(JsonNode::asText) }

    itemMaps.forEach { itemMap ->
        val centrifuges = CentrifugeInfo.getCentrifugesFor(itemMap)
        val recipes = centrifuge[itemMap]
        centrifuges.forEach { centrifuge ->
            recipes.fields().forEach { (input, outputs) ->
                writeObjectFieldStart("${centrifuge.name.lowercase()}\\$input")

                writeArrayFieldStart("input")
                writeObject(Item(input, 1.0 / centrifuge.craftDelay))
                writeObject(Item("Watt", centrifuge.requiredPower))
                writeEndArray()

                writeArrayFieldStart("output")
                outputs.fields().asSequence().forEach { (item, rarity) ->
                    writeObject(
                        Item(item, centrifuge[rarity[0].asText()] / (rarity[1].asInt() * centrifuge.craftDelay))
                    )
                }
                writeEndArray()

                writeArrayFieldStart("groups")
                writeString(centrifuge.name.lowercase())
                writeEndArray()

                writeEndObject()
            }
        }
    }
}

fun JsonGenerator.writeAtmosphericCondenserRecipes(obj: JsonNode) {
    val condenserName = obj["objectName"].asText()
    val namedWeights = obj["namedWeights"]!!
    val totalWeight = namedWeights.sumOf { it.asDouble() }
    val outputs = obj["outputs"]
    val requiredPower = obj["isn_requiredPower"].asInt()
    val productionTime = obj["productionTime"].asInt() / 60.0

    fun writePool(pool: JsonNode) {
        if (pool.isArray) pool
            .flatMap {
                val weight = namedWeights[it["weight"].asText()].asDouble()
                val items = it["items"]
                items.map { item -> Item(item.asText(), weight / (totalWeight * items.size() * productionTime)) }
            }
            .mergeItems()
            .forEach(::writeObject)
        else writePool(outputs[pool.asText()])
    }

    for (name in (outputs as ObjectNode).fieldNames()) {
        writeObjectFieldStart("$condenserName\\$name")

        writeArrayFieldStart("input")
        writeObject(Item("Watt", requiredPower / productionTime))
        writeEndArray()

        writeArrayFieldStart("output")
        writePool(outputs[name])
        writeEndArray()

        writeArrayFieldStart("groups")
        writeString(condenserName)
        writeString(name)
        writeEndArray()

        writeEndObject()
    }
}

fun JsonGenerator.writeExtractorRecipes(extractionConfig: JsonNode, baseGroup: String, validItems: Collection<String>) {
    for (extraction in extractionConfig) {
        val inputs =
            extraction["inputs"].fields().asSequence()
                .map { (k, v) -> Item(k, v.asInt()) }
                .toList()
                .takeIf { it.isNotEmpty() } ?: continue
        if (inputs.any { it.item !in validItems }) continue
        val outputs = extraction["outputs"].fields().asSequence()
            .associate { (item, counts) ->
                item to mapper.convertValue(counts, ExtractionQuantity::class.java)
            }
            .takeIf { it.isNotEmpty() } ?: continue
        outputs.values.flatMapTo(sortedSetOf()) { it.getUniqueTiers() }.forEach { tier ->
            writeObjectField(
                "$baseGroup\\${inputs.joinToString { it.item }}\\$tier",
                Recipe(
                    inputs,
                    outputs.map { (item, quantity) -> Item(item, quantity[tier]) },
                    listOf(baseGroup, tier.toString())
                )
            )
        }
    }
}

fun JsonGenerator.writeReactorRecipes(reactor: JsonNode) {
    val reactorName = reactor["objectName"].asText()
    val fuels = reactor["fuels"]?.fields()
        ?.asSequence()?.toList()
        ?.takeIf { it.isNotEmpty() } ?: return
    reactor["coolant"]?.fields()?.asSequence()
        ?.forEach { (coolant, coolantInfo) ->
            fuels.forEach { (fuel, fuelInfo) ->
                val fuelConsumptionRate = 1 / fuelInfo["decayRate"].asDouble()
                val inputs = listOf(
                    Item(coolant, 1 / coolantInfo["decayRate"].asDouble()),
                    Item(fuel, fuelConsumptionRate)
                )
                val outputs =
                    listOf(Item("Watt", fuelInfo["power"].asDouble()), Item("toxicwaste", fuelConsumptionRate))
                writeObjectField(
                    "$reactorName\\$fuel\\$coolant",
                    Recipe(inputs, outputs, listOf(reactorName, coolant))
                )
            }
        } ?: fuels.forEach { (fuel, fuelInfo) ->
        val fuelConsumptionRate = 1 / fuelInfo["decayRate"].asDouble()
        val inputs = listOf(Item(fuel, fuelConsumptionRate))
        val outputs =
            listOf(Item("Watt", fuelInfo["power"].asDouble()), Item("toxicwaste", fuelConsumptionRate))
        writeObjectField("$reactorName\\$fuel", Recipe(inputs, outputs, listOf(reactorName)))
    }
}

fun JsonGenerator.writeFissionReactorRecipes(reactor: JsonNode) {
    val bonusWasteChance = (reactor["bonusWasteChance"]?.asInt() ?: 50) / 100.0
    val reactorName = reactor["objectName"].asText()
    reactor["fuels"].fields().asSequence().forEach { (fuel, fuelInfo) ->
        val consumptionRate = 1 / fuelInfo["decayRate"].asDouble()
        writeObjectField(
            "$reactorName\\$fuel", Recipe(
                listOf(Item(fuel, consumptionRate)),
                listOf(
                    Item("Watt", fuelInfo["power"].asInt()),
                    Item("toxicwaste", consumptionRate * (1 + bonusWasteChance)),
                    Item("tritium", consumptionRate * bonusWasteChance)
                ),
                listOf(reactorName)
            )
        )
    }
}

fun JsonGenerator.writeGeneratorRecipes(generator: JsonNode) {
    val reactorName = generator["objectName"].asText()
    val maxPower = generator["heat"].asSequence().map { heatInfo ->
        heatInfo["minheat"].asInt() to heatInfo["power"].asInt()
    }.maxByOrNull { it.first }!!.second
    generator["acceptablefuel"].fields().asSequence()
        .forEach { (fuel, burnTime) ->
            val consumptionRate = 1.0 / burnTime.asInt()
            writeObjectField(
                "$reactorName\\$fuel",
                Recipe(listOf(Item(fuel, consumptionRate)), listOf(Item("Watt", maxPower)), listOf(reactorName))
            )
        }
}

suspend fun JsonGenerator.writeContainerLootRecipes(
    container: String,
    treasureChests: Map<String, List<ChestPool>>,
    treasurePools: Map<String, Set<TreasurePool>>,
) {
    val poolsFoundInContainer =
        treasureChests.values.filter { pools -> pools.any { container in it.containers } }.flatten()
    val treasurePoolsFoundInContainer =
        poolsFoundInContainer.map { treasurePools[it.treasurePool] ?: emptySet() }.filterNot(Collection<TreasurePool>::isEmpty)

    val requiredDifficulties: Set<Double> =
        treasurePoolsFoundInContainer.flatMapTo(sortedSetOf(Comparator.reverseOrder())) { it.map(TreasurePool::requiredDifficulty) }

    for (difficulty in requiredDifficulties) {
        writeObjectFieldStart("exploration\\$container\\$difficulty")

        writeArrayFieldStart("input")
        writeEndArray()

        writeArrayFieldStart("output")
        treasurePoolsFoundInContainer.flatMap { it.evaluateAtDifficulty(difficulty, treasurePools) }.mergeItems()
            .forEach(::writeObject)
        writeEndArray()

        writeArrayFieldStart("groups")
        writeString("exploration")
        writeString(container)
        writeEndArray()

        writeEndObject()
    }
}