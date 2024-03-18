package minerofmillions.recipe_factory.core

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import minerofmillions.recipe_factory.core.config.FactoryPlannerPluginSpec
import org.pf4j.ExtensionPoint

abstract class RecipeFactory(val group: String, val name: String) : ExtensionPoint {
    val configSpec : FactoryPlannerPluginSpec = FactoryPlannerPluginSpec.Builder().apply { configure() }.build()

    abstract suspend fun loadRecipes(setStatus: (@Composable RowScope.() -> Unit) -> Unit): Sequence<Recipe>
    abstract fun FactoryPlannerPluginSpec.Builder.configure()
}