package minerofmillions.blueprint_planner.factory_components.diagram

class ActionPair<T>(val data: T, val action: Endo<T>) {
    fun act(nextAction: Endo<T>): ActionPair<T> = ActionPair(data, nextAction(action))
    operator fun invoke(nextAction: Endo<T>) = ActionPair(data, nextAction(action))

    fun run() = action(data)
}