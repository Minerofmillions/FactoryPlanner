package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

object IntRangeDeserializer : StdDeserializer<IntRange>(IntRange::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IntRange = IntRange(p.codec.readTree(p))
}

object IntRangeSerializer : StdSerializer<IntRange>(IntRange::class.java) {
    override fun serialize(value: IntRange, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

object DoubleRangeSerializer : StdSerializer<ClosedFloatingPointRange<*>>(ClosedFloatingPointRange::class.java) {
    override fun serialize(
        value: ClosedFloatingPointRange<*>,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeString(value.toString())
    }
}

fun IntRange(node: JsonNode): IntRange =
    if (node.isArray) IntRange(node[0].asInt(), node[1].asInt())
    else error("Cannot convert $node to IntRange.")

fun DoubleRange(node: JsonNode): ClosedFloatingPointRange<Double> =
    if (node.isArray) node[0].asDouble() .. node[1].asDouble()
    else error("Cannot convert $node to DoubleRange.")