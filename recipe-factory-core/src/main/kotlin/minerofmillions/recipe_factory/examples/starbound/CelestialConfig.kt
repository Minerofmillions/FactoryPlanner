package minerofmillions.recipe_factory.examples.starbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CelestialConfig @JsonCreator constructor(
    @JsonProperty("planetaryTypes") val planetaryTypes: Map<String, TerrestrialType>,
    @JsonProperty("satelliteTypes") val satelliteTypes: Map<String, TerrestrialType>,
    @JsonProperty("systemTypes") val systemTypes: Map<String, SystemType>,
)

data class TerrestrialType @JsonCreator constructor(
    @JsonProperty("satelliteProbability") val satelliteProbability: Double,
    @JsonProperty("maxSatelliteCount") val maxSatelliteCount: Int,
    @JsonProperty("baseParameters") val baseParameters: TerrestrialParameters,
    @JsonProperty("variationParameters") val variationParameters: List<VariationParameters>?,
)

data class TerrestrialParameters @JsonCreator constructor(
    @JsonProperty("worldType") val worldType: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("terrestrialType") val terrestrialType: List<String>?,
    @JsonProperty("worldSize") val worldSize: String?,
)

data class VariationParameters @JsonCreator constructor(
    @JsonProperty("worldSize") val worldSize: String?,
) {
    operator fun plus(baseParameters: TerrestrialParameters) = TerrestrialParameters(
        baseParameters.worldType,
        baseParameters.description,
        baseParameters.terrestrialType,
        worldSize ?: baseParameters.worldSize
    )
}

data class SystemType @JsonCreator constructor(
    @JsonProperty("selectionWeight") override val weight: Double,
    @JsonProperty("orbitRegions") val orbitRegions: List<OrbitRegion>,
) : WeightedEntry

data class OrbitRegion @JsonCreator constructor(
    @JsonProperty("regionName") val regionName: String,
    @JsonProperty("orbitRange") val orbitRange: IntRange,
    @JsonProperty("bodyProbability") val bodyProbability: Double,
    @JsonProperty("planetaryTypes") val planetaryTypes: List<OrbitEntry>,
    @JsonProperty("satelliteTypes") val satelliteTypes: List<OrbitEntry>,
)

data class OrbitEntry @JsonCreator constructor(
    @JsonProperty("item") val terrestrialType: String,
    @JsonProperty("weight") override val weight: Double,
) : WeightedEntry