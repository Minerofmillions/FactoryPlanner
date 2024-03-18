package minerofmillions.blueprint_planner.factory_components.bundle

import minerofmillions.blueprint_planner.factory_components.lane.LanePriority

data class BundlePort(
    val bundleIdx: Int,
    val strandIdx: Int,
    val itemName: String,
    val protType: PortType,
    val portIdx: Int?,
    val priority: LanePriority,
    val flowRate: Double = 0.0
) {
    fun toBypass(bypassIdx: Int) = BundleBypass(bypassIdx, this)
}