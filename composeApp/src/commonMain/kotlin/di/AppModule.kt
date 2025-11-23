package di

import network.api.GroqApi
import network.api.GroqApiImpl
import network.api.SixtApi
import network.api.SixtApiImpl
import network.createHttpClient
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import repositories.BookingRepository
import repositories.BookingRepositoryImpl
import repositories.ChatRepository
import repositories.ChatRepositoryImpl
import repositories.VehiclesRepository
import repositories.VehiclesRepositoryImpl
import com.russhwolf.settings.Settings

expect val platformModule: Module

val appModule = module {
    single { createHttpClient(get()) }
    single<SixtApi> { SixtApiImpl(get()) }
    single<GroqApi> { GroqApiImpl(get()) }
    single<BookingRepository> { BookingRepositoryImpl(get()) }
    single<VehiclesRepository> { VehiclesRepositoryImpl(get()) }
    single<ChatRepository> { ChatRepositoryImpl(get()) }

    single<repositories.SavedBookingRepository> { repositories.SavedBookingRepositoryImpl(get()) }
    single<repositories.UserRepository> { repositories.UserRepositoryImpl(get()) }
    
    viewModel { viewmodels.SearchViewModel(get(), get(), get()) }
    viewModel { viewmodels.VehicleListViewModel(get(), get()) }
    viewModel { viewmodels.ProtectionViewModel(get(), get()) }
    viewModel { viewmodels.BookingSummaryViewModel(get(), get(), get(), get()) }
    viewModel { viewmodels.ChatViewModel(get(), get(), get(), get(), get(), get()) }
    
    // Shared state for booking flow
    single { viewmodels.BookingFlowViewModel(get()) }

    // Persistence
    single<utils.Storage> { utils.StorageImpl(get<Settings>()) }
}
