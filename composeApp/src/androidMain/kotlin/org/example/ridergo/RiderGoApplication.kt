package org.example.ridergo

import android.app.Application
import di.appModule
import di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RiderGoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@RiderGoApplication)
            androidLogger()
            modules(appModule, platformModule)
        }
    }
}
