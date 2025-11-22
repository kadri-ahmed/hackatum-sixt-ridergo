package org.example.ridergo

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.engine.darwin.Darwin
import network.api.SixtApiImpl
import network.createHttpClient
import repositories.BookingRepositoryImpl
import repositories.VehiclesRepositoryImpl

fun MainViewController() = ComposeUIViewController {
    val api = remember {
        SixtApiImpl(createHttpClient(Darwin.create()))
    }
    val bookingRepository = remember {
        BookingRepositoryImpl(api)
    }
    val vehiclesRepository = remember {
        VehiclesRepositoryImpl(api)
    }
    App(
        bookingRepository = bookingRepository,
        vehiclesRepository = vehiclesRepository
    )
}