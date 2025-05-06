pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("androidx.navigation.safeargs.kotlin") version "2.7.7"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Sip&Score"
include(":app")


//pluginManagement {
//    repositories {
//        google {
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
//        mavenCentral()
//        gradlePluginPortal()
//    }
//}
//
//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        maven { url = uri("https://maven.google.com") }
//        mavenCentral()
//    }
//}
//
//rootProject.name = "Sip&Score"
//include(":app")
