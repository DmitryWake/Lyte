rootProject.name = "Lyte"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
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
    }
}

include(":androidApp")
include(":shared")
include(":core:core-app")
include(":core:core-mvi")
include(":core:core-navigation")
include(":core:core-di")
include(":core:core-design")
include(":core:core-db")
include(":core:core-workout")
include(":core:core-session")
include(":core:core-screenshot")
include(":feature:tracker:api")
include(":feature:tracker:impl")
include(":feature:workout:api")
include(":feature:workout:impl")
include(":feature:history:api")
include(":feature:history:impl")
include(":feature:onboarding:api")
include(":feature:onboarding:impl")
include(":feature:splash:api")
include(":feature:splash:impl")