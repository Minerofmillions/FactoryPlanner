package minerofmillions.blueprint_planner.factory_components.lane

import minerofmillions.blueprint_planner.factory_components.bundle.FullConfig
import minerofmillions.blueprint_planner.factory_components.item.Item
import minerofmillions.blueprint_planner.factory_components.item.ItemType

class Lane(
    val item: Item,
    val name: String,
    val direction: LaneDirection,
    val priority: LanePriority,
    val idx: Int,
    val fullConfig: FullConfig,
    val isBaseInput: Boolean = false,
    val isUnusedLane: Boolean = false,
    var constructRange: List<LaneStatus> = emptyList(),
    nextLane: Lane?
) {
    var nextLane: Lane? = nextLane
        set(nextLane: Lane?) {
            if (nextLane == null) error("Next lane set must not be null.")
            if (nextLane.idx <= idx) error("Next lane must have a greater index, idx: $idx nextLane: ${nextLane.idx}")
            field = nextLane
        }

    fun clone(newIdx: Int) = Lane(
        item, name, direction, priority, idx, fullConfig, isBaseInput, isUnusedLane, constructRange.toList(), null
    )

    fun isNormalBelt() = direction == LaneDirection.UP && priority == LanePriority.NORMAL && isBelt()

    fun isOutput() = direction == LaneDirection.DOWN && priority == LanePriority.NORMAL

    fun isInput() = isBaseInput

    fun isUnused() = isUnusedLane

    fun isBelt() = item.itemType == ItemType.ITEM

    fun isPipe() = item.itemType == ItemType.FLUID
}