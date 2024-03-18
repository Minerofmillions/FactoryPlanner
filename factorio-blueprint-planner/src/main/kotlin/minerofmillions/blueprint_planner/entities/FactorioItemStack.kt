package minerofmillions.blueprint_planner.entities

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

@JsonDeserialize(using = FactorioItemStack.Companion::class)
data class FactorioItemStack(
    val name: String,
    val amount: Double
) {
    override fun toString(): String = "$name * $amount"

    companion object : StdDeserializer<FactorioItemStack>(FactorioItemStack::class.java) {
        private fun factorioItemStack(obj: JsonNode): FactorioItemStack {
            return if (obj.isArray) FactorioItemStack(obj[0].asText(), obj[1].asDouble())
            else {
                val probability = obj["probability"]?.asDouble() ?: 1.0
                if (obj.has("amount_min") && obj.has("amount_max")) FactorioItemStack(
                    obj["name"].asText(),
                    (obj["amount_min"].asDouble() + obj["amount_max"].asDouble()) * probability / 2.0
                )
                else FactorioItemStack(obj["name"].asText(), (obj["amount"]?.asDouble() ?: 1.0) * probability)
            }
        }

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FactorioItemStack =
            factorioItemStack(p.codec.readTree<JsonNode>(p))
    }
}