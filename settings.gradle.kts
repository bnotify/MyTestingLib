pluginManagement {
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
        maven { url = uri("$rootDir/build/repo") }
    }
}
dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("$rootDir/build/repo") }
    }
}

rootProject.name = "MyTestingLib"
include(":app")
include(":mycustomlib")
include(":mycustomplugin")
includeBuild("mycustomplugin") {
    name = "mycustomplugin-build" // Unique name
}
