package minerofmillions.blueprint_planner.factory_components.diagram

import kotlin.math.max
import kotlin.math.min

class DiagramBoundingBox(val left: Int, val right: Int, val top: Int, val bottom: Int) {
    fun toEnvelope() = Envelope(-left, right - 1, top - 1, -bottom)

    fun translate(x: Int, y: Int) = DiagramBoundingBox(left + x, right + x, top + y, bottom + y)

    fun includeOrigin() = compose(ORIGIN)

    fun compose(other: DiagramBoundingBox) = DiagramBoundingBox(
        min(left, other.left),
        max(right, other.right),
        max(top, other.top),
        min(bottom, other.bottom)
    )

    companion object {
        val ORIGIN = DiagramBoundingBox(0, 0, 0, 0)
    }
}