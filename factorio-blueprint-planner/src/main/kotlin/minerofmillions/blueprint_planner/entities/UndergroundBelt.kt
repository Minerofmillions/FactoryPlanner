package minerofmillions.blueprint_planner.entities

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UndergroundBelt @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("speed") val speed: Double,
    @JsonProperty("max_distance") val maxDistance: Int,
    @JsonProperty("next_upgrade") val nextUpgrade: String?
){
}