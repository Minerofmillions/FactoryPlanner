package minerofmillions.blueprint_planner.factory_components.recipe

import minerofmillions.blueprint_planner.factory_components.item.IngredientItem

class FullRecipe(
    val recipes: List<RecipeInfo>,
    inputs: Map<String, Double>,
    outputs: Map<String, Double>,
    byproducts: Map<String, Double>,
    val timescale: Int
) {
    val inputs: Map<String, Double>
    val outputs: Map<String, Double>
    val byproducts: Map<String, Double>
    val itemLookup: Map<String, IngredientItem>

    init {
        val lookup = mutableMapOf<String, IngredientItem>()
        fun addToLookup(data: List<IngredientItem>) {
            for (item in data) {
                if (item.name !in lookup) lookup[item.name] = item
            }
        }
        recipes.forEach {
            addToLookup(it.recipe.inputs)
            addToLookup(it.recipe.outputs)
        }
        itemLookup = lookup

        this.inputs = inputs.filterKeys(lookup::contains)
        this.outputs = outputs.filterKeys(lookup::contains)
        this.byproducts = byproducts.filterKeys(lookup::contains)
    }

    fun getAllInputs(): Set<String> = recipes.flatMap { it.inputs.keys } union inputs.keys

    fun getAllOutputs(): Set<String> = recipes.flatMap { it.outputs.keys } union outputs.keys

    fun getOutputs(): Set<String> = getAllInputs().let { inputs ->
        getAllOutputs().filterNotTo(mutableSetOf(), inputs::contains)
    }

    fun getOutputsNotInputs(): Set<String> = getAllInputs().let { inputs ->
        outputs.keys.filterNotTo(mutableSetOf(), inputs::contains)
    }

    fun getNonOutputItems(): Set<String> =
        recipes.flatMapTo(mutableSetOf()) { it.recipe.inputs.map(IngredientItem::name) }

    fun getUnused(): Set<String> = (getNonOutputItems() union getOutputs()).let { seenItems ->
        getAllInputs().filterNot(seenItems::contains) union getAllOutputs().filterNot(seenItems::contains)
    }

    fun getByproducts(): Set<String> = getRecipeTypeItems(Recipe::outputs, Recipe::inputs)

    fun getPrioritizedItems(): Set<String> = getRecipeTypeItems(Recipe::inputs, Recipe::outputs)

    private fun getRecipeTypeItems(produceRecipeItems: Recipe.() -> Collection<IngredientItem>, consumeRecipeItems: Recipe.() -> Collection<IngredientItem>): Set<String> =
        recipes.filter { it.recipeType == RecipeType.PRODUCE }
            .flatMapTo(mutableSetOf()) { it.recipe.produceRecipeItems().map(IngredientItem::name) }
            .let { produceItems ->
                recipes.filter { it.recipeType == RecipeType.CONSUME }
                    .flatMapTo(mutableSetOf()) { it.recipe.consumeRecipeItems().map(IngredientItem::name) }
                    .let(produceItems::intersect)
            }

    fun getNormalOutputItems(): Set<String> = getNonOutputItems()
        .filterNot(getByproducts()::contains)
        .filterNotTo(mutableSetOf(), getPrioritizedItems()::contains)

    fun getBaseInputs() = getAllInputs().filterNot(getAllOutputs()::contains)

    fun setRecipes(recipes: List<RecipeInfo>) = FullRecipe(recipes, inputs, outputs, byproducts, timescale)

    fun getNormalLaneDirections(): Pair<Set<String>, Set<String>> {
        val byproducts = getByproducts()
        val prioritized = getPrioritizedItems()

        val upLanes = mutableSetOf<String>()
        val downLanes = mutableSetOf<String>()

        for (recipe in recipes) {
            if (recipe.recipeType == RecipeType.PRODUCE) {
                for (item in recipe.recipe.inputs)
                    if (item in byproducts || item in prioritized) upLanes += item
                for (item in recipe.recipe.outputs)
                    if (item in byproducts && item !in prioritized) downLanes += item
            } else if (recipe.recipeType == RecipeType.CONSUME) {
                for (item in recipe.recipe.inputs)
                    if (item !in byproducts && item in prioritized) upLanes += item
                for (item in recipe.recipe.outputs)
                    if (item in byproducts || item in prioritized) downLanes += item
            }
        }

        return upLanes to downLanes
    }
}

private operator fun Collection<String>.contains(item: IngredientItem) = contains(item.name)
private operator fun MutableCollection<String>.plusAssign(item: IngredientItem) {
    add(item.name)
}