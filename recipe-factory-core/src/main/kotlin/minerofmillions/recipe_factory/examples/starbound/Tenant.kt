package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Tenant @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("priority") val priority: Int,
    @JsonProperty("colonyTagCriteria") val colonyTagCriteria: Map<String, Int>,
    @JsonProperty("tenants") val tenants: List<Map<String, Any>>,
    @JsonProperty("rent") val rent: Rent
) {
    data class Rent @JsonCreator constructor(@JsonProperty("periodRange") val periodRange: IntRange, @JsonProperty("pool") val pool: String)
}