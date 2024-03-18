package minerofmillions.recipe_factory.examples.factorio

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

@JsonDeserialize(using = Technology.Companion::class)
data class Technology(
    val name: String,
    val effects: List<TechnologyEffect>,
) {
    companion object : StdDeserializer<Technology>(Technology::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Technology {
            val obj = p.readValueAsTree<JsonNode>()
            val name = obj["name"].asText()
            val effects = obj["effects"].let {
                if (it.isObject) emptyList()
                else it.map { p.codec.treeToValue(it, TechnologyEffect::class.java) }
            }
            return Technology(name, effects)
        }
    }
}