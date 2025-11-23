package repositories

import models.AddressResult
import models.MapboxError
import utils.Result

interface MapboxAutocompleter {
    suspend fun search(query: String): Result<List<AddressResult>, MapboxError>
}
