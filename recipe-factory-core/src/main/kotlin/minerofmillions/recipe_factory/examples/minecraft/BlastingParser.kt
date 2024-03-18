package minerofmillions.recipe_factory.examples.minecraft

import com.fasterxml.jackson.databind.JsonNode
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.recipe_factory.examples.MinecraftRecipeDumpFactory
import org.pf4j.Extension

@Extension
class BlastingParser : MinecraftRecipeDumpFactory.TypeParser("minecraft:blasting") {
    override fun parseRecipe(id: String, recipeObject: JsonNode): Sequence<Recipe> {
        val ingredients = recipeObject["ingredient"].toIngredient()
        val product = recipeObject["result"].toIngredient().possibilities[0]
        return ingredients.possibilities.asSequence().mapIndexed { index, it -> Recipe("$id@$index", listOf(it), listOf(product)) }
    }
}