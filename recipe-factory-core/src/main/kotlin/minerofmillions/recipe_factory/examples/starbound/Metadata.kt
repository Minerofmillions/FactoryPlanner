package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.File

@JsonDeserialize(using = Metadata.Deserializer::class)
data class Metadata(private val name: String?, private val steamContentId: String?, val priority: Int, private val includes: List<String>?) {
    fun getIncludes() = includes ?: emptyList()
    fun getName() = name ?: getSteamContentId()
    fun getSteamContentId() = steamContentId ?: ""

    @Transient
    lateinit var directory: File

    object Deserializer : StdDeserializer<Metadata>(Metadata::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Metadata {
            val tree = p.codec.readTree<JsonNode>(p)
            val name = tree["name"]?.asText()
            val steamContentId = tree["steamContentId"]?.asText()
            val priority = tree["priority"]?.asInt() ?: 0
            val includes = tree.findValuesAsText("includes")

            return Metadata(name, steamContentId, priority, includes)
        }
    }
}