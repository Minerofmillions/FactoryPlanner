package minerofmillions.recipe_factory.app.components

import com.arkivanov.decompose.ComponentContext
import minerofmillions.recipe_factory.core.RecipeFactory

class ParserConfigComponent(
    context: ComponentContext,
    val parser: RecipeFactory,
    private val onCancel: () -> Unit,
    private val onConfirm: () -> Unit
) : ComponentContext by context {
    fun cancel() = onCancel()
    fun confirm() = onConfirm()
}