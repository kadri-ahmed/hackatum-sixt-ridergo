package models

import utils.Error

sealed interface MapboxError : Error {
    data object Network : MapboxError
    data object NoResults : MapboxError
    data class Unknown(val message: String? = null) : MapboxError
}
