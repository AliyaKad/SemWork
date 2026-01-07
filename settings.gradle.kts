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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SemWork"
include(":app")
include(":core")
include(":feature")
include(":feature:feature-random")
include(":feature:feature-search")
include(":feature:feature-favorites")
include(":feature:feature-creator")
include(":feature:feature-auth")
include(":core:core-models")
include(":core:core-data")
include(":core:core-ui")
include(":feature:feature-random:feature-random-api")
include(":feature:feature-random:feature-random-impl")
include(":feature:feature-search:feature-search-api")
include(":feature:feature-search:feature-search-impl")
include(":core:core-network")
include(":feature:feature-favorites:feature-favorites-api")
include(":feature:feature-favorites:feature-favorites-impl")
include(":feature:feature-creator:feature-creator-api")
include(":feature:feature-creator:feature-creator-impl")
include(":feature:feature-auth:feature-auth-api")
include(":feature:feature-auth:feature-auth-impl")
include(":core:core-analytics")
