pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // MUDANÇA MESTRE: PREFER_SETTINGS com o portal escancarado!
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { 
            url = uri("https://jitpack.io") 
            // Truque de Gênio: Ignora restrições de metadados
            metadataSources {
                mavenPom()
                artifact()
            }
        }
    }
}

rootProject.name = "Nebulosa"
include(":app")

