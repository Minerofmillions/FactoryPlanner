package minerofmillions.recipe_factory.app.components

import com.arkivanov.decompose.ComponentContext
import minerofmillions.recipe_factory.core.PluginManager
import minerofmillions.recipe_factory.core.RecipeFactory

class ParserListComponent(
    context: ComponentContext,
    private val onShowParser: (RecipeFactory) -> Unit,
    private val onCancel: () -> Unit,
) : ComponentContext by context {
    val parsers: Map<String, Map<String, RecipeFactory>> =
        PluginManager.factories.groupBy { it.group }.mapValues { (_, group) -> group.associateBy { it.name } }


    fun showParser(parser: RecipeFactory) = onShowParser(parser)

    fun cancel() = onCancel()
}