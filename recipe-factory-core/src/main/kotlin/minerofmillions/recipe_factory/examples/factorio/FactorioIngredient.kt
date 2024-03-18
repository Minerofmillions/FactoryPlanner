package minerofmillions.recipe_factory.examples.factorio

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import minerofmillions.recipe_factory.core.ItemStack
import minerofmillions.utils.div
import minerofmillions.utils.times
import org.ojalgo.scalar.RationalNumber

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    Type(FluidIngredient::class, name = "fluid"),
    Type(ItemIngredient::class, name = "item")
)
interface FactorioIngredient {
    val averageAmount: RationalNumber
    fun toItemStack(): ItemStack
}

@JsonDeserialize(using = FluidIngredient.Companion::class)
data class FluidIngredient(
    val name: String,
    val amountMin: Long,
    val amountMax: Long,
    val tempMin: Long?,
    val tempMax: Long?,
    val probability: RationalNumber,
) : FactorioIngredient {
    override val averageAmount = probability * (amountMin + amountMax) / 2

    override fun toItemStack() = if (tempMin != null) ItemStack("$name@[$tempMin,$tempMax]", averageAmount)
    else ItemStack(name, averageAmount)

    companion object : StdDeserializer<FluidIngredient>(FluidIngredient::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FluidIngredient {
            val obj = p.readValueAsTree<JsonNode>()
            val name = obj["name"].asText()
            val amount = obj["amount"]?.asLong()
            val amountMin = amount ?: obj["amount_min"].asLong()
            val amountMax = amount ?: obj["amount_max"].asLong()
            val temperature = obj["temperature"]?.asLong()
            val tempMin = temperature ?: obj["minimum_temperature"]?.asLong()
            val tempMax = temperature ?: obj["maximum_temperature"]?.asLong()
            val probability = RationalNumber.valueOf(obj["probability"]?.asDouble() ?: 1.0)
            return FluidIngredient(name, amountMin, amountMax, tempMin, tempMax, probability)
        }
    }
}

@JsonDeserialize(using = ItemIngredient.Companion::class)
data class ItemIngredient(
    val name: String,
    val amountMin: Long,
    val amountMax: Long,
    val probability: RationalNumber
) : FactorioIngredient {
    override val averageAmount = probability * (amountMin + amountMax) / 2

    override fun toItemStack() = ItemStack(name, averageAmount)

    companion object : StdDeserializer<ItemIngredient>(ItemIngredient::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ItemIngredient {
            val obj = p.readValueAsTree<JsonNode>()
            val name = obj["name"].asText()
            val amount = obj["amount"]?.asLong()
            val amountMin = amount ?: obj["amount_min"].asLong()
            val amountMax = amount ?: obj["amount_max"].asLong()
            val probability = RationalNumber.valueOf(obj["probability"]?.asDouble() ?: 1.0)
            return ItemIngredient(name, amountMin, amountMax, probability)
        }
    }
}