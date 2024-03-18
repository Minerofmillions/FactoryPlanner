package minerofmillions.blueprint_planner.entities

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class TransportBelt @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("speed") val speed: Double,
    @JsonProperty("related_underground_belt") val relatedUndergroundBelt: String,
    @JsonProperty("next_upgrade") val nextUpgrade: String?
) {
    private val tilesPerSecond = speed * 60
    val throughput = tilesPerSecond * 8
}