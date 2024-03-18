package minerofmillions.recipe_factory.examples.factorio

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    Type(UnlockRecipeEffect::class, name = "unlock-recipe"),
    Type(GhostTimeToLiveEffect::class, name = "ghost-time-to-live"),
    Type(AmmoDamageEffect::class, name = "ammo-damage"),
    Type(GunSpeedEffect::class, name = "gun-speed"),
    Type(TrainBrakingForceBonusEffect::class, name = "train-braking-force-bonus"),
    Type(CharacterMiningSpeedEffect::class, name = "character-mining-speed"),
    Type(CharacterLogisticRequestsEffect::class, name = "character-logistic-requests"),
    Type(CharacterLogisticTrashSlotsEffect::class, name = "character-logistic-trash-slots"),
    Type(WorkerRobotSpeedEffect::class, name = "worker-robot-speed"),
    Type(MiningDrillProductivityBonusEffect::class, name = "mining-drill-productivity-bonus"),
    Type(WorkerRobotStorageEffect::class, name = "worker-robot-storage"),
    Type(CharacterInventorySlotsBonusEffect::class, name = "character-inventory-slots-bonus"),
    Type(LaboratorySpeedEffect::class, name = "laboratory-speed"),
    Type(InserterStackSizeBonusEffect::class, name = "inserter-stack-size-bonus"),
    Type(StackInserterCapacityBonusEffect::class, name = "stack-inserter-capacity-bonus"),
    Type(TurretAttackEffect::class, name = "turret-attack"),
    Type(ArtilleryRangeEffect::class, name = "artillery-range"),
    Type(MaximumFollowingRobotsCountEffect::class, name = "maximum-following-robots-count"),
)
interface TechnologyEffect

data class UnlockRecipeEffect @JsonCreator constructor(@JsonProperty("recipe") val recipe: String) : TechnologyEffect

data class GhostTimeToLiveEffect @JsonCreator constructor(@JsonProperty("modifier") val modifier: Double) :
    TechnologyEffect

data class AmmoDamageEffect @JsonCreator constructor(
    @JsonProperty("ammo_category") val category: String,
    @JsonProperty("modifier") val modifier: Double,
) : TechnologyEffect

data class GunSpeedEffect @JsonCreator constructor(
    @JsonProperty("ammo_category") val category: String,
    @JsonProperty("modifier") val modifier: Double
) : TechnologyEffect

data class TrainBrakingForceBonusEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Double
) : TechnologyEffect

data class CharacterMiningSpeedEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Double
) : TechnologyEffect

data class CharacterLogisticRequestsEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Boolean
) : TechnologyEffect

data class CharacterLogisticTrashSlotsEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Long
) : TechnologyEffect

data class WorkerRobotSpeedEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Double
) : TechnologyEffect

data class MiningDrillProductivityBonusEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Double
) : TechnologyEffect

data class WorkerRobotStorageEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Long
) : TechnologyEffect

data class CharacterInventorySlotsBonusEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Long
) : TechnologyEffect

data class LaboratorySpeedEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Double
) : TechnologyEffect

data class TurretAttackEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Double
) : TechnologyEffect

data class ArtilleryRangeEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Double
) : TechnologyEffect

data class InserterStackSizeBonusEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Long
) : TechnologyEffect

data class StackInserterCapacityBonusEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Long
) : TechnologyEffect

data class MaximumFollowingRobotsCountEffect @JsonCreator constructor(
    @JsonProperty("modifier") val modifier: Long
) : TechnologyEffect
