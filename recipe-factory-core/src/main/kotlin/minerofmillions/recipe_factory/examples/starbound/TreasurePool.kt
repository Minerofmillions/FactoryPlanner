package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import kotlinx.coroutines.Deferred

@JsonDeserialize(using = TreasurePool.Deserializer::class)
data class TreasurePool(
    val requiredDifficulty: Double,
    val fill: List<TreasurePoolEntry>,
    val pool: List<TreasurePoolEntry>,
    val poolRounds: List<PoolRoundInfo>,
    val allowDuplication: Boolean,
) {
    override fun toString() = buildString {
        append("%.3f %b".format(requiredDifficulty, allowDuplication))
        if (fill.isNotEmpty()) append(" f=${fill.joinToString()}")
        if (pool.isNotEmpty()) append(" p=${pool.joinToString()} * ${poolRounds.joinToString()}")
    }

    object Deserializer : StdDeserializer<TreasurePool>(TreasurePool::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TreasurePool =
            p.codec.readTree<JsonNode>(p).let { json ->
                val poolData = json[1]
                val rounds = poolData["poolRounds"]?.let { rounds ->
                    if (rounds is ArrayNode) rounds.map { p.codec.treeToValue(it, PoolRoundInfo::class.java) }
                    else listOf(PoolRoundInfo(1.0, rounds.asInt()))
                } ?: listOf(PoolRoundInfo(1.0, 1))
                TreasurePool(
                    json[0].asDouble(),
                    poolData["fill"]?.map { p.codec.treeToValue(it, TreasurePoolEntry::class.java) } ?: emptyList(),
                    poolData["pool"]?.map { p.codec.treeToValue(it, TreasurePoolEntry::class.java) } ?: emptyList(),
                    rounds,
                    poolData["allowDuplication"]?.asBoolean() ?: true
                )
            }
    }
}

fun Collection<PoolRoundInfo>.averageRounds() =
    totalWeight().let { totalWeight -> sumOf { it.numRolls * it.weight / totalWeight } }

suspend fun Collection<TreasurePool>.evaluateAtDifficulty(
    difficulty: Double,
    pools: Deferred<Map<String, Set<TreasurePool>>>
) = evaluateAtDifficulty(difficulty, pools.await())

fun Collection<TreasurePool>.evaluateAtDifficulty(
    difficulty: Double,
    pools: Map<String, Set<TreasurePool>>,
): List<Item> {
    val usedPool = firstOrNull { it.requiredDifficulty <= difficulty } ?: return emptyList()

    val fillLoot = usedPool.fill.flatMap { entry ->
        when (entry) {
            is TreasurePoolItem -> listOf(Item(entry.item, entry.count))
            is TreasurePoolPool -> pools[entry.pool]!!.evaluateAtDifficulty(difficulty, pools)
        }
    }

    val poolWeight = usedPool.pool.totalWeight()
    val poolRounds = usedPool.poolRounds.averageRounds()
    val poolLoot = usedPool.pool.flatMap { entry ->
        when (entry) {
            is TreasurePoolItem -> listOf(Item(entry.item, entry.count * poolRounds * entry.weight / poolWeight))
            is TreasurePoolPool -> pools[entry.pool]?.evaluateAtDifficulty(difficulty, pools)
                ?.map { it * (poolRounds * entry.weight / poolWeight) } ?: emptyList()
        }
    }
    return (fillLoot + poolLoot).mergeItems()
}

fun Collection<TreasurePool>.getDifficulties(): Collection<Double> = mapTo(sortedSetOf()) { it.requiredDifficulty }

@JsonDeserialize(using = TreasurePoolEntry.Deserializer::class)
sealed class TreasurePoolEntry(override val weight: Double): WeightedEntry {
    object Deserializer : StdDeserializer<TreasurePoolEntry>(TreasurePoolEntry::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TreasurePoolEntry =
            p.codec.readTree<JsonNode>(p).let { json ->
                val item = json["item"]
                val weight = json["weight"]?.asDouble() ?: 1.0
                if (item != null) {
                    if (item.isArray) TreasurePoolItem(weight, item[0].asText(), item[1]?.asInt() ?: 1)
                    else TreasurePoolItem(weight, item.asText(), 1)
                } else TreasurePoolPool(weight, json["pool"].asText())
            }
    }
}

class TreasurePoolItem(weight: Double, val item: String, val count: Int) : TreasurePoolEntry(weight) {
    override fun toString() = "%.3f (%s * %d)".format(weight, item, count)
}

class TreasurePoolPool(weight: Double, val pool: String) : TreasurePoolEntry(weight) {
    override fun toString() = "%.3f (pool: %s)".format(weight, pool)
}

data class PoolRoundInfo(override val weight: Double, val numRolls: Int): WeightedEntry {
    override fun toString() = "%.3f * %d".format(weight, numRolls)

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(json: JsonNode) : this(json[0].asDouble(), json[1].asInt())
}