package dto

import kotlinx.serialization.Serializable

@Serializable
data class Deal(
    val vehicle: Vehicle,
    val pricing: Pricing,
    val dealInfo: String,
    val tags: List<String> = emptyList(),
    val priceTag: String? = null
)

@Serializable
data class Filter(
    val brands: List<String> = emptyList(),
    val transmissionTypes: List<String> = emptyList(),
    val fuelTypes: List<String> = emptyList()
)

@Serializable
data class QuickFilter(
    val key: String,
    val title: String,
    val selectType: String
)

@Serializable
data class Pricing(
    val discountPercentage: Int,
    val displayPrice: Price,
    val totalPrice: Price
)

@Serializable
data class Price(
    val currency: String,
    val amount: Double,
    val prefix: String? = null,
    val suffix: String? = null
)

@Serializable
data class ReservationBlockDateTime(
    val date: String,
    val timeZone: String
)

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
data class UpsellReason(
    val title: String,
    val description: String? = null,
    val iconUrl: String? = null
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
    val upsellReasons: List<UpsellReason>
)

@Serializable
data class AvailableVehiclesDto(
    val reservationId: String,
    val deals: List<Deal>,
    val totalVehicles: Int,
    val reservationBlockDateTime: ReservationBlockDateTime? = null,
    val filter: Filter? = null,
    val quickFilters: List<QuickFilter>? = null,
    val terminalList: List<String>? = null,
    val isBundleSelected: Boolean? = null
)