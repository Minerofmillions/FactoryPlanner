package minerofmillions.recipe_factory.app.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import kotlinx.coroutines.launch
import minerofmillions.recipe_factory.app.components.RootComponent.Child.*
import minerofmillions.recipe_factory.core.Calculator
import minerofmillions.recipe_factory.core.RecipeFactory
import minerofmillions.recipe_factory.core.componentCoroutineScope

class RootComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    private val coroutineScope = componentCoroutineScope()

    private val navigation = StackNavigation<Config>()
    val stack: Value<ChildStack<*, Child>> =
        childStack(navigation, Config.CalculatorSetup, handleBackButton = true, childFactory = ::child)

    fun onBackClicked(toIndex: Int) {
        navigation.popTo(toIndex)
    }

    private fun child(config: Config, componentContext: ComponentContext): Child = when (config) {
        is Config.ParserList -> ParserListChild(settingsComponent(componentContext))
        is Config.SelectProduct -> ProductSelectChild(detailsComponent(componentContext, config))
        is Config.CalculatorSetup -> CalculatorSetupChild(calculatorSetupComponent(componentContext))
        is Config.CalculatorUse -> CalculatorUseChild(calculatorUseComponent(componentContext, config))
        is Config.ParserConfig -> ParserConfigChild(parserConfigComponent(componentContext, config))
    }

    private fun settingsComponent(context: ComponentContext): ParserListComponent =
        ParserListComponent(context = context, onShowParser = {
            navigation.pop()
            navigation.push(Config.ParserConfig(it))
        }, onCancel = navigation::pop)

    private fun detailsComponent(context: ComponentContext, config: Config.SelectProduct): SelectProductComponent =
        SelectProductComponent(
            componentContext = context, validProducts = config.validProducts, onCancel = navigation::pop, onSelect = {
                navigation.pop()
                (stack.active.instance as CalculatorUseChild).component.setProduct(it)
            }
        )

    private fun calculatorSetupComponent(
        context: ComponentContext,
    ): CalculatorSetupComponent = CalculatorSetupComponent(componentContext = context,
        onParserImport = { navigation.push(Config.ParserList) },
        onFinalizeCalculator = { navigation.push(Config.CalculatorUse(it)) })

    private fun calculatorUseComponent(context: ComponentContext, config: Config.CalculatorUse) =
        CalculatorUseComponent(componentContext = context,
            calculator = config.calculator,
            onReturnToSetup = navigation::pop,
            onSelectProduct = { navigation.push(Config.SelectProduct(config.calculator.validProducts)) })

    private fun parserConfigComponent(context: ComponentContext, config: Config.ParserConfig) =
        ParserConfigComponent(
            context = context,
            parser = config.parser,
            onCancel = { navigation.pop() },
            onConfirm = {
                navigation.pop()
                (stack.value.active.instance as? CalculatorSetupChild)?.component?.setRecipes(emptySequence())
                coroutineScope.launch {
                    (stack.value.active.instance as? CalculatorSetupChild)?.let { setup ->
                        setup.component.setRecipes(
                            config.parser.loadRecipes(setup.component::setStatus)
                        )
                    }
                }
            })

    private sealed interface Config : Parcelable {
        data object ParserList : Config {
            private fun readResolve(): Any = ParserList
        }

        data object CalculatorSetup : Config {
            private fun readResolve(): Any = CalculatorSetup
        }

        data class SelectProduct(val validProducts: Set<String>) : Config

        data class CalculatorUse(val calculator: Calculator) : Config

        data class ParserConfig(val parser: RecipeFactory) : Config
    }

    sealed class Child {
        data class CalculatorSetupChild(val component: CalculatorSetupComponent) : Child()
        data class ParserListChild(val component: ParserListComponent) : Child()
        data class ProductSelectChild(val component: SelectProductComponent) : Child()
        data class CalculatorUseChild(val component: CalculatorUseComponent) : Child()
        data class ParserConfigChild(val component: ParserConfigComponent) : Child()
    }
}