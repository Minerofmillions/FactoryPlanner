package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

@JsonDeserialize(using = Biome.Deserializer::class)
data class Biome(
    val name: String,
    val friendlyName: String,
    val mainBlock: String?,
    val subBlocks: List<String>?,
    val ores: String?,
    val surfaceItems: List<PlaceableItem>,
    val undergroundItems: List<PlaceableItem>
) {
    object Deserializer : StdDeserializer<Biome>(Biome::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Biome =
            p.codec.readTree<JsonNode>(p).let { biome ->
                Biome(
                    biome["name"].asText(),
                    biome["friendlyName"].asText(),
                    biome["mainBlock"]?.asText(),
                    biome["subBlocks"]?.map(JsonNode::asText),
                    biome["ores"]?.asText(),
                    biome["surfacePlaceables"]?.get("items")?.map { p.codec.treeToValue(it, PlaceableItem::class.java) } ?: emptyList(),
                    biome["undergroundPlaceables"]?.get("items")?.map { p.codec.treeToValue(it, PlaceableItem::class.java) } ?: emptyList(),
                )
            }
    }
}