package minerofmillions.recipe_factory.core

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.getValue
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import minerofmillions.recipe_factory.core.impl.HomebrewGreedyCalculator
import minerofmillions.recipe_factory.core.impl.OjalgoAllAtOnceCalculator
import minerofmillions.utils.*
import mu.KotlinLogging
import org.ojalgo.scalar.RationalNumber
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KProperty
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

abstract class Calculator internal constructor(
    recipes: List<Recipe>,
    internal val primitives: Collection<String>,
    unusableInputs: Collection<String>,
    unusableOutputs: Collection<String>,
) {
    internal val recipesToMake = synchronizedMapOf<String, MutableSet<Recipe>>()
    internal val recipesToUse = synchronizedMapOf<String, MutableSet<Recipe>>()

    internal val logger = KotlinLogging.logger { }

    val validProducts: Set<String> = recipesToMake.keys

    init {
        val primitivesAsSet = primitives.toSet()

        logger.debug("Adding recipes")
        recipes.filter { it.inputItems.none(unusableInputs::contains) }
            .filter { it.outputItems.none(unusableOutputs::contains) }.forEach { recipe ->
                recipe.inputItems.forEach { recipesToUse.getOrPut(it, ::synchronizedSetOf).add(recipe) }
                recipe.outputItems.forEach { recipesToMake.getOrPut(it, ::synchronizedSetOf).add(recipe) }
            }

        val recipesToRemove = synchronizedSetOf<Recipe>()

        logger.debug("Finding duplicate recipes.")
        runBlocking {
            recipesToMake.values.forEachParallel { making ->
                making.forEachIndexed { index, a ->
                    if (a in recipesToRemove) return@forEachIndexed
                    for (b in making.drop(index + 1)) {
                        if (b in recipesToRemove) continue
                        if (!a.hasSameIO(b)) continue

                        val comparison = a.compareIOTo(b, primitivesAsSet)

                        if (comparison > 0) {
                            recipesToRemove.add(a)
                            break
                        } else if (comparison < 0) recipesToRemove.add(b)
                    }
                }
            }
        }

        recipesToRemove.forEach {
            recipesToMake.removeFromAll(it)
            recipesToUse.removeFromAll(it)
        }
        recipesToRemove.clear()

        logger.debug("Finding recipe loops.")
        runBlocking {
            recipesToMake.forEachParallel { (item, making) ->
                val using = recipesToUse[item] ?: return@forEachParallel

                for (make in making - recipesToRemove) {
                    for (use in using - recipesToRemove) {
                        if (!make.inputs.contentsEqual(use.outputs) || !make.outputs.contentsEqual(use.inputs)) continue

                        val currentMaking = making - recipesToRemove
                        val currentUsing = using - recipesToRemove

                        if (currentMaking.size != 1 && currentUsing.size == 1) {
                            recipesToRemove.add(make)
                            break
                        } else if (currentMaking.size == 1 && currentUsing.size != 1) recipesToRemove.add(use)
                    }
                }
            }
        }

        recipesToRemove.forEach {
            recipesToMake.removeFromAll(it)
            recipesToUse.removeFromAll(it)
        }
    }

    fun solve(vararg products: ItemStack, isOneoff: Boolean = false, startingProducts: List<ItemStack> = emptyList()) =
        solve(products.toList(), isOneoff, startingProducts)

    fun solve(
        products: Collection<ItemStack>,
        isOneoff: Boolean = false,
        startingProducts: List<ItemStack> = emptyList(),
    ): Flow<Map<Recipe, RationalNumber>> {
        if (products.isEmpty() || recipesToMake.all { (_, recipes) -> recipes.isEmpty() }) return flowOf(emptyMap())

        return channelFlow {
            send(emptyMap())
            try {
                runBlocking {
                    launch {
                        solve(products, this@channelFlow, isOneoff, startingProducts)
                        this@runBlocking.cancel("Finished calculation")
                    }
                    launch {
                        delay(CALCULATION_DURATION)
                        this@runBlocking.cancel("Calculation timed out")
                    }
                }
            } catch (e: CancellationException) {
                logger.debug(e.message)
            }
            close()
        }
    }

    private val canMakeCache = mutableMapOf<String, Boolean>()
    private fun canMake(item: String) = canMakeCache.getOrPut(item) { recipesToMake[item]?.isNotEmpty() == true }

    private suspend fun solve(
        products: Collection<ItemStack>,
        results: ProducerScope<Map<Recipe, RationalNumber>>,
        isOneoff: Boolean,
        startingProducts: List<ItemStack>,
    ) {
        var usedRecipes = emptyMap<Recipe, RationalNumber>()
        val skippedItems = primitives.toMutableSet()

        val currentInputs = mutableSetOf<ItemStack>()
        val currentOutputs = mutableSetOf<ItemStack>()

        fun getCurrentIO() {
            Recipe.generateIO(currentInputs,
                currentOutputs,
                usedRecipes.flatMap { (recipe, amount) -> recipe.inputs.map { it * amount } } + products,
                usedRecipes.flatMap { (recipe, amount) -> recipe.outputs.map { it * amount } } + startingProducts)
        }

        do {
            getCurrentIO()

            skippedItems.addAll(currentInputs.map(ItemStack::item).filterNot(::canMake))
            val currentlySolvingInputs = currentInputs.filter { it.item !in skippedItems }

            if (currentlySolvingInputs.isEmpty()) return

            val treeSolution = getTreeSolution(currentlySolvingInputs, isOneoff)
            if (treeSolution.isNotEmpty()) {
                usedRecipes = (usedRecipes.keys + treeSolution.keys).sorted().associateWith {
                    (usedRecipes[it] + treeSolution[it])!!
                }
                results.send(usedRecipes)
                continue
            }

            val matrixSolution = getMatrixSolution(currentlySolvingInputs, startingProducts, isOneoff)
            if (matrixSolution.isNotEmpty()) {
                usedRecipes = (usedRecipes.keys + matrixSolution.keys).sorted().associateWith {
                    (usedRecipes[it] + matrixSolution[it])!!
                }
                results.send(usedRecipes)
                continue
            }

            return
        } while (currentInputs.isNotEmpty())

        error("Solve loop should've ended in termination.")
    }

    private fun solveOnly(item: ItemStack) =
        recipesToMake[item.item]?.minWithOrNull(compareBy(Recipe::ratio).thenBy(Recipe::criticalRatio))

    private fun getTreeSolution(currentProducts: List<ItemStack>, isOneoff: Boolean): Map<Recipe, RationalNumber> {
        val current = currentProducts.toMutableList()
        val results = mutableMapOf<Recipe, RationalNumber>()
        while (current.isNotEmpty()) {
            val product = current.removeFirst()
            if (product.amount < RationalNumber.ZERO) continue

            val recipesMaking = recipesToMake[product.item]
            if (recipesMaking?.size != 1) continue

            logger.debug { product }

            val recipe = recipesMaking.first()
            val ratio = product.amount / recipe.outputs.first { it.item == product.item }.amount
            val ratioAccountingForOneoff = if (isOneoff) ratio.ceil() else ratio
            ratio.rem(1)
            results.merge(recipe, ratioAccountingForOneoff, RationalNumber::add)
        }

        return results
    }

    internal abstract fun getMatrixSolution(
        currentProducts: List<ItemStack>,
        startingInputs: List<ItemStack>,
        isOneoff: Boolean,
    ): Map<Recipe, RationalNumber>

    class Builder {
        private var recipes: List<Recipe> = emptyList()
        private var primitives: Collection<String> = emptyList()
        private var unusableInputs: Collection<String> = emptyList()
        private var unusableOutputs: Collection<String> = emptyList()

        fun primitives(primitives: Collection<String>) = apply {
            this.primitives = primitives
        }

        fun primitives(vararg primitives: String) = apply {
            this.primitives = primitives.toList()
        }

        fun unusableInputs(inputs: Collection<String>) = apply {
            this.unusableInputs = inputs
        }

        fun unusableInputs(vararg inputs: String) = apply {
            this.unusableInputs = inputs.toList()
        }

        fun unusableOutputs(outputs: Collection<String>) = apply {
            this.unusableOutputs = outputs
        }

        fun unusableOutputs(vararg outputs: String) = apply {
            this.unusableOutputs = outputs.toList()
        }

        fun recipes(recipes: Sequence<Recipe>) = apply {
            this.recipes = recipes.toList()
        }

        fun recipes(recipes: Iterable<Recipe>) = apply {
            this.recipes = recipes.toList()
        }

        fun recipes(vararg recipes: Recipe) = apply {
            this.recipes = recipes.toList()
        }

        fun build(calculatorType: CalculatorType) = when (calculatorType) {
            CalculatorType.OJALGO_ALL_AT_ONCE -> OjalgoAllAtOnceCalculator(
                recipes,
                primitives,
                unusableInputs,
                unusableOutputs,
            )

            CalculatorType.HOMEBREW_GREEDY -> HomebrewGreedyCalculator(
                recipes,
                primitives,
                unusableInputs,
                unusableOutputs,
            )
        }

    }

    enum class CalculatorType {
        OJALGO_ALL_AT_ONCE, HOMEBREW_GREEDY
    }

    companion object {
        val calculationDurationValue = MutableValue(5.minutes)
        val calculateSubtreeValue = MutableValue(true)
        val commonRecipesValue = MutableValue(25)

        var CALCULATION_DURATION by calculationDurationValue
        var SHOULD_CALCULATE_SUB_TREE_USING_MATRIX by calculateSubtreeValue
        var MAX_COMMON_RECIPES by commonRecipesValue
    }
}

private operator fun <T : Any> MutableValue<T>.setValue(thisRef: Any, property: KProperty<*>, value: T) {
    this.value = value
}

private fun <K, V : Any> MutableMap<K, V>.mergeWith(other: Map<K, V>, remappingFunction: (V, V) -> V) {
    other.forEach {
        merge(it.key, it.value, remappingFunction)
    }
}
