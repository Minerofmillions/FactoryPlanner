package minerofmillions.blueprint_planner.factory_components.lane

enum class LaneStatus {
    BEGIN,
    END,
    CONSTRUCT,
    EMPTY,
    INPUT,
    OUTPUT,
    BYPRODUCT,
    BOTH,
    OUTPUT_BEGIN,
    INPUT_END;

    companion object {
        fun shouldConstruct(lane: LaneStatus) = lane != EMPTY
    }
}