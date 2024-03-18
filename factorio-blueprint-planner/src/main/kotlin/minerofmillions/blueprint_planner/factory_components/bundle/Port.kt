package minerofmillions.blueprint_planner.factory_components.bundle

import minerofmillions.blueprint_planner.factory_components.lane.LanePriority

data class Port(
    val num: Int,
    val coord: Int,
    val portType: PortType,
    val items: List<String>,
    val priority: LanePriority,
    val name: String?
)
