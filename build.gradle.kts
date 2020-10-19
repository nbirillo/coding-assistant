import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.4.22"
    java
    kotlin("jvm") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jetbrains.dokka") version "0.10.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

group = "io.github.nbirillo.coding.assistant"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.github.gumtreediff", "client", "2.1.2")
    implementation("com.github.gumtreediff", "client.diff", "2.1.2")
    implementation("com.github.gumtreediff", "core", "2.1.2")
}

intellij {
    type = "PC"
    version = "2020.2.3"
    downloadSources = false
    setPlugins("PythonCore:202.7660.27")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

ktlint {
    enableExperimentalRules.set(true)
}
