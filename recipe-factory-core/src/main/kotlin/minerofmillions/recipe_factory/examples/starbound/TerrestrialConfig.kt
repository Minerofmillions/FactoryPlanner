package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode

fun generatePlanetTypes(terrestrialConfig: JsonNode): Map<String, Map<String, Planet>> {
    val planetLayers =
        listOf("space", "atmosphere", "surface", "subsurface", "underground1", "underground2", "underground3", "core")

    val planetDefaults = terrestrialConfig["planetDefaults"]
    val defaultLayerDefaults = planetDefaults["layerDefaults"]

    return terrestrialConfig["planetTypes"].fields().asSequence()
        .associateTo(sortedMapOf()) { (planetType, planetInfo) ->
            val threatRange = IntRange(planetInfo["threatRange"] ?: planetDefaults["threatRange"])
            planetType to terrestrialConfig["planetSizes"].fields().asSequence()
                .associateTo(sortedMapOf()) { (planetSize, sizeInfo) ->
                    val gravityRange = IntRange(sizeInfo["gravityRange"] ?: planetDefaults["gravityRange"])
                    val size = sizeInfo["size"].let { WorldSize(it[0].asInt(), it[1].asInt()) }

                    val sizeLayerDefaults = sizeInfo["layerDefaults"]

                    val layerInfo = PlanetLayers(planetLayers.associateWith { layerName ->
                        val planetLayerInfo = planetInfo["layers"][layerName]
                        val sizeLayerInfo = sizeInfo["layers"][layerName]
                        val defaultLayerInfo = planetDefaults["layers"][layerName]

                        fun getLayerInfo(name: String) =
                            planetLayerInfo?.get(name) ?: defaultLayerInfo?.get(name) ?: sizeLayerInfo?.get(name)
                            ?: sizeLayerDefaults?.get(name) ?: defaultLayerDefaults[name]

                        PlanetLayer(
                            getLayerInfo("enabled").asBoolean(),
                            getLayerInfo("layerLevel").asInt(),
                            getLayerInfo("baseHeight").asInt(),
                            getLayerInfo("primaryRegion").map(JsonNode::asText),
                            getLayerInfo("secondaryRegions").map(JsonNode::asText),
                            IntRange(getLayerInfo("secondaryRegionCount")),
                            DoubleRange(getLayerInfo("secondaryRegionSize")),
                            DoubleRange(getLayerInfo("subRegionSize")),
                            getLayerInfo("dungeons").map(::DungeonEntry),
                            IntRange(getLayerInfo("dungeonCountRange"))
                        )
                    })

                    planetSize to Planet(gravityRange, threatRange, layerInfo, size)
                }
        }
}

fun generateRegionTypes(terrestrialConfig: JsonNode): Map<String, Region> {
    val regionDefaults = terrestrialConfig["regionDefaults"]

    return terrestrialConfig["regionTypes"].fields().asSequence().associate { (regionName, regionData) ->
        fun getRegionInfo(name: String) = regionData[name] ?: regionDefaults[name]

        regionName to Region(
            getRegionInfo("blockSelector").map(JsonNode::asText),
            getRegionInfo("fgCaveSelector").map(JsonNode::asText),
            getRegionInfo("bgCaveSelector").map(JsonNode::asText),
            getRegionInfo("fgOreSelector").map(JsonNode::asText),
            getRegionInfo("bgOreSelector").map(JsonNode::asText),
            getRegionInfo("subBlockSelector").map(JsonNode::asText),
            getRegionInfo("oceanLiquid").map(JsonNode::asText),
            getRegionInfo("oceanLevelOffset").asInt(),
            getRegionInfo("encloseLiquids").asBoolean(),
            getRegionInfo("fillMicrodungeons").asBoolean(),
            getRegionInfo("caveLiquid").map(JsonNode::asText),
            DoubleRange(getRegionInfo("caveLiquidSeedDensityRange")),
            getRegionInfo("biome").map(::BiomeEntry),
            getRegionInfo("subRegion").map(JsonNode::asText),
        )
    }
}

data class Planet(
    val gravityRange: IntRange,
    val threatRange: IntRange,
    val layers: PlanetLayers,
    val size: WorldSize,
)

data class WorldSize(
    val width: Int,
    val height: Int,
) {
    @JsonValue
    override fun toString() = "$width x $height"
}

data class PlanetLayers(
    val space: PlanetLayer,
    val atmosphere: PlanetLayer,
    val surface: PlanetLayer,
    val subsurface: PlanetLayer,
    val underground1: PlanetLayer,
    val underground2: PlanetLayer,
    val underground3: PlanetLayer,
    val core: PlanetLayer,
) {
    constructor(layerInfo: Map<String, PlanetLayer>) : this(
        layerInfo["space"]!!,
        layerInfo["atmosphere"]!!,
        layerInfo["surface"]!!,
        layerInfo["subsurface"]!!,
        layerInfo["underground1"]!!,
        layerInfo["underground2"]!!,
        layerInfo["underground3"]!!,
        layerInfo["core"]!!
    )
}

data class PlanetLayer(
    val enabled: Boolean,
    val layerLevel: Int,
    val baseHeight: Int,
    val primaryRegion: List<String>,
    val secondaryRegions: List<String>,
    val secondaryRegionCount: IntRange,
    val secondaryRegionSize: ClosedFloatingPointRange<Double>,
    val subRegionSize: ClosedFloatingPointRange<Double>,
    val dungeons: List<DungeonEntry>,
    val dungeonCountRange: IntRange,
)

data class DungeonEntry(override val weight: Double, val dungeon: String) : WeightedEntry {
    constructor(node: JsonNode) : this(node[0].asDouble(), node[1].asText())
}

data class Region(
    val blockSelector: List<String>,
    val fgCaveSelector: List<String>,
    val bgCaveSelector: List<String>,
    val fgOreSelector: List<String>,
    val bgOreSelector: List<String>,
    val subBlockSelector: List<String>,
    val oceanLiquid: List<String>,
    val oceanLevelOffset: Int,
    val encloseLiquids: Boolean,
    val fillMicrodungeons: Boolean,
    val caveLiquid: List<String>,
    val caveLiquidSeedDensityRange: ClosedFloatingPointRange<Double>,
    val biome: List<BiomeEntry>,
    val subRegion: List<String>,
)

data class BiomeEntry(val minimumLevel: Double, val biomes: List<String>) {
    constructor(node: JsonNode) : this(node[0].asDouble(), node[1].map(JsonNode::asText))
}