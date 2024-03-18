package minerofmillions.recipe_factory.examples

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.fasterxml.jackson.databind.ObjectMapper
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.recipe_factory.core.RecipeFactory
import minerofmillions.recipe_factory.core.config.FactoryPlannerPluginSpec
import mu.KotlinLogging
import org.pf4j.Extension
import java.io.File

@Extension
class MinecraftKJSExportFactory : RecipeFactory("Minecraft", "KubeJS Export") {
    private val logger = KotlinLogging.logger {}
    lateinit var exportPath: FactoryPlannerPluginSpec.ConfigValue<String> private set
    private val exportPathValue by exportPath
    override suspend fun loadRecipes(setStatus: (@Composable RowScope.() -> Unit) -> Unit): Sequence<Recipe> {
        val exportDir = File(exportPathValue)
        if (!exportDir.isDirectory) {
            logger.error("Export directory doesn't exist.")
            setStatus { Text("Export directory doesn't exist. Cancelling import.") }
            return emptySequence()
        }
        val index = exportDir.resolve("index.json")
        if (!index.isFile) {
            logger.error("Export index doesn't exist.")
            setStatus { Text("Export index doesn't exist. Cancelling import.") }
            return emptySequence()
        }
        val exported = mapper.readValue<List<String>>(
            index,
            mapper.typeFactory.constructCollectionType(List::class.java, String::class.java)
        ).filter { '/' in it }
        val groupedExported = exported.groupBy { it.split('/').first() }
        val addedRecipes = groupedExported["added_recipes"] ?: emptyList()
        val lootTables = groupedExported["loot_tables"] ?: emptyList()
        val recipes = groupedExported["recipes"] ?: emptyList()
        val removedRecipes = groupedExported["removed_recipes"] ?: emptyList()
        val tags = (groupedExported["tags"] ?: emptyList()).groupBy {
            it.split('/').component2()
        }
        logger.debug { lootTables }
        TODO("Not yet implemented")
    }

    override fun FactoryPlannerPluginSpec.Builder.configure() {
        exportPath = define("exportPath", "")
    }

    companion object {
        private val mapper = ObjectMapper()
    }
}