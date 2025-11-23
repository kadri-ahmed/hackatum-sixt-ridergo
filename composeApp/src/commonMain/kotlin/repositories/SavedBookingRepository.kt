package repositories

import dto.SavedBooking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface SavedBookingRepository {
    suspend fun saveBooking(booking: SavedBooking)
    fun getSavedBookings(): Flow<List<SavedBooking>>
    suspend fun deleteBooking(id: String)
}

class SavedBookingRepositoryImpl(
    private val settings: Settings
) : SavedBookingRepository {
    private val _savedBookings = MutableStateFlow<List<SavedBooking>>(emptyList())
    private val BOOKINGS_KEY = "saved_bookings"
    
    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadBookings()
    }

    private fun loadBookings() {
        val bookingsString = settings.getString(BOOKINGS_KEY, "")
        if (bookingsString.isNotBlank()) {
            try {
                val bookings = json.decodeFromString<List<SavedBooking>>(bookingsString)
                _savedBookings.value = bookings
            } catch (e: Exception) {
                println("Failed to load bookings: $e")
                _savedBookings.value = emptyList()
            }
        }
    }

    private fun persistBookings() {
        try {
            val bookingsString = json.encodeToString(_savedBookings.value)
            settings.putString(BOOKINGS_KEY, bookingsString)
        } catch (e: Exception) {
            println("Failed to save bookings: $e")
        }
    }

    override suspend fun saveBooking(booking: SavedBooking) {
        val current = _savedBookings.value.toMutableList()
        // Check if booking with same ID exists and replace it, or add new
        val index = current.indexOfFirst { it.id == booking.id }
        if (index != -1) {
            current[index] = booking
        } else {
            current.add(booking)
        }
        _savedBookings.value = current
        persistBookings()
    }

    override fun getSavedBookings(): Flow<List<SavedBooking>> {
        return _savedBookings.asStateFlow().map { list ->
            list.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun deleteBooking(id: String) {
        val current = _savedBookings.value.toMutableList()
        current.removeAll { it.id == id }
        _savedBookings.value = current
        persistBookings()
    }
}
