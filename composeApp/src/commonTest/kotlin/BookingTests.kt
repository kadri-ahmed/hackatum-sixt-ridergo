import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.test.runTest
import network.api.SixtApiImpl
import network.createHttpClient
import utils.Result
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class BookingTests {

    @Test
    fun testBookingFlow() = runTest {
        // 1. Setup
        val client = createHttpClient(CIO.create())
        val api = SixtApiImpl(client)

        println("🚀 Starting Booking Flow Test")

        // 2. Create a fresh booking to ensure we have a valid session/ID
        val createResult = api.createBooking()
        assertTrue(createResult is Result.Success, "Failed to create booking")
        val bookingId = createResult.data.id
        println("✅ Created Booking: $bookingId")

        // 3. Fetch Vehicles
        val vehiclesResult = api.getAvailableVehicles(bookingId)
        assertTrue(vehiclesResult is Result.Success, "Failed to fetch vehicles")
        val vehicles = vehiclesResult.data.deals
        assertTrue(vehicles.isNotEmpty(), "No vehicles available")
        
        val selectedDeal = vehicles.first()
        val vehicleId = selectedDeal.vehicle.id
        println("✅ Selected Vehicle: ${selectedDeal.vehicle.brand} ${selectedDeal.vehicle.model} ($vehicleId)")

        // 4. Assign Vehicle (This is the part user said is "not working")
        val assignResult = api.assignVehicleToBooking(bookingId, vehicleId)
        assertTrue(assignResult is Result.Success, "Failed to assign vehicle")
        
        // Verify the assignment in the response
        val updatedBooking = assignResult.data
        assertNotNull(updatedBooking.selectedVehicle, "Vehicle was not assigned to booking")
        assertTrue(updatedBooking.selectedVehicle?.vehicle?.id == vehicleId, "Assigned vehicle ID mismatch")
        println("✅ Vehicle Assigned Successfully")

        // 5. Fetch Protections (Simulating the next screen)
        val protectionsResult = api.getAvailableProtectionPackages(bookingId)
        assertTrue(protectionsResult is Result.Success, "Failed to fetch protections")
        val protections = protectionsResult.data.protectionPackages
        
        if (protections.isNotEmpty()) {
            val selectedProtection = protections.first()
            val assignProtectionResult = api.assignProtectionPackageToBooking(bookingId, selectedProtection.id)
            assertTrue(assignProtectionResult is Result.Success, "Failed to assign protection")
            println("✅ Protection Assigned: ${selectedProtection.name}")
        }

        // 6. Complete Booking
        val completeResult = api.completeBooking(bookingId)
        assertTrue(completeResult is Result.Success, "Failed to complete booking")
        println("✅ Booking Completed Successfully")
    }
}
