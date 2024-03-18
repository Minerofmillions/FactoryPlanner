package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

data class ObjectPool @JsonCreator constructor(
    @JsonProperty("pool") val pool: List<ObjectPoolEntry>,
    @JsonProperty("parameters") val parameters: Map<String, Any>?,
)

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder("weight", "obj")
data class ObjectPoolEntry @JsonCreator constructor(
    @JsonProperty("weight") override val weight: Double,
    @JsonProperty("obj") val obj: String,
) : WeightedEntry