package minerofmillions.blueprint_planner.entities

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode

@JsonDeserialize(using = FactorioRecipe.Companion::class)
data class FactorioRecipe(
    val name: String,
    val ingredients: List<FactorioItemStack>,
    val results: List<FactorioItemStack>,
) {
    companion object : StdDeserializer<FactorioRecipe>(FactorioRecipe::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FactorioRecipe =
            p.codec.readTree<JsonNode>(p).let { tree ->
                if (tree.has("normal"))
                    return ctxt.readTreeAsValue(
                        (tree["normal"] as ObjectNode).set<ObjectNode>("name", tree["name"]),
                        FactorioRecipe::class.java
                    )
                val name = tree["name"].asText()
                val ingredients = ctxt.readTreeAsValue<List<FactorioItemStack>>(
                    tree["ingredients"],
                    ctxt.typeFactory.constructCollectionType(List::class.java, FactorioItemStack::class.java)
                )
                val results =
                    if (tree.has("result")) listOf(FactorioItemStack(tree["result"].asText(), 1.0))
                    else tree["results"].map { ctxt.readTreeAsValue(it, FactorioItemStack::class.java) }
                FactorioRecipe(name, ingredients, results)
            }
    }
}