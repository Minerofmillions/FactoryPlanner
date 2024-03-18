package minerofmillions.recipe_factory.examples.minecraft

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class Tag @JsonCreator constructor(@JsonProperty("values") val values: Set<String>)
