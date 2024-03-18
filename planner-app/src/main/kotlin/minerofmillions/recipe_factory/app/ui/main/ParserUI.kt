package minerofmillions.recipe_factory.app.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import minerofmillions.recipe_factory.app.components.ParserListComponent
import minerofmillions.utils.ui.ScrollableColumn
import minerofmillions.recipe_factory.core.RecipeFactory

@Composable
fun ParserGroup(group: String, groupedParsers: Map<String, RecipeFactory>, onParserClicked: (RecipeFactory) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { open = !open }) {
        Icon(if (open) Icons.Default.Remove else Icons.Default.Add, null)
        Text(group)
    }

    if (open) groupedParsers.forEach { (name, parser) ->
        Text(name, Modifier.fillMaxWidth().clickable {
            onParserClicked(parser)
        })
    }

}

@Composable
fun Settings(component: ParserListComponent) = Row {
    val parsersList = remember { component.parsers.toList() }
    Column(Modifier.weight(1f)) {
        Text("Parsers")
        ScrollableColumn(Modifier.weight(1f)) {
            items(parsersList) { (group, groupedParsers) ->
                ParserGroup(group, groupedParsers) { component.showParser(it) }
            }
        }
        Button({ component.cancel() }) {
            Text("Cancel")
        }
    }
}