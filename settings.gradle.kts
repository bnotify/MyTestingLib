pluginManagement {
    repositories {
        maven { url = uri("$rootDir/build/repo") }
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
        maven { url = uri("$rootDir/build/repo") }
        google()
        mavenCentral()
    }
}

rootProject.name = "MyTestingLib"
include(":app")
include(":mycustomlib")
include(":mycustomplugin")
