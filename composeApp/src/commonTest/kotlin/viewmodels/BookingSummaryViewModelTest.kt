package viewmodels

import dto.BookingDto
import dto.Deal
import dto.Price
import dto.Pricing
import dto.Vehicle
import dto.VehicleCost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import repositories.BookingRepository
import ui.state.BookingSummaryUiState
import utils.NetworkError
import utils.Result
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class BookingSummaryViewModelTest {

    private lateinit var viewModel: BookingSummaryViewModel
    private lateinit var bookingRepository: MockBookingRepository
    private lateinit var bookingFlowViewModel: BookingFlowViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bookingRepository = MockBookingRepository()
        bookingFlowViewModel = BookingFlowViewModel(bookingRepository)
        viewModel = BookingSummaryViewModel(bookingRepository, bookingFlowViewModel)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBooking success updates state to Success`() = runTest(testDispatcher) {
        // Given
        val bookingId = "test-booking-id"
        bookingFlowViewModel.setBookingId(bookingId)
        val mockBooking = createMockBooking(bookingId)
        bookingRepository.mockGetBookingResult = Result.Success(mockBooking)

        // When
        viewModel.loadBooking()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is BookingSummaryUiState.Success)
        assertEquals(mockBooking, (state as BookingSummaryUiState.Success).booking)
    }

    @Test
    fun `loadBooking failure updates state to Error`() = runTest(testDispatcher) {
        // Given
        val bookingId = "test-booking-id"
        bookingFlowViewModel.setBookingId(bookingId)
        bookingRepository.mockGetBookingResult = Result.Error(NetworkError.SERVER_ERROR)

        // When
        viewModel.loadBooking()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is BookingSummaryUiState.Error)
        assertEquals("Server error", (state as BookingSummaryUiState.Error).message)
    }
    
    @Test
    fun `confirmBooking success updates state and calls onConfirmed`() = runTest(testDispatcher) {
        // Given
        val bookingId = "test-booking-id"
        bookingFlowViewModel.setBookingId(bookingId)
        val mockBooking = createMockBooking(bookingId)
        bookingRepository.mockCompleteBookingResult = Result.Success(mockBooking)
        var onConfirmedCalled = false

        // When
        viewModel.confirmBooking { onConfirmedCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is BookingSummaryUiState.Success)
        assertTrue(onConfirmedCalled)
    }

    private fun createMockBooking(id: String): BookingDto {
        return BookingDto(
            id = id,
            bookedCategory = "Sedan",
            status = "CONFIRMED",
            createdAt = "2023-10-27T10:00:00Z",
            selectedVehicle = Deal(
                vehicle = Vehicle(
                    id = "v1", brand = "BMW", model = "X5", acrissCode = "XVAR",
                    images = emptyList(), bagsCount = 2, passengersCount = 5,
                    groupType = "SUV", tyreType = "Summer", transmissionType = "Automatic",
                    fuelType = "Petrol", isNewCar = true, isRecommended = true,
                    isMoreLuxury = false, isExcitingDiscount = false, attributes = emptyList(),
                    vehicleStatus = "AVAILABLE", vehicleCost = VehicleCost("EUR", 100),
                    upsellReasons = emptyList()
                ),
                pricing = Pricing(0, Price("EUR", 100.0, "€"), Price("EUR", 100.0, "€")),
                dealInfo = "Great Deal"
            )
        )
    }
}

// Mocks
class MockBookingRepository : BookingRepository {
    var mockGetBookingResult: Result<BookingDto, NetworkError>? = null
    var mockCompleteBookingResult: Result<BookingDto, NetworkError>? = null

    override suspend fun createBooking(): Result<dto.CreateBookingDto, NetworkError> {
        TODO("Not yet implemented")
    }

    override suspend fun getBooking(bookingId: String): Result<BookingDto, NetworkError> {
        return mockGetBookingResult ?: Result.Error(NetworkError.UNKNOWN)
    }

    override suspend fun assignVehicleToBooking(bookingId: String, vehicleId: String): Result<BookingDto, NetworkError> {
        TODO("Not yet implemented")
    }

    override suspend fun assignProtectionPackageToBooking(bookingId: String, packageId: String): Result<BookingDto, NetworkError> {
        TODO("Not yet implemented")
    }

    override suspend fun completeBooking(bookingId: String): Result<BookingDto, NetworkError> {
        return mockCompleteBookingResult ?: Result.Error(NetworkError.UNKNOWN)
    }
}


