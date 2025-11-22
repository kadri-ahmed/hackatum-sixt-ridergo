package org.example.ridergo

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.engine.darwin.Darwin
import network.api.SixtApiImpl
import network.createHttpClient

fun MainViewController() = ComposeUIViewController {
    App(client = remember {
        SixtApiImpl(createHttpClient(Darwin.create()))
    })
}