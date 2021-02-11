import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.20"
    id("org.jetbrains.intellij") version "0.6.3"
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
    implementation("io.github.nbirillo.ast.transformations:ast-transformations") {
        version {
            branch = "bugfix/unification"
        }
    }
    implementation("org.jgrapht:jgrapht-core:1.0.1")
    implementation("org.jgrapht:jgrapht-ext:1.0.1")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.15.0")
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
