package minerofmillions.recipe_factory.core

import minerofmillions.utils.*
import org.ojalgo.scalar.RationalNumber

class Recipe(val name: String, inputs: List<ItemStack>, outputs: List<ItemStack>) : Comparable<Recipe> {
    override fun toString(): String = name

    fun hasSameIO(other: Recipe): Boolean =
        inputItems.contentsEqual(other.inputItems) && outputItems.contentsEqual(other.outputItems)

    val inputs: List<ItemStack>
    val outputs: List<ItemStack>

    @Transient
    val inputItems: Set<String> = inputs.mapTo(sortedSetOf(), ItemStack::item)

    @Transient
    val outputItems: Set<String> = outputs.mapTo(sortedSetOf(), ItemStack::item)

    @Transient
    val inputCount = inputs.sumOf(ItemStack::amount)

    @Transient
    val outputCount = outputs.sumOf(ItemStack::amount)

    val criticalNumerator: RationalNumber
        get() = maxOf(
            inputs.maxOfOrNull(ItemStack::amount) ?: RationalNumber.MIN_VALUE,
            outputs.maxOfOrNull(ItemStack::amount) ?: RationalNumber.MIN_VALUE
        )
    val criticalDenominator: RationalNumber
        get() = minOf(
            inputs.minOfOrNull(ItemStack::amount) ?: RationalNumber.MAX_VALUE,
            outputs.minOfOrNull(ItemStack::amount) ?: RationalNumber.MAX_VALUE
        )

    val criticalRatio: RationalNumber
        get() = criticalNumerator / criticalDenominator

    val ratio: RationalNumber
        get() = inputs.sumOf(ItemStack::amount) / outputs.sumOf(ItemStack::amount)

    init {
        val i = mutableListOf<ItemStack>()
        val o = mutableListOf<ItemStack>()
        generateIO(i, o, inputs, outputs)
        this.inputs = i.sorted()
        this.outputs = o.sorted()
    }

    operator fun plus(other: Recipe): Recipe =
        Recipe("$name + ${other.name}", inputs + other.inputs, outputs + other.outputs)

    operator fun times(amount: RationalNumber): Recipe =
        Recipe("%.3f * %s".format(amount.toBigDecimal(), name), inputs.map { it * amount }, outputs.map { it * amount })

    fun normalizeTo(normalizationItem: String): Recipe {
        val normalizationAmount =
            (inputs.firstOrNull { it.item == normalizationItem } ?: outputs.firstOrNull { it.item == normalizationItem }
            ?: return this).amount
        return Recipe(
            name,
            inputs.map(itemStackDivision(normalizationAmount)),
            outputs.map(itemStackDivision(normalizationAmount))
        )
    }

    fun compareIOTo(other: Recipe, primitives: Set<String>): Int = comparisonComparison(
        compareInputs(other, primitives),
        compareOutputs(other, primitives),
        { comparePrimitivesTo(other, primitives) },
        { compareNormalizedIOTo(other, primitives) })

    private fun compareInputs(other: Recipe, primitives: Set<String> = emptySet()) =
        recipeInputComparisonCache.getOrPut(this, ::mutableMapOf).getOrPut(other) {
            if ((inputItems - primitives).contentsEqual(other.inputItems - primitives))
                (inputs.filterNot
                { it.item in primitives }
                    .sumOf(ItemStack::amount) - other.inputs.filterNot
                { it.item in primitives }
                    .sumOf(ItemStack::amount)).let {
                    when {
                        it < RationalNumber.ZERO -> -1
                        it > RationalNumber.ZERO -> 1
                        else -> 0
                    }
                }
            else error("Incomparable inputs: $inputs <-> ${other.inputs}")
        }

    private fun compareOutputs(other: Recipe, primitives: Set<String> = emptySet()) =
        recipeOutputComparisonCache.getOrPut(this, ::mutableMapOf).getOrPut(other) {
            if ((outputItems - primitives).contentsEqual(other.outputItems - primitives))
                (outputs.filterNot { it.item in primitives }
                    .sumOf(ItemStack::amount) - other.outputs.filterNot { it.item in primitives }
                    .sumOf(ItemStack::amount)).let {
                    when {
                        it < RationalNumber.ZERO -> -1
                        it > RationalNumber.ZERO -> 1
                        else -> 0
                    }
                }
            else error("Incomparable outputs: $outputs <-> ${other.outputs}")
        }

    private fun comparePrimitivesTo(other: Recipe, primitives: Set<String>): Int = comparisonComparison(
        inputs.filter { it.item in primitives }
            .sumOf { input -> input.amount - other.inputs.single { it.item == input.item }.amount }.let {
                when {
                    it < RationalNumber.ZERO -> -1
                    it > RationalNumber.ZERO -> 1
                    else -> 0
                }
            },
        outputs.filter { it.item in primitives }
            .sumOf { output -> output.amount - other.outputs.single { it.item == output.item }.amount }.let {
                when {
                    it < RationalNumber.ZERO -> -1
                    it > RationalNumber.ZERO -> 1
                    else -> 0
                }
            },
        { 0 },
        { 0 })

    private fun compareNormalizedIOTo(other: Recipe, primitives: Set<String>): Int {
        val normalizationItem = inputs.filter { it.item !in primitives }.minByOrNull { it.amount }?.item
            ?: outputs.filter { it.item !in primitives }.minByOrNull { it.amount }?.item ?: return 0
        val a = normalizeTo(normalizationItem)
        val b = other.normalizeTo(normalizationItem)

        return comparisonComparison(
            a.inputs.filterNot { it.item in primitives }.sumOf(ItemStack::amount) -
                    b.inputs.filterNot { it.item in primitives }.sumOf(ItemStack::amount),
            a.outputs.filterNot { it.item in primitives }.sumOf(ItemStack::amount) -
                    b.outputs.filterNot { it.item in primitives }.sumOf(ItemStack::amount),
            { a.comparePrimitivesTo(b, primitives) },
            { 0 })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Recipe

        if (inputs != other.inputs) return false
        if (outputs != other.outputs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inputs.hashCode()
        result = 31 * result + outputs.hashCode()
        return result
    }

    override fun compareTo(other: Recipe) = comparator.compare(this, other)

    companion object {
        private val comparator = compareBy(Recipe::name).thenBy(Recipe::ratio).thenBy(Recipe::criticalRatio)

        //.thenComparing(Recipe::compareInputs).thenComparing(Recipe::compareOutputs)
        private val itemStackDivision: (ItemStack, RationalNumber) -> ItemStack = ItemStack::div

        private val recipeInputComparisonCache = mutableMapOf<Recipe, MutableMap<Recipe, Int>>()
        private val recipeOutputComparisonCache = mutableMapOf<Recipe, MutableMap<Recipe, Int>>()

        fun generateIO(i: MutableCollection<ItemStack>, o: MutableCollection<ItemStack>) =
            generateIO(i, o, i.toList(), o.toList())

        fun generateIO(
            i: MutableCollection<ItemStack>,
            o: MutableCollection<ItemStack>,
            solution: Map<Recipe, RationalNumber>,
        ) = generateIO(
            i,
            o,
            solution.flatMap { (recipe, amount) -> recipe.inputs.map { it * amount } },
            solution.flatMap { (recipe, amount) -> recipe.outputs.map { it * amount } })

        fun generateIO(
            i: MutableCollection<ItemStack>,
            o: MutableCollection<ItemStack>,
            inputs: List<ItemStack>,
            outputs: List<ItemStack>,
        ) {
            i.clear()
            o.clear()

            val ins = inputs.mergeStacks()
            val outs = outputs.mergeStacks()

            val inItems = ins.mapTo(mutableSetOf(), ItemStack::item)
            val outItems = outs.mapTo(mutableSetOf(), ItemStack::item)

            i.addAll(ins.filter { it.item !in outItems })
            o.addAll(outs.filter { it.item !in inItems })
            (inItems intersect outItems).forEach { item ->
                val inItem = ins.first { it.item == item }
                val outItem = outs.first { it.item == item }
                if (inItem.amount > outItem.amount) i.add(ItemStack(item, inItem.amount - outItem.amount))
                else if (inItem.amount < outItem.amount) o.add(ItemStack(item, outItem.amount - inItem.amount))
            }

            i.removeIf { it.amount.aboutZero() }
            o.removeIf { it.amount.aboutZero() }
        }

        private fun comparisonComparison(
            inputComparison: Int,
            outputComparison: Int,
            bothZeroResult: () -> Int,
            bothSameSignResult: () -> Int,
        ) = when {
            inputComparison == 0 && outputComparison == 0 -> bothZeroResult()
            inputComparison == 0 -> -outputComparison
            outputComparison == 0 -> inputComparison
            inputComparison * outputComparison < 0 -> inputComparison
            else -> bothSameSignResult()
        }

        private fun comparisonComparison(
            inputComparison: RationalNumber,
            outputComparison: RationalNumber,
            bothZeroResult: () -> Int,
            bothSameSignResult: () -> Int,
        ) = when {
            inputComparison == RationalNumber.ZERO && outputComparison == RationalNumber.ZERO -> bothZeroResult()
            inputComparison == RationalNumber.ZERO && outputComparison < RationalNumber.ZERO -> 1
            inputComparison == RationalNumber.ZERO -> -1
            outputComparison == RationalNumber.ZERO && inputComparison < RationalNumber.ZERO -> -1
            outputComparison == RationalNumber.ZERO -> 1
            inputComparison < RationalNumber.ZERO && outputComparison > RationalNumber.ZERO -> -1
            inputComparison > RationalNumber.ZERO && outputComparison < RationalNumber.ZERO -> 1
            else -> bothSameSignResult()
        }
    }
}

fun Map<Recipe, RationalNumber>.getInputs() = flatMap { (recipe, amount) -> recipe.inputs.map { it * amount } }.mergeStacks()
fun Map<Recipe, RationalNumber>.getOutputs() = flatMap { (recipe, amount) -> recipe.outputs.map { it * amount} }.mergeStacks()
fun Map<Recipe, RationalNumber>.print() {
    forEach { (recipe, amount) ->
        println("%s * %.3f".format(recipe.name, amount.toBigDecimal()))
    }
    val i = mutableListOf<ItemStack>()
    val o = mutableListOf<ItemStack>()
    Recipe.generateIO(i, o, this)
    println(i.sorted())
    println(o.sorted())
}

fun Iterable<Recipe>.getInputItems(): Set<String> = flatMapTo(sortedSetOf(), Recipe::inputItems)
fun Iterable<Recipe>.getOutputItems(): Set<String> = flatMapTo(sortedSetOf(), Recipe::outputItems)
fun Iterable<Recipe>.getAllItems(): Set<String> = (getInputItems() + getOutputItems()).toSortedSet()