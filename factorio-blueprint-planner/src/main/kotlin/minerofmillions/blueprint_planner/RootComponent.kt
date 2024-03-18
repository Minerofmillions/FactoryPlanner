package minerofmillions.blueprint_planner

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import minerofmillions.blueprint_planner.entities.AssemblingMachine
import minerofmillions.blueprint_planner.entities.FactorioRecipe
import minerofmillions.blueprint_planner.entities.TransportBelt
import minerofmillions.blueprint_planner.entities.UndergroundBelt
import minerofmillions.recipe_factory.core.componentCoroutineScope
import java.io.File
import java.util.*

class RootComponent(context: ComponentContext) : ComponentContext by context {
    private val scope = componentCoroutineScope()

    private val rawEntityData = scope.async(Dispatchers.IO) {
        mapper.readValue<Map<String, JsonNode>>(
            factorioOutput.resolve("raw.json"),
            mapper.typeFactory.constructMapType(Map::class.java, String::class.java, JsonNode::class.java)
        )
    }

    private val _recipes = MutableValue(emptyList<FactorioRecipe>())
    val recipes: Value<List<FactorioRecipe>> = _recipes

    private val _recipeCategories = MutableValue(emptyMap<String, Collection<String>>())
    val recipeCategories: Value<Map<String, Collection<String>>> = _recipeCategories

    private val _assemblingMachines = MutableValue(emptyList<AssemblingMachine>())
    val assemblingMachines: Value<List<AssemblingMachine>> = _assemblingMachines

    private val _transportBelts = MutableValue(emptyList<TransportBelt>())
    val transportBelts: Value<List<TransportBelt>> = _transportBelts

    private val _undergroundBelts = MutableValue(emptyList<UndergroundBelt>())
    val undergroundBelts: Value<List<UndergroundBelt>> = _undergroundBelts

    init {
        scope.launch(Dispatchers.IO) {
            _recipes.value = mapper.readValue<Map<String, FactorioRecipe>>(
                factorioOutput.resolve("recipes.json"),
                mapper.typeFactory.constructMapType(Map::class.java, String::class.java, FactorioRecipe::class.java)
            ).values
                .sortedBy(FactorioRecipe::name)
                .filterNot { it.name.startsWith("bpsb-") }
                .filterNot { it.name.startsWith("creative-mod_") }
        }
        scope.launch(Dispatchers.IO) {
            _recipeCategories.value = mapper.readValue(
                factorioOutput.resolve("recipe_categories.json"),
                object : TypeReference<SortedMap<String, SortedSet<String>>>() {}
            )
                .filterKeys { it !in skippedCategories }
        }
        scope.launch(Dispatchers.IO) {
            _assemblingMachines.value = getEntitiesOfType("assembling_machines", AssemblingMachine::class.java)
                .sortedBy(AssemblingMachine::name)
                .filterNot { it.name.startsWith("bpsb-ils-") }
                .filterNot { it.craftingCategories.all(skippedCategories::contains) }
        }
        scope.launch(Dispatchers.IO) {
            _transportBelts.value = getEntitiesOfType("transport_belts", TransportBelt::class.java)
                .sortedBy(TransportBelt::speed)
        }
        scope.launch(Dispatchers.IO) {
            _undergroundBelts.value = getEntitiesOfType("underground_belts", UndergroundBelt::class.java)
                .sortedBy(UndergroundBelt::speed)
        }
    }

    private suspend fun <T : Any> getEntitiesOfType(entityType: String, klass: Class<T>): List<T> {
        val entities = readStringList("$entityType.json")
        val raw = rawEntityData.await()
        return entities.mapNotNull(raw::get).map {
            mapper.treeToValue<T>(it, klass)
        }
    }

    companion object {
        val mapper = ObjectMapper()
//            .registerModule(
//                SimpleModule()
//                    .addDeserializer(FactorioRecipe::class.java, FactorioRecipe.Companion)
//                    .addDeserializer(FactorioItemStack::class.java, FactorioItemStack.Companion)
//            )
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)!!

        private object StringListType : TypeReference<List<String>>()

        private val factorioOutput = File("I:\\FactorioOutput")

        private fun readStringList(fileName: String) =
            mapper.readValue<List<String>>(factorioOutput.resolve(fileName), StringListType)!!
    }
}