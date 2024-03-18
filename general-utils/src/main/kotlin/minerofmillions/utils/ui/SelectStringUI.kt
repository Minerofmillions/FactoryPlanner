package minerofmillions.utils.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun SelectString(values: Collection<String>, onSelect: (String) -> Unit) = Column {
    var filter by remember { mutableStateOf("") }

    TextField(filter, { filter = it }, label = { Text("Filter") })
    ScrollableColumn {
        items(values.filter { filter in it }) {
            Text(it, Modifier.clickable { onSelect(it) })
        }
    }
}