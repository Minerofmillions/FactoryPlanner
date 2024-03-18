package minerofmillions.recipe_factory.core

import org.pf4j.DefaultPluginManager
import java.io.File

object PluginManager {
    private val pluginManager =
        DefaultPluginManager(File("plugins").apply { if (!exists()) mkdir() }.toPath()).apply {
            loadPlugins()
            startPlugins()
        }

    val parsers by lazy { pluginManager.getExtensions(RecipeParser::class.java).filter(RecipeParser::enabled) }
    val factories: List<RecipeFactory> by lazy { pluginManager.getExtensions(RecipeFactory::class.java) }

    fun <T> getExtensions(clazz: Class<T>): List<T> = pluginManager.getExtensions(clazz)

    fun exit() {
        pluginManager.unloadPlugins()
    }
}