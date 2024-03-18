package minerofmillions.recipe_factory.examples

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.JsonPatchException
import kotlinx.coroutines.*
import minerofmillions.recipe_factory.examples.starbound.*
import minerofmillions.utils.*
import mu.KotlinLogging
import java.io.File
import java.util.*
import kotlin.io.resolve
import kotlin.time.measureTime

class StarboundDatabaseGenerator private constructor(
    coroutineScope: CoroutineScope,
    private val starboundDirectory: File,
    private val unpackDirectory: File,
) : CoroutineScope by coroutineScope {
    private val modPriority: Deferred<Set<Metadata>> = async {
        unpackDirectory.listFiles()!!.filter(File::isDirectory)
            .mapNotNull { mod ->
                mod.resolve("_metadata").takeIf(File::exists)?.let {
                    mapper.readValue(it, Metadata::class.java)
                }?.apply {
                    directory = mod
                }
            }
            .sortedBy(Metadata::priority)
            .let { allMetadata ->
                val modPriority = mutableSetOf<Metadata>()
                fun addMod(metadata: Metadata) {
                    metadata.getIncludes().mapNotNull { mod -> allMetadata.firstOrNull { it.getName() == mod } }
                        .sortedBy(Metadata::priority).forEach(::addMod)
                    modPriority.add(metadata)
                }

                allMetadata.forEach(::addMod)
                modPriority
            }
            .also { logger.debug("Finished loading mod priority.") }
    }

    private val filesByPriority = async {
        modPriority.await().flatMap { it.directory.walk() }.filter(File::isFile)
            .also { logger.debug { "Finished loading files by priority" } }
    }

    private val patches = async {
        filesByPriority.await().filter { it.extension == "patch" }
            .groupBy { it.resolveSibling(it.nameWithoutExtension).getPatchName() }
            .mapValues { (_, patchFiles) ->
                async {
                    patchFiles.flatMap { patchFile ->
                        mapper.readValue<PatchList>(patchFile, PatchList::class.java)
                    }
                }
            }.also { logger.debug("Finished loading patches.") }
    }

    private val fileWithPatchesCache = mutableMapOf<File, JsonNode>()
    private val invalidFilesForPatches = mutableSetOf<File>()
    private suspend fun getFileWithPatches(file: File): JsonNode? {
        if (file in invalidFilesForPatches) return null
        fileWithPatchesCache[file]?.let { return it }
        val tree = try {
            mapper.readTree(file)
        } catch (_: JsonParseException) {
            return null
        }
        return (patches.await()[file.getPatchName()]?.await() ?: emptyList()).fold(tree) { acc, patchOps ->
            try {
                JsonPatch(patchOps).apply(acc)
            } catch (e: JsonPatchException) {
                acc
            }
        }.also {
            if (it == null) invalidFilesForPatches.add(file)
            else fileWithPatchesCache[file] = it
        }
    }

    private val fileNameWithPatchesCache = mutableMapOf<String, JsonNode>()
    private val invalidFileNamesForPatches = mutableSetOf<String>()
    private suspend fun getFileWithPatches(fileNameForPatches: String): JsonNode? {
        if (fileNameForPatches in invalidFileNamesForPatches) return null
        fileNameWithPatchesCache[fileNameForPatches]?.let { return it }
        return getFileWithPatches(filesByPriority.await().first { it.getPatchName() == fileNameForPatches }).also {
            if (it == null) invalidFileNamesForPatches.add(fileNameForPatches)
            else fileNameWithPatchesCache[fileNameForPatches] = it
        }
    }

    private val tenants = async {
        getPatchedObjectsByPriority { it.extension == "tenant" }.map {
            mapper.convertValue<Tenant>(it, Tenant::class.java)
        }.also { logger.debug("Finished loading tenants.") }
    }

    private val treasurePools = async {
        getPatchedObjectsByPriority { it.extension == "treasurepools" }.flatMap {
            mapper.convertValue<Map<String, List<TreasurePool>>>(
                it, mapper.typeFactory.constructMapType(
                    TreeMap::class.java,
                    mapper.typeFactory.constructType(String::class.java),
                    mapper.typeFactory.constructCollectionType(List::class.java, TreasurePool::class.java)
                )
            ).entries
        }.groupBy { it.key }
            .mapValues { it.value.flatMapTo(sortedSetOf(compareByDescending(TreasurePool::requiredDifficulty))) { (_, value) -> value } }
            .also { logger.debug("Finished loading treasure pools.") }
    }

    private val treasureChests = async {
        getPatchedObjectsByPriority { it.extension == "treasurechests" }.flatMap {
            mapper.convertValue<Map<String, List<ChestPool>>>(
                it, mapper.typeFactory.constructMapType(
                    TreeMap::class.java,
                    mapper.typeFactory.constructType(String::class.java),
                    mapper.typeFactory.constructCollectionType(ArrayList::class.java, ChestPool::class.java)
                )
            ).entries
        }.groupBy { it.key }.mapValues { it.value.flatMap { (_, value) -> value } }
            .also { logger.debug("Finished loading treasure chests.") }
    }

    private val celestialConfig = async {
        mapper.convertValue(getFileWithPatches("celestial.config"), CelestialConfig::class.java)!!
            .also { logger.debug("Finished loading celestial config.") }
    }

    private val terrestrialConfigObject = async {
        getFileWithPatches("terrestrial_worlds.config")!!.also { logger.debug("Finished loading terrestrial config.") }
    }
    private val planets = async { generatePlanetTypes(terrestrialConfigObject.await()) }
    private val regions = async { generateRegionTypes(terrestrialConfigObject.await()) }

    private val oreDistribution = async {
        mapper.convertValue<Map<String, List<OreDistribution>>>(
            getFileWithPatches(File("biomes", "oredistributions.configfunctions").path),
            mapper.typeFactory.constructMapType(
                Map::class.java,
                mapper.typeFactory.constructType(String::class.java),
                mapper.typeFactory.constructCollectionType(List::class.java, OreDistribution::class.java)
            )
        )!!.also { logger.debug("Finished loading ore distribution.") }
    }
    private val biomes = async {
        getPatchedObjectsByPriority { it.extension == "biome" }.map {
            mapper.treeToValue(it, Biome::class.java)
        }.associateBy { it.name }.also { logger.debug("Finished loading biomes.") }
    }
    private val materialMods = async {
        getPatchedObjectsByPriority { it.extension == "matmod" }.filter { it.has("itemDrop") }
            .associate { it["modName"].asText() to it["itemDrop"].asText() }
            .also { logger.debug("Finished loading material modifiers.") }
    }

    private val objects = async {
        getPatchedObjectsByPriority { it.extension == "object" }.also { logger.debug("Finished loading objects.") }
    }

    private val items = async {
        getPatchedObjectsByPriority().filter { it.has("itemName") }.also { logger.debug("Finished loading items.") }
    }

    private val validItems = async {
        (objects.await().map { it["objectName"].asText() } union items.await()
            .mapTo(mutableSetOf()) { it["itemName"].asText() }).toSortedSet()
            .also { logger.debug("Finished loading valid items.") }
    }

    private val recipes = async {
        filesByPriority.await().filter { it.extension == "recipe" }
            .map { it.getPatchName() to mapper.convertValue(getFileWithPatches(it), Recipe::class.java) }
            .mapNotNull(Pair<String?, Recipe?>::extractNull).also { logger.debug("Finished loading recipes.") }
    }

    private val plants = async {
        getPatchedObjectsByPriority { it.extension == "object" }.mapPairNotNull { obj ->
            val seed = obj["objectName"].asText()
            seed to obj["stages"]?.let {
                it.last()["harvestPool"]?.asText()
            }
        }.map { (name, pool) -> name to getPoolLoot(pool) }.filter { it.second.isNotEmpty() }
            .also { logger.debug("Finished loading plants.") }
    }

    private val npcs = async {
        filesByPriority.await().filter { it.extension == "npctype" }
            .map { it.getPatchName() to getFileWithPatches(it) }
            .mapNotNull(Pair<String, JsonNode?>::extractNull)
            .mapPairNotNull { (patchName, obj) ->
                patchName to obj["dropPools"]
            }
            .mapPairNotNull { (patchName, pools) ->
                patchName to pools.mapNotNull(JsonNode::asText).takeIf(Collection<String>::isNotEmpty)
            }
            .also { logger.debug("Finished loading npcs.") }
    }

    private fun generateRecipes(setStatus: (@Composable () -> Unit) -> Unit) = runBlocking {
        setStatus @Composable {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        coroutineScope {
            launch {
                logger.info("Writing star info")
                mapper.factory.createGenerator(unpackDirectory.resolve("StarDatabase.json"), JsonEncoding.UTF8)
                    .setPrettyPrinter(DefaultPrettyPrinter().apply {
                        indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
                    })
                    .runUsing {
                        writeStartObject()
                        val celestialConfig = celestialConfig.await()
                        celestialConfig.systemTypes.forEach { (starName, systemType) ->
                            writeObjectFieldStart(starName)
                            writeNumberField("weight", systemType.weight)
                            writeArrayFieldStart("orbits")
                            (1 until systemType.orbitRegions.minOf { it.orbitRange.first }).forEach { _ ->
                                writeStartObject()
                                writeEndObject()
                            }
                            systemType.orbitRegions.forEach { region ->
                                region.orbitRange.forEach { _ ->
                                    writeStartObject()
                                    region.planetaryTypes.forEachWithProbability { planetType, probability ->
                                        writeNumberField(planetType.terrestrialType, probability)
                                    }
                                    writeEndObject()
                                }
                            }
                            writeEndArray()
                            writeEndObject()
                        }
                        writeEndObject()
                    }
            }

            launch {
                logger.info("Writing planet info")
                mapper.factory.createGenerator(unpackDirectory.resolve("PlanetDatabase.json"), JsonEncoding.UTF8)
                    .useDefaultPrettyPrinter()
//            .setPrettyPrinter(DefaultPrettyPrinter().apply {
//                indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
//            })
                    .runUsing {
                        writeStartObject()
                        val celestialConfig = celestialConfig.await()
                        celestialConfig.planetaryTypes.forEach { (typeName, type) ->
                            writeArrayFieldStart(typeName)
                            type.variationParameters?.forEach { variation ->
                                (variation + type.baseParameters).let { parameters ->
                                    parameters.terrestrialType?.forEach { planetType ->
                                        planets.await()[planetType]?.get(parameters.worldSize)?.let { planet ->
                                            writeStartObject()
                                            writeStringField("planetType", planetType)
                                            writeObjectField("gravityRange", planet.gravityRange)
                                            writeObjectField("threatRange", planet.threatRange)
                                            writeObjectField("size", planet.size)
                                            writeLayers(planet.layers, planet.threatRange.first.toDouble())
                                            writeEndObject()
                                        } ?: writeNull()
//                                writeObject(planets[planetType]?.get(parameters.worldSize!!))
                                    }
//                            writeObject(parameters)
                                }
                            }
                            writeEndArray()
                        }
                        writeEndObject()
                    }
            }

            launch {
                logger.info("Writing biome info")
                mapper.factory.createGenerator(unpackDirectory.resolve("BiomeDatabase.json"), JsonEncoding.UTF8)
                    .useDefaultPrettyPrinter()
                    .runUsing {
                        writeStartObject()
                        val biomes = biomes.await()
                        biomes.forEach { (name, biome) ->
                            writeObjectFieldStart(name)
                            writeStringField("name", biome.name)
                            writeStringField("friendlyName", biome.friendlyName)
                            writeStringField("mainBlock", biome.mainBlock)
                            if (biome.subBlocks?.isNotEmpty() == true) writeObjectField("subBlocks", biome.subBlocks)
                            if (biome.ores?.isNotEmpty() == true) {
                                oreDistribution.await()[biome.ores]?.let { ores ->
                                    writeObjectFieldStart("ores")
                                    ores.forEach { distribution ->
                                        writeObjectFieldStart(distribution.minimumLevel.toString())
                                        val totalOreWeight = distribution.ores.totalWeight()
                                        distribution.ores.filter { it.weight > 0 }.sortedByDescending(OreEntry::weight)
                                            .forEach { ore ->
                                                writeNumberField(
                                                    materialMods.await()[ore.oreName]!!,
                                                    ore.probability(totalOreWeight)
                                                )
                                            }
                                        writeEndObject()
                                    }
                                    writeEndObject()
                                } ?: writeNullField("ores")
                            }
                            if (biome.surfaceItems.isNotEmpty()) {
                                writeArrayFieldStart("surfaceItems")
                                biome.surfaceItems.filterNot { it is PlaceableGrass || it is PlaceableBush }
                                    .forEach(::writeObject)
                                writeEndArray()
                            }
                            writeEndObject()
                        }
                        writeEndObject()
                    }
            }

            launch {
                logger.info("Writing item names")
                mapper.factory.createGenerator(unpackDirectory.resolve("ItemDatabase.json"), JsonEncoding.UTF8)
                    .useDefaultPrettyPrinter()
                    .runUsing {
                        writeStartObject()

                        val itemNames =
                            items.await()
                                .mapNotNull { (it["itemName"].asText() to it["shortdescription"]?.asText()).extractNull() }
                                .toMap()

                        val objectNames =
                            objects.await()
                                .mapNotNull { (it["objectName"].asText() to it["shortdescription"]?.asText()).extractNull() }
                                .toMap()

                        val names = (objectNames + itemNames).toSortedMap()

                        names.forEach { (item, name) ->
                            writeStringField(item, name)
                        }

                        writeEndObject()
                    }
            }

            launch {
                logger.info("Writing crafting stations")
                mapper.factory.createGenerator(unpackDirectory.resolve("StationDatabase.json"), JsonEncoding.UTF8)
                    .useDefaultPrettyPrinter()
                    .runUsing {
                        writeStartObject()

                        val stations = (objects.await().filter { it.has("upgradeStages") }
                            .flatMap { station ->
                                val startingStage = station["startingUpgradeStage"].asInt()
                                val maxStage = station["maxUpgradeStage"].asInt()

                                (startingStage..maxStage)
                                    .mapNotNull { station["upgradeStages"][it - 1] as? ObjectNode }
                                    .onEach {
                                        it.replace("objectName", station["objectName"])
                                        it.replace("description", it["itemSpawnParameters"]["description"])
                                        it.replace("shortdescription", it["itemSpawnParameters"]["shortdescription"])
                                    }
                                    .mapNotNull(::readCraftingStation)
                            } + objects.await().filter { it.has("interactData") }
                            .mapNotNull(::readCraftingStation)).toMap()
                            .toSortedMap()
                        val stationSet = stations.values.flatten().toSortedSet()

                        stationSet.forEach { category ->
                            writeFieldName(category)
                            writeCollection(stations.filterValues { category in it }.keys)
                        }

                        writeEndObject()
                    }
            }

            launch {
                mapper.factory.createGenerator(unpackDirectory.resolve("RecipeDatabase.json"), JsonEncoding.UTF8)
                    .setPrettyPrinter(DefaultPrettyPrinter().apply {
                        indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
                    }).runUsing {
                        writeStartObject()

                        // Serialize recipes
                        logger.info("Writing recipes")
                        recipes.await().forEach(::writeObjectField)

                        logger.info("Writing plants")
                        plants.await().forEach { (plant, loot) ->
                            writeObjectField(
                                "growing\\$plant", Recipe(emptyList(), loot, listOf("growing"))
                            )
                        }

                        logger.info("Writing npcs")
                        val treasurePools = treasurePools.await()
                        npcs.await().forEach { (name, pools) ->
                            val treasures = pools.mapNotNull { treasurePools[it] }
                            treasures.flatMapTo(sortedSetOf()) { it.getDifficulties() }.forEach { difficulty ->
                                writeObjectField(
                                    "kills\\$name\\$difficulty", Recipe(
                                        emptyList(),
                                        treasures.flatMap { it.evaluateAtDifficulty(difficulty, treasurePools) }
                                            .mergeItems(),
                                        listOf("kills", name)
                                    )
                                )
                            }
                        }

                        logger.info("Writing openable items.")
                        val items = items.await()
                        (items.filter { it.has("pool") } + items.mapNotNull {
                            ((it["treasure"] ?: it["content"]) as? ObjectNode)?.apply {
                                replace("itemName", it["itemName"])
                            }
                        }).forEach { openable ->
                            val itemName = openable["itemName"].asText()
                            val pool = openable["pool"].asText()
                            val level = openable["level"]?.asDouble() ?: 1.0

                            treasurePools[pool]?.evaluateAtDifficulty(level, treasurePools)?.let {
                                writeObjectField("open\\$itemName", Recipe(listOf(Item(itemName)), it, listOf("open")))
                            }
                        }

                        logger.info("Writing tenant gifts.")
                        tenants.await().forEach { tenant ->
                            val rentPool = tenant.rent.pool
                            getAllRequiredDifficulties(rentPool).forEach { difficulty ->
                                val totalLoot = treasurePools[rentPool]?.evaluateAtDifficulty(difficulty, treasurePools)
                                    ?: emptyList()
                                if (totalLoot.isNotEmpty()) writeObjectField(
                                    """tenants\\${tenant.name}\\$difficulty""",
                                    Recipe(emptyList(), totalLoot, listOf("tenantRent"))
                                )
                            }
                        }

                        //Serialize misc objects
                        logger.info("Writing miscellaneous objects")
                        filesByPriority.await().forEach { file ->
                            when (file.name) {
                                "isn_atmoscondenser.object", "isn_atmoscondensermadness.object" -> writeAtmosphericCondenserRecipes(
                                    getFileWithPatches(file)!!
                                )

                                "fu_liquidmixer_recipes.config" -> writeLiquidMixerRecipes(getFileWithPatches(file)!!)

                                "centrifuge_recipes.config" -> writeCentrifugeRecipes(getFileWithPatches(file)!!)

                                "electricfurnace.object", "fu_blastfurnace.object", "isn_arcsmelter.object" -> writeFurnaceRecipes(
                                    getFileWithPatches(file)!!, validItems.await()
                                )

                                "extractionlab_recipes.config" -> writeExtractorRecipes(
                                    getFileWithPatches(file)!!, "extractionlab", validItems.await()
                                )

                                "extractionlabmadness_recipes.config" -> writeExtractorRecipes(
                                    getFileWithPatches(file)!!, "extractionlabmadness", validItems.await()
                                )

                                "xenostation_recipes.config" -> writeExtractorRecipes(
                                    getFileWithPatches(file)!!, "xenostation", validItems.await()
                                )

                                "makeshiftreactor.object", "makeshiftreactor2.object", "skathfusionreactor.object" -> writeReactorRecipes(
                                    getFileWithPatches(file)!!
                                )

                                "precursoromegagenerator.object", "braingenerator.object", "fu_alternatorgenerator.object", "fu_quantumgenerator.object", "isn_thermalgenerator.object" -> writeGeneratorRecipes(
                                    getFileWithPatches(file)!!
                                )

                                "isn_fissionreactornew.object" -> writeFissionReactorRecipes(getFileWithPatches(file)!!)
                            }
                        }
                        writeEndObject()
                    }
            }
        }
        setStatus @Composable { }
    }

    private suspend fun JsonGenerator.writeLayers(layers: PlanetLayers, currentLevel: Double) {
        writeObjectFieldStart("layers")
        writeLayer("space", layers.space, currentLevel)
        writeLayer("atmosphere", layers.atmosphere, currentLevel)
        writeLayer("surface", layers.surface, currentLevel)
        writeLayer("subsurface", layers.subsurface, currentLevel)
        writeLayer("underground1", layers.underground1, currentLevel)
        writeLayer("underground2", layers.underground2, currentLevel)
        writeLayer("underground3", layers.underground3, currentLevel)
        writeLayer("core", layers.core, currentLevel)
        writeEndObject()
    }

    private suspend fun JsonGenerator.writeLayer(layerName: String, layer: PlanetLayer, currentLevel: Double) {
        writeObjectFieldStart(layerName)
        if (layer.enabled) {
            writeObjectField("layerLevel", layer.layerLevel)
            writeObjectField("baseHeight", layer.baseHeight)
            writeRegions("primaryRegions", layer.primaryRegion, currentLevel)
            writeObjectField("subRegionSize", layer.subRegionSize)
            if (layer.secondaryRegions.isNotEmpty() && layer.secondaryRegionCount.last > 0) {
                writeRegions("secondaryRegions", layer.secondaryRegions, currentLevel)
                writeObjectField("secondaryRegionCount", layer.secondaryRegionCount)
                writeObjectField("secondaryRegionSize", layer.secondaryRegionSize)
            }
            if (layer.dungeons.isNotEmpty() && layer.dungeonCountRange.last > 0) {
                writeObjectField("dungeons", layer.dungeons)
                writeObjectField("dungeonCount", layer.dungeonCountRange)
            }
        }
        writeEndObject()
    }

    private suspend fun JsonGenerator.writeRegions(
        typeOfRegions: String,
        regionTypes: List<String>,
        currentLevel: Double,
    ) {
        writeObjectFieldStart(typeOfRegions)
        regionTypes.forEach { regionType ->
            // regions.mapNotNull(regions::get).map {
            //     it.biome.filter { it.minimumLevel <= currentLevel }.maxByOrNull { it.minimumLevel }
            //         .biomes
            //         .map(biomes::get)
            // }
            regions.await()[regionType]?.let { region ->
                writeObjectField(
                    regionType,
                    region.biome.filter { it.minimumLevel <= currentLevel }.maxByOrNull { it.minimumLevel }?.biomes
                )
            }
        }
        writeEndObject()
    }

    private fun File.getPatchName() = relativeTo(unpackDirectory).dropRoot()

    private suspend fun getPatchedObjectsByPriority(predicate: (File) -> Boolean = { true }) =
        filesByPriority.await().filter(predicate).mapNotNull {
            try {
                getFileWithPatches(it)
            } catch (_: Exception) {
                null
            }
        }

    private val requiredDifficultyCache = synchronizedMapOf<String, Set<Double>>()
    private suspend fun getAllRequiredDifficulties(pool: String) = requiredDifficultyCache.getOrPut(pool) {
        treasurePools.await()[pool]!!.getAllRequiredDifficulties()
    }

    private suspend fun Iterable<TreasurePool>.getAllRequiredDifficulties(): Set<Double> {
        val results = sortedSetOf<Double>()
        forEach {
            results.add(it.requiredDifficulty)
            (it.fill + it.pool).filterIsInstance<TreasurePoolPool>().forEach { pool ->
                results.addAll(getAllRequiredDifficulties(pool.pool))
            }
        }

        return results
    }

    private val totalLootCache = synchronizedMapOf<TreasurePool, List<Item>>()
    private suspend fun TreasurePool.getTotalLoot(): List<Item> = totalLootCache.getOrPut(this) {
        val treasurePools = treasurePools.await()
        val fillLoot = fill.flatMap { entry ->
            when (entry) {
                is TreasurePoolItem -> listOf(Item(entry.item, entry.count))
                is TreasurePoolPool -> treasurePools[entry.pool]!!.evaluateAtDifficulty(
                    requiredDifficulty,
                    treasurePools
                )
            }
        }

        val poolWeight = pool.totalWeight()
        val averageRounds = poolRounds.averageRounds()
        val poolLoot = pool.flatMap { entry ->
            when (entry) {
                is TreasurePoolItem -> listOf(Item(entry.item, entry.count * entry.weight / poolWeight))
                is TreasurePoolPool -> treasurePools[entry.pool]?.evaluateAtDifficulty(
                    requiredDifficulty,
                    treasurePools
                )
                    ?.map { it * (entry.weight / poolWeight) } ?: emptyList()
            }
        }.map { it * averageRounds }

        return (fillLoot + poolLoot).mergeItems()
    }

    private suspend fun getPoolLoot(poolName: String) =
        (treasurePools.await()[poolName] ?: emptyList()).flatMap { it.getTotalLoot() }.mergeItems()
            .filterNot { it.item == "" }

    companion object {
        private val logger = KotlinLogging.logger { }
        private val mapper = ObjectMapper(
            JsonFactoryBuilder().enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS).build()
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
        )
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(
                SimpleModule().addDeserializer(IntRange::class.java, IntRangeDeserializer)
                    .addSerializer(IntRangeSerializer)
            )


//        private val starboundDirectory = File("""I:\\SteamLibrary\\steamapps\\common\\Starbound""")
//        private val modDirectory = starboundDirectory.parentFile.parentFile.resolve("workshop", "content", "211820")
//        val unpackDirectory = File("""E:\\SBUnpacked""")

        //        private val unpacker = starboundDirectory.resolve("win32", "asset_unpacker.exe")
        private val removedExtensions = listOf(/*"ogg", */"png", "disabled")

        suspend fun generateRecipes(
            forceGenerate: Boolean = false,
            starboundDirectory: File,
            unpackDirectory: File,
            setStatus: (@Composable () -> Unit) -> Unit,
        ) = coroutineScope {
            if (unpack(starboundDirectory, unpackDirectory) || forceGenerate) withContext(Dispatchers.IO) {
                StarboundDatabaseGenerator(this, starboundDirectory, unpackDirectory).generateRecipes(
                    setStatus
                )
            }
        }

        private suspend fun unpack(starboundDirectory: File, unpackDirectory: File): Boolean {
            if (!unpackDirectory.exists() && !unpackDirectory.mkdirs()) error("Cannot make output directory.")
            var anyUnpacked: Boolean

            val modDirectory = starboundDirectory.parentFile.parentFile.resolve("workshop", "content", "211820")
            val unpacker = starboundDirectory.resolve("win32", "asset_unpacker.exe")

            logger.info("Unpacking...")
            val unpackDuration = measureTime {
                withContext(dispatcher) {
                    coroutineScope {
                        val unpackDeferred = buildList {
                            add(
                                unpackIfNecessary(
                                    unpacker,
                                    starboundDirectory.resolve("assets").resolve("packed.pak"),
                                    unpackDirectory.resolve("base"),
                                )
                            )
                            modDirectory.walk().filter { it.isFile && it.extension == "pak" }.forEach { pak ->
                                add(
                                    unpackIfNecessary(
                                        unpacker, pak, unpackDirectory.resolve(pak.parentFile.name),
                                    )
                                )
                            }
                        }
                        anyUnpacked = unpackDeferred.awaitAll().any(::identity)
                        unpackDirectory.listFiles()?.filter(File::isDirectory)?.forEach {
                            if (!modDirectory.resolve(it.name).exists() && it.name != "base") it.deleteRecursively()
                        }
                    }
                }
            }

            unpackDirectory.resolve("729480149").resolve("recipes").resolve("craftingfurnace")
                .resolve("solariumstar.recipe.patch").let {
                    it.takeIf(File::exists)?.renameTo(it.resolveSibling("solariumstar.recipe"))
                }

            unpackDirectory.resolve(File("2359135864\\recipes\\arcana\\arcana_crafting_techStation_3\\augments\\arcana_augment_ruinous.recipe"))
                .takeIf(File::exists)?.let { it.writeText(it.readText().replace(".e", ".")) }

            logger.info("Done! Finished in $unpackDuration")
            return anyUnpacked
        }

        private fun CoroutineScope.unpackIfNecessary(
            unpacker: File,
            pakFile: File,
            outputDirectory: File,
        ): Deferred<Boolean> = async {
            if (!outputDirectory.exists() && !outputDirectory.mkdirs()) error("Cannot make output directory: $outputDirectory")
            val pakModified = pakFile.lastModified()
            val outputModified =
                outputDirectory.resolve("lastModified.date").takeIf(File::exists)?.readText()?.toLong(16) ?: 0
            if (pakModified > outputModified) {
                logger.info { outputDirectory }
                outputDirectory.deleteRecursively()

                ProcessBuilder(
                    unpacker.absolutePath, pakFile.absolutePath, outputDirectory.absolutePath
                ).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.INHERIT).start()
                    .waitFor()

                outputDirectory.walkBottomUp().onLeave { if (it.list()?.size == 0) it.delete() }.forEach {
                    if (it.isFile && it.extension in removedExtensions) it.delete()
                }

                outputDirectory.resolve("lastModified.date").writeText(pakModified.toString(16))
                true
            } else false
        }

    }
}

private fun File.dropRoot(): String = toString().split(File.separator).drop(1).joinToString(File.separator)

private fun readCraftingStation(station: JsonNode): Pair<String, Collection<String>>? {
    val objectName =
        if (station.has("animationState")) station["objectName"].asText() + "/" + station["animationState"].asText()
        else station["objectName"].asText()
    val filters = station["interactData"]["filter"]?.map(JsonNode::asText) ?: return null

    return objectName to filters
}

private fun JsonGenerator.writeCollection(
    collection: Collection<String>, offset: Int = 0, length: Int = collection.size,
) {
    writeArray(collection.toTypedArray(), offset, length)
}
