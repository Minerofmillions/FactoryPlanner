package minerofmillions.recipe_factory.examples

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import minerofmillions.recipe_factory.core.Recipe
import minerofmillions.recipe_factory.core.RecipeFactory
import minerofmillions.recipe_factory.core.config.FactoryPlannerPluginSpec
import minerofmillions.recipe_factory.examples.factorio.FactorioRecipe
import minerofmillions.recipe_factory.examples.factorio.Technology
import minerofmillions.recipe_factory.examples.factorio.UnlockRecipeEffect
import org.pf4j.Extension
import java.io.File

@Extension
class FactorioRecipeFactory : RecipeFactory("Factorio", "Data Exporter to JSON") {
    lateinit var outputPath: FactoryPlannerPluginSpec.ConfigValue<String> private set
    private val outputPathValue by outputPath
    override suspend fun loadRecipes(setStatus: (@Composable RowScope.() -> Unit) -> Unit): Sequence<Recipe> {
        setStatus {
            Text("Loading technologies...")
        }
        val unlockedRecipes = mapper.readValue<Map<String, Technology>>(
            File(outputPathValue, "technology.json"),
            mapper.typeFactory.constructMapType(Map::class.java, String::class.java, Technology::class.java)
        ).values.flatMap(Technology::effects).filterIsInstance<UnlockRecipeEffect>().map(UnlockRecipeEffect::recipe)
            .toSet()
        setStatus {
            Text("Loading recipes...")
        }
        val allRecipes = mapper.readValue<Map<String, FactorioRecipe>>(
            File(outputPathValue, "recipe.json"),
            mapper.typeFactory.constructMapType(Map::class.java, String::class.java, FactorioRecipe::class.java)
        ).values
        val ingameRecipes = allRecipes.filter { recipe -> recipe.enabled || recipe.name in unlockedRecipes }
        return ingameRecipes.asSequence().filterNot { it.name.matches(barrelingRegex) }.map(FactorioRecipe::toRecipe)
    }

    override fun FactoryPlannerPluginSpec.Builder.configure() {
        outputPath = define("outputPath", "")
    }

    companion object {
        internal val mapper = ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        private val barrelingRegex = Regex("""(fill|empty)-.+-barrel""")
    }
}