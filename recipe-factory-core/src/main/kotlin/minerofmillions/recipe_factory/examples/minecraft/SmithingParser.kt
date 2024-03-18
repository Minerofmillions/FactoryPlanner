package minerofmillions.recipe_factory.examples.minecraft

import com.fasterxml.jackson.databind.JsonNode
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.recipe_factory.examples.MinecraftRecipeDumpFactory
import minerofmillions.recipe_factory.examples.permutations
import org.pf4j.Extension

@Extension
class SmithingParser : MinecraftRecipeDumpFactory.TypeParser("minecraft:smithing") {
    override fun parseRecipe(id: String, recipeObject: JsonNode): Sequence<Recipe> {
        val base = recipeObject["base"].toIngredient()
        val addition = recipeObject["addition"].toIngredient()
        val product = recipeObject["result"].toIngredient().possibilities[0]
        return listOf(base, addition).permutations().mapIndexed { index, it -> Recipe("$id@$index", it, listOf(product)) }
    }
}