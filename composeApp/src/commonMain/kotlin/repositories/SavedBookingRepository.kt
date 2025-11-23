package repositories

import dto.SavedBooking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface SavedBookingRepository {
    suspend fun saveBooking(booking: SavedBooking)
    fun getSavedBookings(): Flow<List<SavedBooking>>
    suspend fun deleteBooking(id: String)
}

class SavedBookingRepositoryImpl : SavedBookingRepository {
    private val _savedBookings = MutableStateFlow<List<SavedBooking>>(emptyList())

    override suspend fun saveBooking(booking: SavedBooking) {
        val current = _savedBookings.value.toMutableList()
        current.add(booking)
        _savedBookings.value = current
    }

    override fun getSavedBookings(): Flow<List<SavedBooking>> {
        return _savedBookings.asStateFlow()
    }

    override suspend fun deleteBooking(id: String) {
        val current = _savedBookings.value.toMutableList()
        current.removeAll { it.id == id }
        _savedBookings.value = current
    }
}
