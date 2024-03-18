package minerofmillions.blueprint_planner.factory_components.recipe

class RecipeInfo(
    val recipe: Recipe,
    val machineCount: Double,
    val recipeType: RecipeType,
    val machineName: String,
    val modules: List<ModuleCount>,
    val customRecipe: Boolean,
    val beaconName: String?,
    val beaconModules: ModuleCount,
    val inputs: Map<String, Double>,
    val outputs: Map<String, Double>
) {
    fun scaleRecipe(scale: Double): RecipeInfo = RecipeInfo(
        recipe,
        machineCount * scale,
        recipeType,
        machineName,
        modules,
        customRecipe,
        beaconName,
        beaconModules,
        inputs.mapValues { (_, amount) -> amount * scale },
        outputs.mapValues { (_, amount) -> amount * scale }
    )
}