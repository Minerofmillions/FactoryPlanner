package minerofmillions.blueprint_planner.factory_components.coord_table

import minerofmillions.blueprint_planner.factory_components.diagram.DiagramBoundingBox

class CoordTable<T> {
    private val table = mutableMapOf<Int, MutableMap<Int, T>>()

    operator fun get(x: Int, y: Int): T? = table[x]?.get(y)

    operator fun set(x: Int, y: Int, value: T) {
        table.getOrPut(x, ::mutableMapOf)[y] = value
    }

    fun iterate(lambda: (x: Int, y: Int, value: T) -> Unit) {
        for ((x, xTable) in table) {
            for ((y, value) in xTable) {
                lambda(x, y, value)
            }
        }
    }

    fun calculateBoundingBox(): DiagramBoundingBox = DiagramBoundingBox(
        table.keys.minOrNull() ?: 0,
        table.keys.maxOrNull() ?: 1,
        table.values.mapNotNull { it.keys.maxOrNull() }.maxOrNull() ?: 1,
        table.values.mapNotNull { it.keys.minOrNull() }.minOrNull() ?: 0
    )

    fun calculateEnvelope() = calculateBoundingBox().toEnvelope()
}