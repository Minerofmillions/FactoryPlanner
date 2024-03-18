package minerofmillions.recipe_factory.app.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import minerofmillions.recipe_factory.app.components.SelectProductComponent
import minerofmillions.utils.ui.SelectString

@Composable
fun ProductSelect(component: SelectProductComponent) = Column {
    val products = remember { component.validProducts.sorted().filterNot(String::isBlank) }
    SelectString(products, component::select)
    Button({ component.cancel() }) {
        Text("Cancel")
    }
}