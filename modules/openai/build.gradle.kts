plugins {
    id("java")
}

group = "io.github.yricky.oh"
version = "0.1.0-main-62d9c41"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.devlive.sdk:openai-java-sdk:2024.01.3")
}

tasks.test {
    useJUnitPlatform()
}