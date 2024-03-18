package minerofmillions.recipe_factory.examples.minecraft

import com.fasterxml.jackson.databind.JsonNode
import minerofmillions.recipe_factory.core.ItemStack
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.recipe_factory.examples.MinecraftRecipeDumpFactory
import org.pf4j.Extension

@Extension
class StonecuttingParser : MinecraftRecipeDumpFactory.TypeParser("minecraft:stonecutting") {
    override fun parseRecipe(id: String, recipeObject: JsonNode): Sequence<Recipe> {
        val ingredients = recipeObject["ingredient"].toIngredient()
        val product = ItemStack(recipeObject["result"].asText(), recipeObject["count"]?.asInt() ?: 1)

        return ingredients.possibilities.asSequence().mapIndexed { index, it -> Recipe("$id@$index", listOf(it), listOf(product)) }
    }
}