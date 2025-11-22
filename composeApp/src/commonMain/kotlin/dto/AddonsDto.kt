package dto

import kotlinx.serialization.Serializable

@Serializable
data class AddonsDto(
    val addons: List<AddonCategory> = emptyList()
)

@Serializable
data class AddonCategory(
    val id: Int,
    val name: String,
    val options: List<AddonOption>
)

@Serializable
data class AddonOption(
    val chargeDetail: ChargeDetail,
    val additionalInfo: AdditionalInfo
)

@Serializable
data class ChargeDetail(
    val id: String,
    val title: String,
    val description: String,
    val iconUrl: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class AdditionalInfo(
    val price: PriceInfo,
    val isPreviouslySelected: Boolean,
    val isSelected: Boolean,
    val isEnabled: Boolean,
    val selectionStrategy: SelectionStrategy? = null,
    val isNudge: Boolean
)

@Serializable
data class SelectionStrategy(
    val isMultiSelectionAllowed: Boolean,
    val maxSelectionLimit: Int,
    val currentSelection: Int
)
