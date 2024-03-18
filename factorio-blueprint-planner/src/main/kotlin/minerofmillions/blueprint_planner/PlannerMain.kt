package minerofmillions.blueprint_planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import minerofmillions.utils.ui.BorderedColumn
import minerofmillions.utils.ui.ScrollableColumn

val skippedCategories = listOf(
    "incineration",
    "creative-mod_energy-absorption",
    "OSM-crafting-void",
    "OSM-removed",
    "flaring",
    "fuel-incineration",
    "creative-mod_free-fluids",
    "equipment-change",
)

@OptIn(ExperimentalDecomposeApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()

    val root = RootComponent(DefaultComponentContext(lifecycle))

    application {
        fun exit() {
            exitApplication()
        }

        val windowState = rememberWindowState()

        LifecycleController(lifecycle, windowState)

        Window(onCloseRequest = ::exit, title = "Factorio Blueprint Planner") {
            RootContent(root)
        }
    }
}

@Composable
fun RootContent(component: RootComponent) {
    val transportBelts by component.transportBelts.subscribeAsState()
    val undergroundBelts by component.undergroundBelts.subscribeAsState()
    val assemblingMachines by component.assemblingMachines.subscribeAsState()
    val recipes by component.recipes.subscribeAsState()
    val recipeCategories by component.recipeCategories.map { categories ->
        categories.mapValues { (_, recipesInCategory) ->
            recipesInCategory.mapNotNull { recipeInCategory -> recipes.firstOrNull { it.name == recipeInCategory } }
        }
    }.subscribeAsState()

    Row {
        Column(Modifier.weight(1f)) {
            Text("Transport Belts")
            ScrollableColumn(Modifier.weight(1f)) {
                items(transportBelts) { belt ->
                    BorderedColumn(1.dp, MaterialTheme.colors.secondary, modifier = Modifier.fillMaxWidth()) {
                        Text(belt.name)
                        Text("%.1f i/s".format(belt.throughput))
                        val underground = undergroundBelts.first { it.name == belt.relatedUndergroundBelt }
                        Text("${underground.maxDistance} tiles")
                    }
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text("Assembling Machines")
            ScrollableColumn {
                items(assemblingMachines) {
                    BorderedColumn(1.dp, MaterialTheme.colors.secondary, modifier = Modifier.fillMaxWidth()) {
                        val fluidBoxes by it.fluidBoxes.subscribeAsState()

                        Text(it.name)
                        Text(it.energyUsage)
                        Text(it.craftingCategories.joinToString())
                        Text("Crafting Speed: %.2f".format(it.craftingSpeed))
                        Text("Allowed in space: ${it.allowedInSpace}")

                        fluidBoxes.forEach {
                            Row {
                                Text(it.productionType.name)
                                Box(Modifier.width(4.dp))
                                Text(it.pipeConnections.joinToString())
                            }
                        }
                        if (fluidBoxes.isNotEmpty()) Text(it.fluidBoxesOffWhenNoFluidRecipe.toString())
                    }
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text("Recipes")
            ScrollableColumn {
                recipeCategories.entries.filter { it.value.isNotEmpty() }.forEach { (category, recipesInCategory) ->
                    item {
                        Text(category)
                    }
                    items(recipesInCategory) {
                        BorderedColumn(1.dp, MaterialTheme.colors.secondary, modifier = Modifier.fillMaxWidth()) {
                            Text(it.name)
                            Text(it.ingredients.joinToString())
                            Text(it.results.joinToString())
                        }
                    }
                }
            }
        }
    }
}