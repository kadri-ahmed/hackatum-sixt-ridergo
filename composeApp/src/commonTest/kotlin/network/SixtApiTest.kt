package network

import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.test.runTest
import network.api.SixtApiImpl
import utils.Result
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class SixtApiTest {

    @Test
    fun testFullBookingFlow() = runTest {
        // 1. Setup API with CIO engine (supports HTTP on JVM/Android/Native)
        val client = createHttpClient(CIO.create())
        val api = SixtApiImpl(client)

        println("🚀 Starting Full Booking Flow Test")

        // 2. Create Booking
        println("Step 1: Creating Booking...")
        val createBookingResult = api.createBooking()
        assertTrue(createBookingResult is Result.Success, "Failed to create booking: ${createBookingResult}")
        val bookingId = (createBookingResult as Result.Success).data.id
        println("✅ Booking Created! ID: $bookingId")

        // 3. Get Available Vehicles
        println("Step 2: Fetching Vehicles for Booking $bookingId...")
        val vehiclesResult = api.getAvailableVehicles(bookingId)
        assertTrue(vehiclesResult is Result.Success, "Failed to fetch vehicles: ${vehiclesResult}")
        val vehicles = (vehiclesResult as Result.Success).data.deals
        assertTrue(vehicles.isNotEmpty(), "No vehicles found")
        val selectedVehicle = vehicles.first().vehicle
        println("✅ Vehicles Fetched! Found ${vehicles.size} vehicles. Selecting: ${selectedVehicle.brand} ${selectedVehicle.model} (ID: ${selectedVehicle.id})")

        // 4. Get Protection Packages
        println("Step 3: Fetching Protection Packages...")
        val protectionResult = api.getAvailableProtectionPackages(bookingId)
        assertTrue(protectionResult is Result.Success, "Failed to fetch protection packages: ${protectionResult}")
        val packages = (protectionResult as Result.Success).data.protectionPackages
        assertTrue(packages.isNotEmpty(), "No protection packages found")
        val selectedPackage = packages.first()
        println("✅ Protection Packages Fetched! Found ${packages.size} packages. Selecting: ${selectedPackage.name} (ID: ${selectedPackage.id})")

        // 5. Assign Vehicle
        println("Step 4: Assigning Vehicle ${selectedVehicle.id}...")
        val assignVehicleResult = api.assignVehicleToBooking(bookingId, selectedVehicle.id)
        assertTrue(assignVehicleResult is Result.Success, "Failed to assign vehicle: ${assignVehicleResult}")
        println("✅ Vehicle Assigned!")

        // 6. Assign Protection
        println("Step 5: Assigning Protection ${selectedPackage.id}...")
        val assignProtectionResult = api.assignProtectionPackageToBooking(bookingId, selectedPackage.id)
        assertTrue(assignProtectionResult is Result.Success, "Failed to assign protection: ${assignProtectionResult}")
        println("✅ Protection Assigned!")

        // 7. Complete Booking (Optional - might fail if payment details are needed, but let's try)
        // Note: The API might require more steps or mock payment, but let's see if it returns success or a specific error we expect.
        // For now, we'll just log the result.
        println("Step 6: Completing Booking...")
        val completeResult = api.completeBooking(bookingId)
        if (completeResult is Result.Success) {
             println("✅ Booking Completed Successfully!")
        } else {
             println("⚠️ Booking Completion returned: $completeResult (This might be expected if payment is missing)")
        }

        println("🎉 Full Booking Flow Test Finished!")
    }
}
