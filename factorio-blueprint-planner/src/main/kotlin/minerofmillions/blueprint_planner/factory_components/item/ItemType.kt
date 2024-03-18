package minerofmillions.blueprint_planner.factory_components.item

enum class ItemType {
    ITEM,
    FLUID,
    FUEL;

    companion object {
        operator fun get(type: String) = entries.first { it.name == type.uppercase() }
    }
}