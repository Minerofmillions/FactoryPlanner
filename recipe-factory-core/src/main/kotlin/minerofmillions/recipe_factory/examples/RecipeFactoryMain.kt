package minerofmillions.recipe_factory.examples

import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import minerofmillions.recipe_factory.core.Calculator
import minerofmillions.recipe_factory.core.print
import minerofmillions.recipe_factory.core.times

fun main(): Unit = runBlocking {
    val recipeFactory = FactorioRecipeFactory()
    recipeFactory.outputPath.set("C:\\Users\\Jason\\AppData\\Roaming\\Factorio\\script-output\\temp-pack")
    val recipes = recipeFactory.loadRecipes { }
        .filterNot { it.name.startsWith("particle-accelerator") }
    val calculator = Calculator.Builder()
        .recipes(recipes)
        .build(Calculator.CalculatorType.OJALGO_ALL_AT_ONCE)

    Calculator.SHOULD_CALCULATE_SUB_TREE_USING_MATRIX = false
    Calculator.MAX_COMMON_RECIPES = 10

    val solution = calculator.solve("automation-science-pack" * 10)
    solution.last().print()
}