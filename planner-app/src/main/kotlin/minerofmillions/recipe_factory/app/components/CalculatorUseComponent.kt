package minerofmillions.recipe_factory.app.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import minerofmillions.recipe_factory.core.componentCoroutineScope
import minerofmillions.recipe_factory.core.*
import org.ojalgo.scalar.RationalNumber

class CalculatorUseComponent(
    componentContext: ComponentContext,
    val calculator: Calculator,
    private val onReturnToSetup: () -> Unit,
    private val onSelectProduct: () -> Unit
) :
    ComponentContext by componentContext {
    private val scope = componentCoroutineScope()

    private val _isRunning = MutableValue(false)
    val isRunning: Value<Boolean> = _isRunning

    private val _products = MutableValue(emptyList<ItemStack>())
    val products: Value<List<ItemStack>> = _products

    private val _hasProducts = _products.map(List<ItemStack>::isNotEmpty)
    val hasProducts: Value<Boolean> = _hasProducts

    private val _solution = MutableValue(emptyMap<Recipe, RationalNumber>())
    val solution: Value<Map<Recipe, RationalNumber>> = _solution

    private val _editingProduct = MutableValue("" * 1)
    val editingProduct: Value<ItemStack> = _editingProduct

    val solutionIO: Value<Pair<List<ItemStack>, List<ItemStack>>> = _solution.map {
        val i = mutableListOf<ItemStack>()
        val o = mutableListOf<ItemStack>()
        Recipe.generateIO(i, o, it)
        i to o
    }

    fun addProduct() {
        _products.value = (_products.value + _editingProduct.value).mergeStacks()
        _editingProduct.value = "" * 1
    }

    fun removeProduct(productName: String) {
        _products.value = products.value.filterNot { it.item == productName }
    }

    fun startCalculation() {
        scope.launch(Dispatchers.IO) {
            _isRunning.value = true
            calculator.solve(_products.value)
                .onEach { _solution.value = it }
                .onCompletion { _isRunning.value = false }
                .collect()
        }
    }

    fun returnToSetup() = onReturnToSetup()
    fun selectProduct() = onSelectProduct()

    fun setProduct(item: String) {
        _editingProduct.value = item * _editingProduct.value.amount
    }

    fun setProductAmount(amount: RationalNumber) {
        _editingProduct.value = _editingProduct.value.item * amount
    }
}