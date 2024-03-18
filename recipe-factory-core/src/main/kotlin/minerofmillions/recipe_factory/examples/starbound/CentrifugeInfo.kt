package minerofmillions.recipe_factory.examples.starbound

enum class CentrifugeInfo(
    private val rarest: Double,
    private val rare: Double,
    private val uncommon: Double,
    private val normal: Double,
    private val common: Double,
    val requiredPower: Double,
    val craftDelay: Double
) {
    GAS_CENTRIFUGE(0.012, 0.032, 0.09, 0.22, 0.32, 36.0, 0.52),
    LAB_CENTRIFUGE(0.01, 0.03, 0.08, 0.2, 0.3, 24.0, 0.4),
    INDUSTRIAL_CENTRIFUGE(0.007, 0.02, 0.07, 0.1, 0.2, 12.0, 0.2),
    IRON_CENTRIFUGE(0.008, 0.03, 0.09, 0.2, 0.3, 4.0, 0.85),
    WOODEN_CENTRIFUGE(0.005, 0.02, 0.08, 0.15, 0.25, 0.0, 2.0),
    PRECURSOR_SMELTER(0.012, 0.045, 0.105, 0.225, 0.375, 120.0, 0.04),
    WOODEN_SIFTER(0.005, 0.01, 0.04, 0.09, 0.11, 2.0, 1.0/3.0),
    ROCK_BREAKER(0.002, 0.008, 0.02, 0.05, 0.07, 6.0, 0.75),
    ROCK_CRUSHER(0.006, 0.025, 0.055, 0.12, 0.20, 30.0, 0.25),
    POWDER_SIFTER(0.008, 0.03, 0.07, 0.15, 0.25, 40.0, 7.0/60.0);

    operator fun get(rarity: String): Double = when (rarity) {
        "rarest" -> rarest
        "rare" -> rare
        "uncommon" -> uncommon
        "normal" -> normal
        "common" -> common
        else -> error("Invalid rarity: $rarity")
    }

    companion object {
        val values = values()

        fun getCentrifugesFor(type: String) = when (type) {
            "itemMapFarm" -> listOf(
                GAS_CENTRIFUGE,
                LAB_CENTRIFUGE,
                INDUSTRIAL_CENTRIFUGE,
                IRON_CENTRIFUGE,
                WOODEN_CENTRIFUGE
            )

            "itemMapBees" -> listOf(
                GAS_CENTRIFUGE,
                LAB_CENTRIFUGE,
                INDUSTRIAL_CENTRIFUGE,
                IRON_CENTRIFUGE,
                WOODEN_CENTRIFUGE
            )

            "itemMapLiquids" -> listOf(GAS_CENTRIFUGE, LAB_CENTRIFUGE, INDUSTRIAL_CENTRIFUGE, IRON_CENTRIFUGE)
            "itemMapIsotopes" -> listOf(GAS_CENTRIFUGE)
            "itemMapPowder" -> listOf(PRECURSOR_SMELTER, WOODEN_SIFTER, POWDER_SIFTER)
            "itemMapRocks" -> listOf(PRECURSOR_SMELTER, ROCK_BREAKER, ROCK_CRUSHER)
            else -> error("Invalid centrifuge map type: $type")
        }
    }
}