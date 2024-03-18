package minerofmillions.recipe_factory.app.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import minerofmillions.recipe_factory.app.components.CalculatorUseComponent
import minerofmillions.utils.ui.ScrollableColumn
import minerofmillions.recipe_factory.core.Calculator
import org.ojalgo.scalar.RationalNumber

@Composable
fun CalculatorUse(component: CalculatorUseComponent) {
    val currentProduct by component.editingProduct.subscribeAsState()
    var productAmount by remember { mutableStateOf("1") }
    val amountValid by remember {
        derivedStateOf {
            try {
                RationalNumber.parse(productAmount)
                true
            } catch (_: NumberFormatException) {
                false
            }
        }
    }
    val productValid by remember {
        derivedStateOf {
            amountValid && currentProduct.item in component.calculator.validProducts
        }
    }

    Row {
        Column(Modifier.weight(1f)) {
            val products by component.products.subscribeAsState()
            Text("Products")
            ScrollableColumn(Modifier.weight(1f)) {
                items(products) {
                    ItemStack(it, Modifier.clickable { component.removeProduct(it.item) })
                }
            }
            Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                TextField(currentProduct.item, component::setProduct, Modifier.weight(1f).height(IntrinsicSize.Max))
                Button(component::selectProduct) {
                    Text("Select item")
                }
                TextField(productAmount, {
                    productAmount = it
                    try {
                        component.setProductAmount(RationalNumber.parse(productAmount))
                    } catch (_: NumberFormatException) {
                    }
                }, Modifier.weight(1f).height(IntrinsicSize.Max))
                Button({
                    component.addProduct()
                    productAmount = "1"
                }, Modifier.height(IntrinsicSize.Max), enabled = productValid) {
                    Text("Add product")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isRunning by component.isRunning.subscribeAsState()
                val hasProducts by component.hasProducts.subscribeAsState()

                val calculateSubtree by Calculator.calculateSubtreeValue.subscribeAsState()
                val commonRecipes by Calculator.commonRecipesValue.subscribeAsState()

                Button({ component.returnToSetup() }) {
                    Text("Return to setup")
                }
                Button({ component.startCalculation() }, enabled = !isRunning && hasProducts) {
                    Text("Start calculation")
                }
                Box(Modifier.weight(1f))
                Checkbox(
                    calculateSubtree,
                    { Calculator.SHOULD_CALCULATE_SUB_TREE_USING_MATRIX = it })
                Text("Calculate subtree?")
                TextField(commonRecipes.toString(), { new ->
                    new.toIntOrNull()?.takeIf { it > 0 }?.let { Calculator.MAX_COMMON_RECIPES = it }
                })
            }
        }
        Column(Modifier.weight(1f)) {
            val solution by component.solution.subscribeAsState()
            val solutionIO by component.solutionIO.subscribeAsState()
            Text("Solution")
            Text("Recipes")
            ScrollableColumn(Modifier.weight(1f)) {
                items(solution.toList()) { (recipe, amount) ->
                    Recipe(recipe * amount)
                }
            }
            Text("Inputs")
            ScrollableColumn(Modifier.weight(1f)) {
                items(solutionIO.first) {
                    ItemStack(it)
                }
            }
            Text("Outputs")
            ScrollableColumn(Modifier.weight(1f)) {
                items(solutionIO.second) {
                    ItemStack(it)
                }
            }
        }
    }
}