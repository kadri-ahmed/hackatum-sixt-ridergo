package di

import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { Darwin.create() }
    single<com.russhwolf.settings.Settings> { com.russhwolf.settings.NSUserDefaultsSettings(platform.Foundation.NSUserDefaults.standardUserDefaults) }
    single<repositories.MapboxAutocompleter> { repositories.IosMapboxAutocompleter() }
}
