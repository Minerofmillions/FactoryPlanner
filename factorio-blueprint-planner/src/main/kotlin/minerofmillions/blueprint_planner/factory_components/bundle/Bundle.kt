package minerofmillions.blueprint_planner.factory_components.bundle

import minerofmillions.blueprint_planner.factory_components.lane.Lane
import minerofmillions.blueprint_planner.factory_components.recipe.RecipeType

open class Bundle(lanes: List<Lane>, width: Int) {
    fun drawBundle(recipeIdx: Int, height: Int, yBase: Int, taps: List<Tap>, bypasses: List<Bypass>, fullConfig: FullConfig, flowrates: Map<String, Double>, recipeType: RecipeType) {

    }

    fun getBundleSignals(): BundleSignals = BundleSignals()

    class BundleSignals(val type: String = "", name: List<String> = emptyList())
}