package minerofmillions.recipe_factory.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fasterxml.jackson.databind.ObjectMapper
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.recipe_factory.core.RecipeFactory
import minerofmillions.recipe_factory.core.config.FactoryPlannerPluginSpec
import minerofmillions.recipe_factory.examples.starbound.Item
import org.pf4j.Extension
import java.io.File
import minerofmillions.recipe_factory.examples.starbound.Recipe as SBRecipe

@Extension
class StarboundRecipeFactory : RecipeFactory("Starbound", "Recipe Database") {
    private lateinit var forceGenerateValue: FactoryPlannerPluginSpec.ConfigValue<Boolean>
    private lateinit var starboundInstallationValue: FactoryPlannerPluginSpec.ConfigValue<String>
    private lateinit var unpackDirectoryValue: FactoryPlannerPluginSpec.ConfigValue<String>

    private val forceGenerate by forceGenerateValue
    private val starboundInstallation by starboundInstallationValue
    private val unpackDirectory by unpackDirectoryValue

    override fun FactoryPlannerPluginSpec.Builder.configure() {
        forceGenerateValue = define("forceGenerate", false)
        starboundInstallationValue =
            define("starboundInstallation", """I:\\SteamLibrary\\steamapps\\common\\Starbound""")
        unpackDirectoryValue = define("unpackDirectory", """E:\\SBUnpacked""")
    }

    override suspend fun loadRecipes(setStatus: (@Composable RowScope.() -> Unit) -> Unit): Sequence<Recipe> {
        var childStatusState by mutableStateOf<@Composable () -> Unit>({ })
        setStatus @Composable {
            Column {
                childStatusState()
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }

        val unpackFile = File(unpackDirectory)
        StarboundDatabaseGenerator.generateRecipes(
            forceGenerate, File(starboundInstallation), unpackFile
        ) { childStatusState = it }

        val mapper = ObjectMapper()

        return mapper.readValue<Map<String, SBRecipe>>(
            unpackFile.resolve("RecipeDatabase.json"),
            mapper.typeFactory.constructMapType(Map::class.java, String::class.java, SBRecipe::class.java)
        ).asSequence().map { (name, recipe) ->
            Recipe(name, recipe.input.map(Item::asItemStack), recipe.output.map(Item::asItemStack))
        }.also {
            setStatus @Composable { }
        }
    }
}