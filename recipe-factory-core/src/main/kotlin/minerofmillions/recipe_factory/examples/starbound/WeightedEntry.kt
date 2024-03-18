package minerofmillions.recipe_factory.examples.starbound

interface WeightedEntry {
    val weight: Double

    fun probability(totalWeight: Double) = weight / totalWeight
}

fun Collection<WeightedEntry>.totalWeight() = sumOf(WeightedEntry::weight)
fun <T: WeightedEntry> Collection<T>.forEachWithProbability(block: (T, Double) -> Unit) = totalWeight().let { weight ->
    forEach { block(it, it.probability(weight)) }
}