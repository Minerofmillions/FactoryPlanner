package minerofmillions.recipe_factory.app.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import minerofmillions.recipe_factory.app.components.CalculatorSetupComponent
import minerofmillions.utils.ui.BorderedColumn
import minerofmillions.utils.ui.RadioList
import minerofmillions.utils.ui.ScrollableColumn
import minerofmillions.utils.ui.SelectString
import minerofmillions.recipe_factory.core.Calculator

@Composable
fun CalculatorSetup(component: CalculatorSetupComponent) {
    val calculatorTypes = remember { Calculator.CalculatorType.entries }

    val status by component.status.subscribeAsState()
    val recipes by component.recipes.subscribeAsState()
    val primitives by component.primitives.subscribeAsState()
    val unusableInputs by component.unusableInputs.subscribeAsState()
    val unusableOutputs by component.unusableOutputs.subscribeAsState()
    val calculatorType by component.calculatorType.subscribeAsState()

    val allInputs by component.allInputs.subscribeAsState()
    val allOutputs by component.allOutputs.subscribeAsState()
    val allItems by component.allItems.subscribeAsState()

    val calculatorValid by component.calculatorValid.subscribeAsState()

    var selectingPrimitive by remember { mutableStateOf(false) }
    var selectingUnusableInput by remember { mutableStateOf(false) }
    var selectingUnusableOutput by remember { mutableStateOf(false) }

    Window(
        visible = selectingPrimitive,
        onCloseRequest = { selectingPrimitive = false },
        title = "Select Primitive"
    ) {
        SelectString(allItems) {
            component.addPrimitive(it)
            selectingPrimitive = false
        }
    }
    Window(
        visible = selectingUnusableInput,
        onCloseRequest = { selectingUnusableInput = false },
        title = "Select Unusable Input"
    ) {
        SelectString(allInputs) {
            component.addUnusableInput(it)
            selectingUnusableInput = false
        }
    }
    Window(
        visible = selectingUnusableOutput,
        onCloseRequest = { selectingUnusableOutput = false },
        title = "Select Unusable Output"
    ) {
        SelectString(allOutputs) {
            component.addUnusableOutput(it)
            selectingUnusableOutput = false
        }
    }

    Column {
        Row(Modifier.weight(1f)) {
            BorderedColumn(1.dp, MaterialTheme.colors.secondary, modifier = Modifier.weight(1f)) {
                Text("Recipes")
                ScrollableColumn(Modifier.weight(1f)) {
                    items(recipes) {
                        Recipe(it)
                    }
                }
                Row {
                    Button(component::loadExample) {
                        Text("Load example setup")
                    }
                    Button(component::importFromParsers) {
                        Text("Import from parsers")
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                BorderedColumn(1.dp, MaterialTheme.colors.secondary, modifier = Modifier.weight(1f)) {
                    Text("Primitives")
                    ScrollableColumn(Modifier.weight(1f)) {
                        items(primitives.toList()) {
                            Text(it, Modifier.clickable { component.removePrimitive(it) })
                        }
                    }
                    Button({ selectingPrimitive = true }) {
                        Text("Select Primitive")
                    }
                }
                BorderedColumn(1.dp, MaterialTheme.colors.secondary, modifier = Modifier.weight(1f)) {
                    Text("Unusable Inputs")
                    ScrollableColumn(Modifier.weight(1f)) {
                        items(unusableInputs.toList()) {
                            Text(it, Modifier.clickable { component.removeUnusableInput(it) })
                        }
                    }
                    Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = CenterVertically) {
                        Button({ selectingUnusableInput = true }) {
                            Text("Select Unusable Input")
                        }
                    }
                }
                BorderedColumn(1.dp, MaterialTheme.colors.secondary, modifier = Modifier.weight(1f)) {
                    Text("Unusable Outputs")
                    ScrollableColumn(Modifier.weight(1f)) {
                        items(unusableOutputs.toList()) {
                            Text(it, Modifier.clickable { component.removeUnusableOutput(it) })
                        }
                    }
                    Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = CenterVertically) {
                        Button({ selectingUnusableOutput = true }) {
                            Text("Select Unusable Output")
                        }
                    }
                }
                RadioList(
                    calculatorTypes,
                    calculatorType,
                    onItemSelect = component::setCalculatorType,
                    label = { Text(it.name) },
                    modifier = Modifier.weight(1f))
                BorderedColumn(1.dp, MaterialTheme.colors.secondary, modifier = Modifier.fillMaxWidth()) {
                    val calculateSubtree by Calculator.calculateSubtreeValue.subscribeAsState()
                    val commonRecipes by Calculator.commonRecipesValue.subscribeAsState()
                    Row(verticalAlignment = CenterVertically) {
                        Checkbox(
                            calculateSubtree,
                            { Calculator.SHOULD_CALCULATE_SUB_TREE_USING_MATRIX = it })
                        Text("Calculate Subtrees Using Matrix?")
                    }
                    TextField(commonRecipes.toString(), {
                        it.toIntOrNull()?.takeIf { it > 0 }?.let {
                            Calculator.MAX_COMMON_RECIPES = it
                        }
                    }, label = { Text("Number of Common Recipes") })
                }
            }
        }
        Row {
            Button(component::finalizeCalculator, enabled = calculatorValid) {
                Text("Finalize calculator")
            }
        }
        Row {
            status()
        }
    }
}