package minerofmillions.recipe_factory.core.impl

import minerofmillions.recipe_factory.core.Calculator
import minerofmillions.recipe_factory.core.ItemStack
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.utils.ceil
import minerofmillions.utils.div
import minerofmillions.utils.sumOf
import org.ojalgo.scalar.RationalNumber

internal class HomebrewGreedyCalculator(
    recipes: List<Recipe>,
    primitives: Collection<String>,
    unusableInputs: Collection<String>,
    unusableOutputs: Collection<String>
) : Calculator(recipes, primitives, unusableInputs, unusableOutputs) {
    override fun getMatrixSolution(currentProducts: List<ItemStack>, startingInputs: List<ItemStack>, isOneoff: Boolean): Map<Recipe, RationalNumber> {
        val currentlySolving = currentProducts.filter {
            it.item !in primitives && (recipesToMake[it.item]?.size ?: 0) in 2..MAX_COMMON_RECIPES
        }
        val product = currentlySolving.firstOrNull() ?: return emptyMap()
        logger.debug { product }
        val recipes = recipesToMake[product.item]!!
        val chosenRecipe = recipes.maxWith(compareBy<Recipe> {
            it.outputs.filter { it.item == product.item }.sumOf(ItemStack::amount) /
                    if (it.inputs.isEmpty()) RationalNumber.ONE else it.inputs.sumOf(ItemStack::amount)
        })
        logger.debug { chosenRecipe }
        val amount = (product.amount / chosenRecipe.outputs.filter { it.item == product.item }.sumOf(ItemStack::amount)).let {
            if (isOneoff) it.ceil() else it
        }
        return mapOf(chosenRecipe to amount)
    }
}