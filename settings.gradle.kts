pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/public/")
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform").version(extra["kotlin.version"] as String) apply false
        kotlin("plugin.serialization") version(extra["kotlin.version"] as String) apply false
        id("org.jetbrains.kotlin.plugin.compose").version(extra["kotlin.version"] as String) apply false
        id("org.jetbrains.compose").version(extra["compose.version"] as String) apply false
        kotlin("jvm") version "2.0.21"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "kra"

include(":modules:common")
include(":modules:abcde")
include(":modules:resde")
include(":modules:hapde")

//include(":examples:findStr")

include("abcdecoder")
include("modules:openai")
findProject(":modules:openai")?.name = "openai"
