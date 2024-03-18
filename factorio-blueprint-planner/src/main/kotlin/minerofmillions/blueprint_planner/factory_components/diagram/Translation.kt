package minerofmillions.blueprint_planner.factory_components.diagram

class Translation(val x: Int, val y: Int) {
    fun compose(other: Translation) = Translation(x + other.x, y + other.y)

    companion object {
        val ID = Translation(0, 0)
    }
}