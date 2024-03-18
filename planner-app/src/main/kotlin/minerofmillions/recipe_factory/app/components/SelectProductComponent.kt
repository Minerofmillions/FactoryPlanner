package minerofmillions.recipe_factory.app.components

import com.arkivanov.decompose.ComponentContext

class SelectProductComponent(
    componentContext: ComponentContext,
    val validProducts: Set<String>,
    private val onCancel: () -> Unit,
    private val onSelect: (String) -> Unit
) : ComponentContext by componentContext {
    fun cancel() = onCancel()
    fun select(item: String) = onSelect(item)
}