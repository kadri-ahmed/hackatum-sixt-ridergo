package di

import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { OkHttp.create() }
    single<com.russhwolf.settings.Settings> {
        val context = get<android.content.Context>()
        val sharedPrefs = context.getSharedPreferences("ridergo_settings", android.content.Context.MODE_PRIVATE)
        com.russhwolf.settings.SharedPreferencesSettings(sharedPrefs)
    }
}
