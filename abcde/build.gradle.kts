
plugins {
    kotlin("jvm")
}

group = "com.abcslim"
version = "0.0.1"

repositories {

    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("com.aallam.openai:openai-client:3.8.2")
    implementation("org.devlive.sdk:openai-java-sdk:2024.01.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
//    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}