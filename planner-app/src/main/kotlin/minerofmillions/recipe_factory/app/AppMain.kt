package minerofmillions.recipe_factory.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import minerofmillions.recipe_factory.app.components.RootComponent
import minerofmillions.recipe_factory.app.components.RootComponent.Child.*
import minerofmillions.recipe_factory.app.ui.main.*
import minerofmillions.recipe_factory.core.PluginManager

@OptIn(ExperimentalDecomposeApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()

    val root = RootComponent(DefaultComponentContext(lifecycle))

    application {
        fun exit() {
            PluginManager.exit()
            exitApplication()
        }

        val windowState = rememberWindowState()

        LifecycleController(lifecycle, windowState)

        Window(onCloseRequest = ::exit, title = "Factory Planner") {
            RootContent(root, Modifier.fillMaxSize())
        }
    }
}

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    Children(component.stack, modifier, stackAnimation(fade() + scale())) {
        when (val child = it.instance) {
            is ProductSelectChild -> ProductSelect(child.component)
            is ParserListChild -> Settings(child.component)
            is CalculatorSetupChild -> CalculatorSetup(child.component)
            is CalculatorUseChild -> CalculatorUse(child.component)
            is ParserConfigChild -> ParserConfig(child.component)
        }
    }
}