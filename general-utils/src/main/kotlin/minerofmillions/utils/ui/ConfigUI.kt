package minerofmillions.utils.ui

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.electronwill.nightconfig.core.UnmodifiableConfig

@Composable
fun Config(config: UnmodifiableConfig, leafView: @Composable (Any) -> Unit): Unit {
    LazyVerticalGrid(ConfigGridCells(config.entrySet().map(UnmodifiableConfig.Entry::getKey))) {
        config.entrySet().forEach {
            item {
                Text(it.key)
            }
            item {
                val value = it.getValue<Any>()
                if (value is UnmodifiableConfig) Config(value, leafView)
                else leafView(value)
            }
        }
    }
}

private class ConfigGridCells(keys: Collection<String>) : GridCells {
    private val maxKeyLength = keys.maxOf(String::length)

    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        val gridSizeWithoutSpacing = availableSize - spacing
        val keySize = (maxKeyLength * 8).sp.roundToPx()
        return listOf(keySize, gridSizeWithoutSpacing - keySize)
    }
}