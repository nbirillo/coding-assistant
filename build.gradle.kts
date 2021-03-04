import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.30"
    id("org.jetbrains.intellij") version "0.7.2"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jetbrains.dokka") version "0.10.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

group = "io.github.nbirillo.coding.assistant"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.research.ml.ast.transformations:ast-transformations") {
        version {
            branch = "master"
        }
    }
    implementation("org.jgrapht:jgrapht-core:1.1.0")
    implementation("org.jgrapht:jgrapht-ext:1.1.0")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.15.0") {
        exclude("org.slf4j")
    }
    implementation("com.github.gumtreediff", "core", "2.1.2")

    implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

intellij {
    type = "PC"
    version = "2020.3.3"
    downloadSources = false
    setPlugins("PythonCore")
    updateSinceUntilBuild = true
}

ktlint {
    enableExperimentalRules.set(true)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    runIde {
        val input: String? by project
        val output: String? by project
        args = listOfNotNull(
            "solution-space",
            input?.let { "--input_path=$it" },
            output?.let { "--output_path=$it" }
        )
        jvmArgs = listOf("-Djava.awt.headless=true", "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
        standardInput = System.`in`
        standardOutput = System.`out`
    }

    register("solution-space-cli") {
        dependsOn("runIde")
    }
}

tasks.withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
    .forEach { it.enabled = false }
