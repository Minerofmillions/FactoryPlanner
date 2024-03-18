package minerofmillions.recipe_factory.examples.minecraft

import com.fasterxml.jackson.databind.JsonNode
import minerofmillions.recipe_factory.core.ItemStack
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.recipe_factory.examples.MinecraftRecipeDumpFactory
import minerofmillions.recipe_factory.examples.permutations
import org.pf4j.Extension

@Extension
class ShapelessParser : MinecraftRecipeDumpFactory.TypeParser("minecraft:crafting_shapeless") {
    override fun parseRecipe(id: String, recipeObject: JsonNode): Sequence<Recipe> {
        val ingredients = recipeObject["ingredients"]
            .map { it.toIngredient() }
        val product = recipeObject["result"].let {
            ItemStack(it["item"].asText(), it["count"]?.asInt() ?: 1)
        }
        return ingredients.permutations().mapIndexed { index, it -> Recipe("$id@$index", it, listOf(product)) }
    }
}