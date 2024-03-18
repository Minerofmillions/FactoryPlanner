package minerofmillions.recipe_factory.core.impl

import minerofmillions.recipe_factory.core.Calculator
import minerofmillions.recipe_factory.core.ItemStack
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.utils.*
import org.ojalgo.OjAlgoUtils
import org.ojalgo.machine.Hardware
import org.ojalgo.machine.VirtualMachine
import org.ojalgo.optimisation.ExpressionsBasedModel
import org.ojalgo.optimisation.Variable
import org.ojalgo.scalar.RationalNumber

internal class OjalgoAllAtOnceCalculator(
    recipes: List<Recipe>,
    primitives: Collection<String>,
    unusableInputs: Collection<String>,
    unusableOutputs: Collection<String>
) : Calculator(recipes, primitives, unusableInputs, unusableOutputs) {
    init {
        System.setProperty("shut.up.ojAlgo", "true")

        OjAlgoUtils.ENVIRONMENT = Hardware.makeSimple(
            VirtualMachine.getArchitecture(),
            VirtualMachine.getMemory(),
            Runtime.getRuntime().availableProcessors() - 1
        ).virtualise()
    }

    private fun getUsefulRecipes(products: Collection<String>): Set<Recipe> {
        val usefulRecipes = sortedSetOf<Recipe>()
        val currentInputs = products.toMutableSet()
        val solvedInputs = mutableSetOf<String>()

        while (currentInputs.isNotEmpty()) {
            val input = currentInputs.first()
            currentInputs.remove(input)
            solvedInputs.add(input)

            if (input in primitives) continue

            val recipesMaking =
                recipesToMake[input]?.takeIf { it.size > if (SHOULD_CALCULATE_SUB_TREE_USING_MATRIX) 0 else 1 }
                    ?: continue
            if (recipesMaking.size > MAX_COMMON_RECIPES) {
                continue
            }
            usefulRecipes.addAll(recipesMaking)
            currentInputs.addAll(recipesMaking.flatMap(Recipe::inputItems) - solvedInputs)
        }

        return usefulRecipes
    }

    override fun getMatrixSolution(currentProducts: List<ItemStack>, startingInputs: List<ItemStack>, isOneoff: Boolean): Map<Recipe, RationalNumber> {
//        val product = currentProducts.first { (recipesToMake[it.item]?.size ?: 0) in 2..<MAX_COMMON_RECIPES }.item
//        val usefulRecipes = getUsefulRecipes(product)

        val usefulRecipes = getUsefulRecipes(currentProducts.map(ItemStack::item))

        if (usefulRecipes.isEmpty()) return emptyMap()

        val allInputs = usefulRecipes.flatMapTo(sortedSetOf(), Recipe::inputItems)
        val allOutputs = usefulRecipes.flatMapTo(sortedSetOf(), Recipe::outputItems)

        val allItems = allInputs + allOutputs

        val slackItems = allInputs - allOutputs + primitives.filter(allItems::contains)
        val surplusItems = allOutputs - allInputs + primitives.filter(allItems::contains)

        val criticalNumerator = usefulRecipes.maxOf(Recipe::criticalNumerator)
        val criticalDenominator = usefulRecipes.minOf(Recipe::criticalDenominator)
        val criticalRatio = (criticalNumerator / criticalDenominator).toBigDecimal()

        val recipeVariables: Map<Recipe, Variable>
        val model = ExpressionsBasedModel().apply {
            recipeVariables = usefulRecipes.associateWith { recipe ->
                addVariable(recipe.name).lower(0).weight(1).integer(isOneoff)
            }

            allItems.forEach { item ->
                addExpression(item).lower(currentProducts.filter { it.item == item }.sumOf(ItemStack::amount) - startingInputs.filter { it.item == item }.sumOf(ItemStack::amount)).apply {
                    if (item in slackItems) addVariable("Slack $item").weight(criticalRatio).lower(0).let {
                        set(it, 1)
                    }

                    recipeVariables.forEach { (recipe, variable) ->
                        recipe.inputs.filter { it.item == item }.forEach { add(variable, -it.amount) }
                        recipe.outputs.filter { it.item == item }.forEach { add(variable, it.amount) }
                    }
                }
            }
        }

        logger.debug { "${model.variables.size} variables" }
        logger.debug { "${model.expressions.size} expressions" }

        logger.debug { "Solving $currentProducts" }
        val solution = model.minimise()
        return recipeVariables.takeIf { solution.state.isFeasible }
            ?.mapValues { (_, variable) -> RationalNumber.valueOf(variable.value) }
            ?.filterValues { !it.aboutZero() } ?: emptyMap()
    }
}