package minerofmillions.blueprint_planner.factory_components.diagram

import kotlin.math.max
import kotlin.math.min

class Region(val minX: Int, val maxX: Int, val minY: Int, val maxY: Int, val tags: Map<String, Any> = emptyMap()) {
    fun act(transform: Transformation<Any, Map<String, Any>>) = Region(
        minX + transform.translation.x,
        maxX + transform.translation.x,
        minY + transform.translation.y,
        maxY + transform.translation.y,
        transform.regionMapping(tags)
    )

    fun containsPoint(x: Int, y: Int) = x in minX..maxX && y in minY..maxY

    companion object {
        fun fromPoints(first: Point, second: Point) = Region(
            min(first.x, second.x),
            max(first.x, second.x),
            min(first.y, second.y),
            max(first.y, second.y)
        )
    }
}