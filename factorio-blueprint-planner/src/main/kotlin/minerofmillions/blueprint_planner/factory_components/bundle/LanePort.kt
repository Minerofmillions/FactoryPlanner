package minerofmillions.blueprint_planner.factory_components.bundle

import minerofmillions.blueprint_planner.factory_components.lane.LanePriority

data class LanePort(
    val laneIdx: Int,
    val itemName: String,
    val portType: PortType,
    val portIdx: Int?,
    val priority: LanePriority,
    val flowRate: Double = 0.0
) {
    fun toBundlePort(bundleIdx: Int, strandIdx: Int) =
        BundlePort(bundleIdx, strandIdx, itemName, portType, portIdx, priority, flowRate)

    fun updateIdx(newIdx: Int) = LanePort(newIdx, itemName, portType, portIdx, priority, flowRate)
}