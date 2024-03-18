package minerofmillions.recipe_factory.app.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import minerofmillions.recipe_factory.core.*

class CalculatorSetupComponent(
    componentContext: ComponentContext,
    private val onParserImport: () -> Unit,
    private val onFinalizeCalculator: (Calculator) -> Unit
) : ComponentContext by componentContext {
    private val _recipes = MutableValue(emptyList<Recipe>())
    val recipes: Value<List<Recipe>> = _recipes

    private val _primitives = MutableValue(emptySet<String>())
    val primitives: Value<Set<String>> = _primitives

    private val _unusableInputs = MutableValue(emptySet<String>())
    val unusableInputs: Value<Set<String>> = _unusableInputs

    private val _unusableOutputs = MutableValue(emptySet<String>())
    val unusableOutputs: Value<Set<String>> = _unusableOutputs

    private val _calculatorType = MutableValue(Calculator.CalculatorType.OJALGO_ALL_AT_ONCE)
    val calculatorType: Value<Calculator.CalculatorType> = _calculatorType

    val calculatorValid = _recipes.map(List<Recipe>::isNotEmpty)

    val allInputs = _recipes.map(List<Recipe>::getInputItems)
    val allOutputs = _recipes.map(List<Recipe>::getOutputItems)
    val allItems = _recipes.map(List<Recipe>::getAllItems)

    private val _status = MutableValue<@Composable RowScope.() -> Unit> {}
    val status: Value<@Composable RowScope.() -> Unit> = _status

    private fun buildCalculator(): Calculator = Calculator.Builder()
        .recipes(_recipes.value)
        .primitives(_primitives.value)
        .unusableInputs(_unusableInputs.value)
        .unusableOutputs(_unusableOutputs.value)
        .build(_calculatorType.value)

    fun setStatus(status: @Composable RowScope.() -> Unit) {
        _status.value = status
    }

    fun setCalculatorType(type: Calculator.CalculatorType) {
        _calculatorType.value = type
    }

    fun importFromParsers() = onParserImport()
    fun finalizeCalculator() {
        onFinalizeCalculator(buildCalculator())
    }

    fun loadExample() {
        _recipes.value = EXAMPLE_STATE.recipes
        _primitives.value = EXAMPLE_STATE.primitives
        _unusableInputs.value = EXAMPLE_STATE.unusableInputs
        _unusableOutputs.value = EXAMPLE_STATE.unusableOutputs
        _calculatorType.value = EXAMPLE_STATE.calculatorType
    }

    fun addPrimitive(primitive: String) {
        _primitives.value += primitive
    }

    fun removePrimitive(primitive: String) {
        _primitives.value -= primitive
    }

    fun addUnusableInput(inputName: String) {
        _unusableInputs.value += inputName
    }

    fun removeUnusableInput(inputName: String) {
        _unusableInputs.value -= inputName
    }

    fun addUnusableOutput(outputName: String) {
        _unusableOutputs.value += outputName
    }

    fun removeUnusableOutput(outputName: String) {
        _unusableOutputs.value -= outputName
    }

    fun setRecipes(recipes: Sequence<Recipe>) {
        _recipes.value = recipes.toList().sorted()
        _primitives.value = emptySet()
        _unusableInputs.value = emptySet()
        _unusableOutputs.value = emptySet()
    }

    companion object {
        private val EXAMPLE_STATE = SetupState(
            listOf(
                Recipe(
                    "crude-oil",
                    listOf("se-core-fragment-crude-oil" * 1),
                    listOf("crude-oil" * 100)
                ),
                Recipe(
                    "petroleum-gas",
                    listOf("crude-oil" * 100, "water" * 50),
                    listOf("petroleum-gas" * 90)
                ),
                Recipe(
                    "heavy-oil-processing",
                    listOf("crude-oil" * 100, "water" * 10),
                    listOf("heavy-oil" * 70, "light-oil" * 30, "petroleum-gas" * 20)
                ),
                Recipe(
                    "light-oil-processing",
                    listOf("crude-oil" * 100, "water" * 50),
                    listOf("heavy-oil" * 20, "light-oil" * 70, "petroleum-gas" * 30)
                ),
                Recipe(
                    "fuel-from-heavy",
                    listOf("heavy-oil" * 20),
                    listOf("solid-fuel" * 1)
                ),
                Recipe(
                    "fuel-from-light",
                    listOf("light-oil" * 10),
                    listOf("solid-fuel" * 1)
                ),
                Recipe(
                    "fuel-from-petroleum",
                    listOf("petroleum-gas" * 20),
                    listOf("solid-fuel" * 1)
                ),
                Recipe(
                    "heavy-oil-cracking",
                    listOf("water" * 50, "heavy-oil" * 40),
                    listOf("light-oil" * 30)
                ),
                Recipe(
                    "light-oil-cracking",
                    listOf("water" * 50, "light-oil" * 30),
                    listOf("petroleum-gas" * 20)
                ),
                Recipe(
                    "solid-rocket-fuel",
                    listOf("solid-fuel" * 10, "light-oil" * 10),
                    listOf("solid-rocket-fuel" * 1)
                )
            ),
            calculatorType = Calculator.CalculatorType.OJALGO_ALL_AT_ONCE
        )
    }

    private data class SetupState(
        val recipes: List<Recipe> = emptyList(),
        val primitives: Set<String> = emptySet(),
        val unusableInputs: Set<String> = emptySet(),
        val unusableOutputs: Set<String> = emptySet(),
        val calculatorType: Calculator.CalculatorType = Calculator.CalculatorType.OJALGO_ALL_AT_ONCE,
    )
}