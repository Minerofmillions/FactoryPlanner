package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

@JsonDeserialize(using = ExtractionQuantity.Deserializer::class)
data class ExtractionQuantity(val basicQuantity: Int, val advancedQuantity: Int, val quantumQuantity: Int) {
    constructor(quantity: Int) : this(quantity, quantity, quantity)

    operator fun get(index: Int) = when (index) {
        0 -> basicQuantity
        1 -> advancedQuantity
        2 -> quantumQuantity
        else -> error("Invalid index for quantity.")
    }

    fun getUniqueTiers() = listOfNotNull(
        0,
        1.takeIf { basicQuantity != advancedQuantity },
        2.takeIf { basicQuantity != quantumQuantity && advancedQuantity != quantumQuantity }
    )

    object Deserializer : StdDeserializer<ExtractionQuantity>(ExtractionQuantity::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ExtractionQuantity =
            p.readValueAsTree<JsonNode>().let {
                if (it.isArray) ExtractionQuantity(it[0].asInt(), it[1].asInt(), it[2].asInt())
                else ExtractionQuantity(it.asInt())
            }
    }
}
