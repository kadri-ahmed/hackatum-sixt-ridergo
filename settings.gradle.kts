rootProject.name = "ridergo"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = java.util.Properties().apply {
                    val localFile = settingsDir.resolve("local.properties")
                    if (localFile.exists()) load(localFile.inputStream())
                }.getProperty("MAPBOX_DOWNLOADS_TOKEN")
                    ?: providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").orNull
            }
        }
    }
}

include(":composeApp")