package minerofmillions.recipe_factory.app.ui.main

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import minerofmillions.recipe_factory.core.Recipe

@Composable
fun Recipe(recipe: Recipe) {
    var open by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize(0, 0)) }

    Column(Modifier.border(1.dp, MaterialTheme.colors.secondary).padding(2.dp).fillMaxWidth().clickable { open = !open }
        .onSizeChanged { size = it }) {
        Text(recipe.name)

        if (open) {
            val columns = with(GridCells.Adaptive(200.dp)) {
                LocalDensity.current.calculateCrossAxisCellSizes(size.width, 2).size
            }
            Row(Modifier.border(1.dp, MaterialTheme.colors.secondary), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, null)
                Column {
                    recipe.inputs.chunked(columns).forEach { row ->
                        Row {
                            row.forEach {
                                ItemStack(it, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Row(Modifier.border(1.dp, MaterialTheme.colors.secondary), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Remove, null)
                Column {
                    recipe.outputs.chunked(columns).forEach { row ->
                        Row {
                            row.forEach {
                                ItemStack(it, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
