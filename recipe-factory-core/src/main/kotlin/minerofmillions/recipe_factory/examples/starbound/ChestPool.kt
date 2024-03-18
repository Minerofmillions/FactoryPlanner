package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ChestPool @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @JsonProperty("containers") val containers: List<String>,
    @JsonProperty("treasurePool") val treasurePool: String,
    @JsonProperty("minimumLevel") val minimumLevel: Int,
)
