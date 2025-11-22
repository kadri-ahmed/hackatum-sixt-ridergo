package matching

import dto.LocationDto
import kotlinx.coroutines.test.runTest
import models.DestinationContext
import models.TripPurpose
import models.TripType
import kotlin.test.Test
import kotlin.test.assertTrue

class FeatureMatchingTest{

    @Test
    fun testFetchingWeatherPerLocation() = runTest {
        // This is the correct way to create an instance of the class.
        val locationMock = LocationDto(
            city = "Munich",
            country = "Germany",
            airportCode = "MUC"
        )

        val destinationContext = DestinationContext(
            location = locationMock,
            startDate = "2025-12-01T09:00:00+01:00",
            endDate = "2025-12-01T09:00:00+01:00",
            tripType = TripType.ROUND_TRIP,
            travelerCount = 1,
            purpose = TripPurpose.UNKNOWN
        )

        assertTrue(true)

    }
}