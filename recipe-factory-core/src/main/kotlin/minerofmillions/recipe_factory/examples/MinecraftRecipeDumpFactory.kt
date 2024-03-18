package minerofmillions.recipe_factory.examples

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import minerofmillions.recipe_factory.core.*
import minerofmillions.recipe_factory.core.config.FactoryPlannerPluginSpec
import minerofmillions.recipe_factory.examples.minecraft.Tag
import minerofmillions.utils.times
import mu.KotlinLogging
import org.ojalgo.scalar.RationalNumber
import org.ojalgo.scalar.RationalNumber.valueOf
import org.pf4j.Extension
import org.pf4j.ExtensionPoint
import java.io.File
import java.util.*

@Extension
class MinecraftRecipeDumpFactory : RecipeFactory("Minecraft", "Recipe Dump") {
    private val logger = KotlinLogging.logger { }

    abstract class TypeParser(val type: String) : ExtensionPoint {
        lateinit var toIngredient: (JsonNode) -> Ingredient
        abstract fun parseRecipe(id: String, recipeObject: JsonNode): Sequence<Recipe>

        fun JsonNode.toIngredient() = toIngredient(this)
    }

    lateinit var dumpPath: FactoryPlannerPluginSpec.ConfigValue<String> private set
    private val dumpPathValue by dumpPath
    override fun FactoryPlannerPluginSpec.Builder.configure() {
        dumpPath = define("dumpPath", File(System.getProperty("user.home"), "RecipeDump").path)
    }

    override suspend fun loadRecipes(
        setStatus: (@Composable() (RowScope.() -> Unit)) -> Unit,
    ): Sequence<Recipe> {
        val dumpDir = File(dumpPathValue)
        if (!dumpDir.isDirectory) {
            logger.error("Dump directory doesn't exist.")
            return emptySequence()
        }
        val tagFile = dumpDir.resolve("tags.json")
        if (!tagFile.exists()) {
            logger.error("Tag dump doesn't exist.")
            return emptySequence()
        }
        val recipesFile = dumpDir.resolve("recipes.json")
        if (!recipesFile.exists()) {
            logger.error("Recipe dump doesn't exist.")
            return emptySequence()
        }

        logger.info("Reading tags")
        val tagMap = mapper.readValue(tagFile, TagMapTypeToken)
        TagIngredient.tags = tagMap

        val parsers = PluginManager.getExtensions(TypeParser::class.java)
            .onEach { p -> p.toIngredient = { Ingredient.createIngredient(it) } }.associateBy(TypeParser::type)

        return mapper.readTree(recipesFile.bufferedReader()).fields().asSequence().mapNotNull { (id, recipeObject) ->
                val type = recipeObject["type"].asText()
                if (type == "pneumaticcraft:refinery") Unit
                parsers[type]?.parseRecipe(id, recipeObject)
            }.flatten()
    }

    companion object {
        private val mapper = ObjectMapper()
    }

    private object TagMapTypeToken : TypeReference<Map<String, SortedMap<String, Tag>>>()
}

sealed class Ingredient {
    abstract val possibilities: List<ItemStack>

    abstract operator fun times(n: RationalNumber): Ingredient
    operator fun times(n: Long) = times(valueOf(n))
    operator fun times(n: Int) = times(valueOf(n.toLong()))

    companion object {
        @JvmStatic
        @JsonCreator
        fun createIngredient(element: JsonNode): Ingredient =
            if (element.isTextual) ItemIngredient(element.asText(), 1, 1.0)
            else if (element.isArray) OptionIngredient(element.map(::createIngredient))
            else {
                val chance = element["chance"]?.asDouble() ?: 1.0
                if (element.has("fluid")) ItemIngredient(
                    element["fluid"].asText(),
                    RationalNumber.of(element["amount"]?.asLong() ?: 1000, 1000),
                    chance,
                    element["nbt"]
                )
                else {
                    val count = element["count"]?.asInt() ?: 1
                    if (element.has("tag")) TagIngredient(element["tag"].asText(), count, chance)
                    else ItemIngredient(element["item"].asText(), count, chance, element["nbt"])
                }
            }
    }
}

data class ItemIngredient(
    private val name: String,
    private val count: RationalNumber,
    private val chance: RationalNumber = RationalNumber.ONE,
    private val nbt: JsonNode? = null,
) : Ingredient() {
    constructor(name: String, count: Long, chance: RationalNumber = RationalNumber.ONE, nbt: JsonNode? = null) : this(
        name, RationalNumber.of(count, 1), chance, nbt
    )

    constructor(name: String, count: Int, chance: RationalNumber = RationalNumber.ONE, nbt: JsonNode? = null) : this(
        name, count.toLong(), chance, nbt
    )

    constructor(name: String, count: RationalNumber, chance: Double, nbt: JsonNode? = null) : this(
        name, count, valueOf(chance), nbt
    )

    constructor(name: String, count: Long, chance: Double, nbt: JsonNode? = null) : this(
        name, RationalNumber.of(count, 1), chance, nbt
    )

    constructor(name: String, count: Int, chance: Double, nbt: JsonNode? = null) : this(
        name, count.toLong(), chance, nbt
    )

    override val possibilities = listOf((name + (nbt ?: "")) * (count * chance))

    override fun times(n: RationalNumber): Ingredient = ItemIngredient(name, count * n, chance, nbt)
}

data class TagIngredient(
    private val tag: String,
    private val count: RationalNumber,
    private val chance: RationalNumber = RationalNumber.ONE,
) : Ingredient() {
    constructor(tag: String, count: Long, chance: RationalNumber = RationalNumber.ONE) : this(
        tag, RationalNumber.of(count, 1), chance
    )

    constructor(tag: String, count: Int, chance: RationalNumber = RationalNumber.ONE) : this(
        tag, count.toLong(), chance
    )

    constructor(tag: String, count: RationalNumber, chance: Double) : this(
        tag, count, valueOf(chance)
    )

    constructor(tag: String, count: Long, chance: Double) : this(
        tag, RationalNumber.of(count, 1), valueOf(chance)
    )

    constructor(tag: String, count: Int, chance: Double) : this(tag, count.toLong(), chance)

    private val values
        get() = (tags["items"]!![tag] ?: tags["fluids"]!![tag])?.values ?: listOf("#$tag")
    override val possibilities: List<ItemStack>
        get() = values.map { it * (count * chance) }

    override fun times(n: RationalNumber): Ingredient = TagIngredient(tag, count * n, chance)

    companion object {
        lateinit var tags: Map<String, Map<String, Tag>>
    }
}

data class OptionIngredient(private val options: List<Ingredient>) : Ingredient() {
    override val possibilities = options.flatMap { it.possibilities }

    override fun times(n: RationalNumber): Ingredient = OptionIngredient(options.map { it * n })
}

fun Collection<Ingredient>.permutations(): Sequence<List<ItemStack>> = when (size) {
    0 -> emptySequence()
    1 -> first().possibilities.asSequence().map(::listOf)
    else -> first().possibilities.asSequence().flatMap { first ->
        drop(1).permutations().map { listOf(first, *it.toTypedArray()) }
    }
}
