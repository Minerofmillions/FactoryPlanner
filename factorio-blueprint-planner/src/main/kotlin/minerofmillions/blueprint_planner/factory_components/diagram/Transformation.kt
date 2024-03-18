package minerofmillions.blueprint_planner.factory_components.diagram

class Transformation<EntityMapping, RegionMapping>(val translation: Translation, val entityMapping: Endo<EntityMapping>, val regionMapping: Endo<RegionMapping>) {
    fun compose(other: Transformation<EntityMapping, RegionMapping>) = Transformation(
        translation.compose(other.translation),
        entityMapping.act(other.entityMapping),
        regionMapping.act(other.regionMapping)
    )

    companion object {
        fun <E, R> id() = Transformation(Translation.ID, Endo.id<E>(), Endo.id<R>())
        fun <E, R> translate(x: Int, y: Int) = Transformation(Translation(x, y), Endo.id<E>(), Endo.id<R>())
        fun <E, R> map(mapping: Endo<E>) = Transformation(Translation.ID, mapping, Endo.id<R>())
        fun <E, R> regionMap(mapping: Endo<R>) = Transformation(Translation.ID, Endo.id<E>(), mapping)
    }
}