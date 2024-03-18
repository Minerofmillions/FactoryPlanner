package minerofmillions.recipe_factory.app.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import minerofmillions.recipe_factory.app.components.ParserConfigComponent
import minerofmillions.utils.ui.Config
import minerofmillions.recipe_factory.core.config.FactoryPlannerPluginSpec

@Composable
fun ParserConfig(component: ParserConfigComponent) = Column {
    val parser = remember { component.parser }

    Text(parser.group, style = MaterialTheme.typography.h1)
    Text(parser.name, style = MaterialTheme.typography.h2)
    Config(parser.configSpec) {
        when (it) {
            is FactoryPlannerPluginSpec.BooleanValue -> BooleanValue(it)
            is FactoryPlannerPluginSpec.StringValue -> StringValue(it)
            else -> Row() {
                Text((it as FactoryPlannerPluginSpec.ConfigValue<*>).toString())
                Text(it.klass.qualifiedName ?: "")
            }
        }
    }
    Row {
        Button(component::cancel) {
            Text("Use different recipe factory")
        }
        Button({ component.confirm() }) {
            Text("Use this recipe factory")
        }
    }
}

@Composable
fun BooleanValue(value: FactoryPlannerPluginSpec.ConfigValue<Boolean>, modifier: Modifier = Modifier) = Row(modifier, verticalAlignment = Alignment.CenterVertically) {
    val isTrue by value.subscribeAsState()
    Checkbox(isTrue, { value.set(it) })
}

@Composable
fun StringValue(value: FactoryPlannerPluginSpec.ConfigValue<String>, modifier: Modifier = Modifier) {
    val currentValue by value.subscribeAsState()
    TextField(currentValue, { value.set(it) }, modifier, label = { Text(value.path) })
}
