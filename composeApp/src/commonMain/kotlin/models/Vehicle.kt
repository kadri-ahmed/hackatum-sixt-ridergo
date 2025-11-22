package models

import kotlinx.serialization.Serializable

@Serializable
data class VehicleAttribute(
    val key: String,
    val title: String,
    val value: String,
    val attributeType: String,
    val iconUrl: String? = null,
)

@Serializable
data class VehicleCost(
    val currency: String,
    val value: Int
)

@Serializable
data class Vehicle(
    val id: String,
    val brand: String,
    val model: String,
    val acrissCode: String,
    val images: List<String>,
    val bagsCount: Int,
    val passengersCount: Int,
    val groupType: String,
    val tyreType: String,
    val transmissionType: String,
    val fuelType: String,
    val isNewCar: Boolean,
    val isRecommended: Boolean,
    val isMoreLuxury: Boolean,
    val isExcitingDiscount: Boolean,
    val attributes: List<VehicleAttribute>,
    val vehicleStatus: String,
    val vehicleCost: VehicleCost,
    val upsellReasons: List<String>
)

