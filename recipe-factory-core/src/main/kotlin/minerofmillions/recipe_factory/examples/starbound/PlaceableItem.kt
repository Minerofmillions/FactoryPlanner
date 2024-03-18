package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    Type(value = PlaceableObject::class, name = "object"),
    Type(value = PlaceableMicrodungeon::class, name = "microdungeon"),
    Type(value = PlaceableTree::class, name = "tree"),
    Type(value = PlaceableGrass::class, name = "grass"),
    Type(value = PlaceableBush::class, name = "bush"),
    Type(value = PlaceableTreasureBox::class, name = "treasureBox")
)
sealed class PlaceableItem(val mode: String, val priority: Double, val variants: Int, val distribution: String)


class PlaceableObject @JsonCreator constructor(
    @JsonProperty("mode") mode: String,
    @JsonProperty("priority") priority: Double,
    @JsonProperty("variants") variants: Int,
    @JsonProperty("distribution") distribution: String,
    @JsonProperty("objectSets") val objectSets: List<ObjectPool>,
) : PlaceableItem(mode, priority, variants, distribution)

class PlaceableMicrodungeon @JsonCreator constructor(
    @JsonProperty("mode") mode: String,
    @JsonProperty("priority") priority: Double,
    @JsonProperty("variants") variants: Int,
    @JsonProperty("distribution") distribution: String,
    @JsonProperty("microdungeons") val microdungeons: List<String>,
) : PlaceableItem(mode, priority, variants, distribution)

class PlaceableTree @JsonCreator constructor(
    @JsonProperty("mode") mode: String,
    @JsonProperty("priority") priority: Double,
    @JsonProperty("variants") variants: Int,
    @JsonProperty("distribution") distribution: String,
    @JsonProperty("treeStemList") val treeStemList: List<String>,
    @JsonProperty("treeFoliageList") val treeFoliageList: List<String>,
) : PlaceableItem(mode, priority, variants, distribution)

class PlaceableGrass @JsonCreator constructor(
    @JsonProperty("mode") mode: String,
    @JsonProperty("priority") priority: Double,
    @JsonProperty("variants") variants: Int,
    @JsonProperty("distribution") distribution: String,
    @JsonProperty("grasses") val grasses: List<String>,
) : PlaceableItem(mode, priority, variants, distribution)

@JsonDeserialize(using = PlaceableBush.Deserializer::class)
class PlaceableBush(
    mode: String,
    priority: Double,
    variants: Int,
    distribution: String,
    val bushes: List<String>
) : PlaceableItem(mode, priority, variants, distribution) {
    internal object Deserializer : StdDeserializer<PlaceableBush>(PlaceableBush::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PlaceableBush =
            p.codec.readTree<JsonNode>(p).let { bush ->
                PlaceableBush(
                    bush["mode"].asText(),
                    bush["priority"].asDouble(),
                    bush["variants"].asInt(),
                    bush["distribution"].asText(),
                    bush["bushes"].map { it["name"].asText() }
                )
            }
    }
}

class PlaceableTreasureBox @JsonCreator constructor(
    @JsonProperty("mode") mode: String,
    @JsonProperty("priority") priority: Double,
    @JsonProperty("variants") variants: Int,
    @JsonProperty("distribution") distribution: String,
    @JsonProperty("treasureBoxSets") val treasureBoxSets: List<String>,
) : PlaceableItem(mode, priority, variants, distribution)