package dto
import kotlinx.serialization.Serializable

@Serializable
data class ProtectionPackagesDto(
    val protectionPackages: List<ProtectionPackageDto>
)

@Serializable
data class ProtectionPackageDto(
    val id: String,
    val name: String,
    val deductibleAmount: DeductibleAmount,
    val ratingStars: Int,
    val isPreviouslySelected: Boolean,
    val isSelected: Boolean,
    val isDeductibleAvailable: Boolean,
    val includes: List<ProtectionIncluded> = emptyList(),
    val price: PriceInfo,
    val isNudge: Boolean,
    // Only present for the fourth option:
    val description: String? = null
)

@Serializable
data class DeductibleAmount(
    val currency: String,
    val value: Int
)

@Serializable
data class ProtectionIncluded(
    val id: String,
    val title: String,
    val description: String,
    val tags: List<String> = emptyList()
)