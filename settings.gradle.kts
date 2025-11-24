pluginManagement {
    // Workaround for unmockplugin not being on gradle plugin portal
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "de.mobilej.unmock") {
                useModule("com.github.bjoernq:unmockplugin:${requested.version}")
            }
        }
    }

    repositories {

        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "criticalmaps-android"
include(":app")
