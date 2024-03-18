package minerofmillions.recipe_factory.examples.minecraft

import com.fasterxml.jackson.databind.JsonNode
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.recipe_factory.core.mergeStacks
import minerofmillions.recipe_factory.examples.MinecraftRecipeDumpFactory
import minerofmillions.recipe_factory.examples.permutations
import org.pf4j.Extension

@Extension
class ShapedParser : MinecraftRecipeDumpFactory.TypeParser("minecraft:crafting_shaped") {
    override fun parseRecipe(id: String, recipeObject: JsonNode): Sequence<Recipe> {
        val key = recipeObject["key"].let {
            it.fieldNames()!!.asSequence().associate { key ->
                key[0] to it[key].toIngredient()
            }
        }
        val pattern = recipeObject["pattern"].map(JsonNode::asText)
        val result = recipeObject["result"].toIngredient().possibilities[0]

        val ingredients = pattern.flatMap { it.toList() }.mapNotNull { if (it == ' ') null else key[it] }
        return ingredients.permutations().mapIndexed { index, it -> Recipe("$id@$index", it.mergeStacks(), listOf(result)) }
    }
}