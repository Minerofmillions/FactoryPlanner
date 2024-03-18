package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder("minimumLevel", "ores")
data class OreDistribution @JsonCreator constructor(
    @JsonProperty("minimumLevel") val minimumLevel: Double,
    @JsonProperty("ores") val ores: List<OreEntry>,
)

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder("oreName", "oreWeight")
data class OreEntry @JsonCreator constructor(
    @JsonProperty("oreName") val oreName: String,
    @JsonProperty("oreWeight") override val weight: Double,
): WeightedEntry {
    override fun toString(): String = "$weight * $oreName"
}

fun Collection<OreDistribution>.evaluateAtLevel(level: Double) =
    filter { it.minimumLevel <= level }.maxByOrNull(OreDistribution::minimumLevel)