package models

import kotlinx.serialization.Serializable

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
