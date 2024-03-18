package minerofmillions.blueprint_planner.factory_components.item

import com.fasterxml.jackson.databind.JsonNode

data class IngredientItem(
    val name: String,
    val itemType: ItemType,
    val fluidboxIndex: Int?,
    val probability: Double = 1.0,
    val catalystAmount: Int = 0,
    val amount: Int = 0,
    val amountMax: Int = 0,
    val amountMin: Int = 0
) {
    fun getAverageAmount() = if (amount != 0) amount * probability else (amountMax + amountMin) * probability / 2.0

    fun getProductiveAmount(): Double {
        var amount = amount.toDouble()
        if (amount == 0.0) amount = (amountMax + amountMin) / 2.0
        return if (amount > catalystAmount) (amount - catalystAmount) * probability else amount * probability
    }

    companion object {
        fun fromJson(item: JsonNode, fluidboxIndexDefault: Int?): IngredientItem {
            val name = item["name"].asText()
            val itemType = ItemType[item["type"].asText()]
            val fluidboxIndex =
                (if (itemType == ItemType.FLUID) item["fluidbox_index"]?.asInt() else null)
                    ?: fluidboxIndexDefault
            val probability = item["probability"]?.asDouble() ?: 1.0
            val amount = item["amount"]?.asInt() ?: 0
            val amountMax = item["amount_max"]?.asInt() ?: 0
            val amountMin = item["amount_min"]?.asInt() ?: 0
            val catalystAmount = item["catalyst_amount"]?.asInt() ?: 0
            return IngredientItem(
                name,
                itemType,
                fluidboxIndex,
                probability,
                catalystAmount,
                amount,
                amountMax,
                amountMin
            )
        }
    }
}