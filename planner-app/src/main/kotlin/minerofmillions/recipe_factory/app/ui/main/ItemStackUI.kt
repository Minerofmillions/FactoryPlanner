package minerofmillions.recipe_factory.app.ui.main

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import minerofmillions.recipe_factory.core.ItemStack

@Composable
fun ItemStack(stack: ItemStack, modifier: Modifier = Modifier) {
    Text("%s * %.3f".format(stack.item, stack.amount.toBigDecimal()), modifier)
}