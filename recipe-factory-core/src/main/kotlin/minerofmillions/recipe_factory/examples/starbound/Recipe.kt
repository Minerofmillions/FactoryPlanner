package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

@JsonDeserialize(using = Recipe.Deserializer::class)
data class Recipe(val input: List<Item>, val output: List<Item>, val groups: List<String>) {
    @JsonIgnore
    val allItems = input.map(Item::item) union output.map(Item::item)

    object Deserializer : StdDeserializer<Recipe>(Recipe::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Recipe =
            p.codec.readTree<JsonNode>(p).let { tree ->
                if (tree["output"].isObject) Recipe(
                    tree["input"].map { p.codec.treeToValue(it, Item::class.java) },
                    listOf(p.codec.treeToValue(tree["output"], Item::class.java)),
                    tree["groups"]?.map(JsonNode::asText) ?: emptyList()
                )
                else Recipe(
                    tree["input"].map { p.codec.treeToValue(it, Item::class.java) },
                    tree["output"].map { p.codec.treeToValue(it, Item::class.java) },
                    tree["groups"]?.map(JsonNode::asText) ?: emptyList()
                )
            }
    }
}
