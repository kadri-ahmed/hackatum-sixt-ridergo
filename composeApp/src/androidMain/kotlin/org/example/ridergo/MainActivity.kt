package org.example.ridergo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import io.ktor.client.engine.okhttp.OkHttp
import network.api.SixtApiImpl
import network.createHttpClient
import repositories.BookingRepositoryImpl
import repositories.VehiclesRepositoryImpl

class MainActivity : ComponentActivity() {
    @androidx.compose.foundation.ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val api = remember {
                SixtApiImpl(createHttpClient(OkHttp.create()))
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
    }
}