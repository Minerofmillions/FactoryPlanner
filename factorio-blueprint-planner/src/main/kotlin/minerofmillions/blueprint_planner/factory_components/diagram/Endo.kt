package minerofmillions.blueprint_planner.factory_components.diagram

import minerofmillions.utils.identity

class Endo<T>(endo: ((T) -> T)?) {
    private val endo = endo ?: ::identity

    fun act(other: Endo<T>) = Endo<T> { endo(other.endo(it)) }
    operator fun invoke(other: Endo<T>) = Endo<T> { endo(other.endo(it)) }

    fun eval(value: T) = endo(value)
    operator fun invoke(value: T) = endo(value)

    companion object {
        fun <T> id(): Endo<T> = Endo(null)
    }
}