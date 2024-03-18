package minerofmillions.blueprint_planner.entities

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import minerofmillions.blueprint_planner.RootComponent

data class AssemblingMachine @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("crafting_categories") val craftingCategories: List<String>,
    @JsonProperty("crafting_speed") val craftingSpeed: Double,
    @JsonProperty("energy_source") val energySource: JsonNode,
    @JsonProperty("energy_usage") val energyUsage: String,
    @JsonProperty("se_allow_in_space") val allowedInSpace: Boolean,
) {
    private val _fluidBoxes = MutableValue(emptyList<FluidBox>())
    val fluidBoxes: Value<List<FluidBox>> = _fluidBoxes
    var fluidBoxesOffWhenNoFluidRecipe = false
        private set

    @JsonProperty("fluid_boxes")
    fun setFluidBoxes(boxes: JsonNode) {
        if (boxes.isArray) {
            _fluidBoxes.value = boxes.map { RootComponent.mapper.treeToValue(it, FluidBox::class.java) }
        } else {
            fluidBoxesOffWhenNoFluidRecipe = boxes["off_when_no_fluid_recipe"]?.asBoolean() ?: false
            val validKeys = boxes.fieldNames().asSequence().mapNotNull { it.toIntOrNull() }.toList()
            _fluidBoxes.value = List(validKeys.max()) {
                RootComponent.mapper.treeToValue(boxes[(it + 1).toString()], FluidBox::class.java)!!
            }
        }
    }

    data class FluidBox @JsonCreator constructor(
        @JsonProperty("production_type") val productionType: ProductionType,
        @JsonProperty("pipe_connections") val pipeConnections: List<PipeConnection>
    )

    data class PipeConnection @JsonCreator constructor(
        @JsonProperty("type") val type: ConnectionType?,
        @JsonProperty("position") @JsonDeserialize(using = Location.Companion::class) val position: Location
    ) {
        override fun toString(): String = buildString {
            append(position)
            if (type != null) append(": ", type)
        }
    }

    enum class ProductionType {
        @JsonAlias("input")
        INPUT,

        @JsonAlias("output")
        OUTPUT
    }

    enum class ConnectionType {
        @JsonAlias("input")
        INPUT,

        @JsonAlias("output")
        OUTPUT,

        @JsonAlias("input-output")
        INPUT_OUTPUT
    }

    data class Location(val x: Double, val y: Double) {
        override fun toString(): String = "(%.2f, %.2f)".format(x, y)
        companion object : StdDeserializer<Location>(Location::class.java){
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Location {
                return when (p.currentToken) {
                    JsonToken.START_ARRAY -> {
                        p.nextToken()
                        val x = p.valueAsDouble
                        p.nextToken()
                        val y = p.valueAsDouble
                        assert(p.nextToken() == JsonToken.END_ARRAY)
                        Location(x, y)
                    }
                    JsonToken.START_OBJECT -> {
                        var name: String

                        var x: Double = 0.0
                        var y: Double = 0.0

                        while (p.currentToken != JsonToken.END_OBJECT) {
                            name = p.nextFieldName()
                            p.nextToken()
                            when (name) {
                                "x" -> x = p.valueAsDouble
                                "y" -> y = p.valueAsDouble
                            }
                        }

                        Location(x, y)
                    }
                    else -> error("Cannot deserialize location from primitive: ${p.currentValue}")
                }
            }
        }
    }
}