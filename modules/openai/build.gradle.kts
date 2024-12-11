plugins {
    id("java")
    kotlin("jvm")
}

group = "io.github.yricky.oh"
version = "0.1.0-main-62d9c41"

repositories {
    maven("https://maven.aliyun.com/repository/public/")
//    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.aallam.openai:openai-client:3.8.2")
    implementation("org.devlive.sdk:openai-java-sdk:2024.01.3")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}