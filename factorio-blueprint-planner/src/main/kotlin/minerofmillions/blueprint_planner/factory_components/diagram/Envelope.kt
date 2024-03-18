package minerofmillions.blueprint_planner.factory_components.diagram

import kotlin.math.max
import kotlin.math.min

class Envelope(val left: Int, val right: Int, val up: Int, val down: Int) {
    fun clone() = Envelope(left, right, up, down)

    fun toRegion() = Region(-left, right, -down, up)

    fun toDiagramBoundingBox() = DiagramBoundingBox(
        min(right, -left),
        max(right, -left) + 1,
        max(up, -down) + 1,
        min(up, -down)
    )

    fun translate(x: Int, y: Int) = Envelope(left - x, right + x, up + y, down - y)

    fun act(translation: Translation) = translate(translation.x, translation.y)

    fun compose(other: Envelope) = Envelope(
        max(left, other.left),
        max(right, other.right),
        max(up, other.up),
        max(down, other.down)
    )

    companion object {
        fun fromPoint(x: Int, y: Int) = Envelope(
            max(-x, 0),
            max(x, 0),
            max(y, 0),
            max(-y, 0)
        )
    }
}