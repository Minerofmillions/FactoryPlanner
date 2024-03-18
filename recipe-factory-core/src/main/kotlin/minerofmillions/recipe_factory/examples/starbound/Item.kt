package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import minerofmillions.recipe_factory.core.ItemStack
import org.ojalgo.scalar.RationalNumber

@JsonDeserialize(using = Item.Deserializer::class)
data class Item(val item: String, val count: Double = 1.0) {
    constructor(item: String, count: Int) : this(item, count.toDouble())

    override fun toString(): String =
        if (count > 1) "%s * %.3f".format(item, count) else "%s * %.3f%%".format(item, count * 100)

    @get:JsonIgnore
    val asItemStack get() = ItemStack(item, RationalNumber.valueOf(count))

    operator fun times(d: Int) = Item(item, count * d)
    operator fun times(d: Double) = Item(item, count * d)

    object Deserializer : StdDeserializer<Item>(Item::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Item =
            p.codec.readTree<JsonNode>(p).let {
                Item(it["item"]?.asText() ?: "unnamed_item", it["count"]?.asDouble() ?: 1.0)
            }
    }
}

fun Collection<Item>.mergeItems() = groupBy(Item::item)
    .map { (item, items) -> Item(item, items.sumOf(Item::count)) }
    .sortedByDescending(Item::count)
