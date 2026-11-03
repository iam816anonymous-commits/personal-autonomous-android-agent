pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "personal-autonomous-android-agent"
include(":agent:core", ":agent:app")
project(":agent:core").projectDir = file("agent/core")
project(":agent:app").projectDir = file("agent/app")
