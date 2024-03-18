package minerofmillions.blueprint_planner.factory_components.recipe

import com.fasterxml.jackson.databind.JsonNode
import minerofmillions.blueprint_planner.factory_components.item.IngredientItem
import minerofmillions.blueprint_planner.factory_components.item.Item
import minerofmillions.blueprint_planner.factory_components.item.ItemType

class Recipe(
    val name: String,
    val inputs: List<IngredientItem>,
    val outputs: List<IngredientItem>,
    val energy: Double,
    val category: String
) {
    val loops: List<Item>
    init {
        val inputNames = inputs.map(IngredientItem::name)
        loops = outputs.filter { it.name in inputNames }.map { Item(it.name, it.itemType) }
    }

    fun getFluidboxes(): List<FluidboxInfo> {
        val inputFluidboxes = inputs.filter { it.itemType == ItemType.FLUID }.map {
            FluidboxInfo("input", it.fluidboxIndex!!, it.fluidboxIndex)
        }
        val maxInputIndex = inputFluidboxes.maxOfOrNull(FluidboxInfo::index) ?: 0
        val outputFluidboxes = outputs.filter { it.itemType == ItemType.FLUID }.map {
            FluidboxInfo("output", it.fluidboxIndex!!, it.fluidboxIndex + maxInputIndex)
        }
        return inputFluidboxes + outputFluidboxes
    }

    fun getInput(name: String) = inputs.firstOrNull { it.name == name }
    fun getOutput(name: String) = outputs.firstOrNull { it.name == name }

    companion object {
        fun fromJson(recipe: JsonNode): Recipe {
            var fluidboxIndex = 1
            val products = recipe["products"].asSequence()
                .map { IngredientItem.fromJson(it, fluidboxIndex) }
                .onEach { if (it.itemType == ItemType.FLUID) fluidboxIndex++ }
                .toList()
            fluidboxIndex = 1
            val ingredients = recipe["ingredients"].asSequence()
                .map { IngredientItem.fromJson(it, fluidboxIndex) }
                .onEach { if (it.itemType == ItemType.FLUID) fluidboxIndex++ }
                .toList()

            return Recipe(recipe["name"].asText(), ingredients, products, recipe["energy"].asDouble(), recipe["category"].asText())
        }
    }

    data class FluidboxInfo(
        val type: String,
        val index: Int,
        val globalIndex: Int
    )
}