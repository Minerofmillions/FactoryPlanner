package minerofmillions.recipe_factory.core

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.pf4j.ExtensionPoint

abstract class RecipeParser(val group: String, val name: String) : ExtensionPoint {
    abstract suspend fun loadRecipes(setStatus: (@Composable RowScope.() -> Unit) -> Unit): List<Recipe>
    @Composable
    open fun ConfigMenu(onCancel: () -> Unit, onConfirm: () -> Unit) = Column {
        Text(group, style = MaterialTheme.typography.h1)
        Text(name, style = MaterialTheme.typography.h2)
        Row {
            Button(onClick = onCancel) {
                Text("Select a different parser.")
            }
            Button(onClick = onConfirm) {
                Text("Confirm use parser")
            }
        }
    }
    open val enabled: Boolean = true
}