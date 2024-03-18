package minerofmillions.recipe_factory.examples.factorio

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import minerofmillions.recipe_factory.core.Recipe

@JsonDeserialize(using = FactorioRecipe.Companion::class)
data class FactorioRecipe(
    val name: String,
    val ingredients: List<FactorioIngredient>,
    val products: List<FactorioIngredient>,
    val enabled: Boolean
) {
    fun toRecipe() = Recipe(name, ingredients.map(FactorioIngredient::toItemStack), products.map(FactorioIngredient::toItemStack))
    companion object : StdDeserializer<FactorioRecipe>(FactorioRecipe::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FactorioRecipe {
            val obj = p.readValueAsTree<JsonNode>()
            val name = obj["name"].asText()
            val ingredients = obj["ingredients"].let {
                if (it.isObject) emptyList()
                else it.map { p.codec.treeToValue(it, FactorioIngredient::class.java) }
            }
            val products = obj["products"].let {
                if (it.isObject) emptyList()
                else it.map { p.codec.treeToValue(it, FactorioIngredient::class.java) }
            }
            val enabled = obj["enabled"].asBoolean()
            return FactorioRecipe(name, ingredients, products, enabled)
        }
    }
}
