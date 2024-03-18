package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.fge.jsonpatch.JsonPatchOperation

@JsonDeserialize(using = PatchList.Deserializer::class)
class PatchList(patches: List<List<JsonPatchOperation>>) : List<List<JsonPatchOperation>> by patches {
    object Deserializer : StdDeserializer<PatchList>(PatchList::class.java) {
        private fun getOps(json: JsonNode, codec: ObjectCodec): List<List<JsonPatchOperation>> =
            if (json.has("op")) {
                listOf(
                    listOf(
                        codec.treeToValue(
                            (json as ObjectNode).apply {
                                put("path", get("path").asText().dropWhile { it != JsonPointer.SEPARATOR })
                                putIfAbsent("value", NullNode.instance)
                            }, JsonPatchOperation::class.java
                        )
                    )
                )
            } else json.flatMap { getOps(it, codec) }

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PatchList =
            PatchList(getOps(p.readValueAsTree(), p.codec))
    }
}