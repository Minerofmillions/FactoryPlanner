package minerofmillions.utils.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <E> RadioList(options: List<E>, selected: E, modifier: Modifier = Modifier, label: @Composable RowScope.(E) -> Unit = { Text(it.toString()) }, onItemSelect: (E) -> Unit) {
    if (options.size > 1) ScrollableColumn(modifier) {
        items(options) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected == it, onClick = { onItemSelect(it) })
                label(it)
            }
        }
    }
}