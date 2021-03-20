import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.1.0")

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

/**
 * Gradle task to build and serialize solution space into `output` directory.
 * input is a directory with a name of the task containing dataset .csv files.
 */
open class SolutionSpaceCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Input directory with csv files
    @get:Input
    val input: String? by project
    // Output directory
    @get:Input
    val output: String? by project

    init {
        jvmArgs = listOf("-Djava.awt.headless=true", "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

/**
 * Generates hint report using implemented algorithm.
 */
open class HintGenerationCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Path to the serialized solution space file.
    @get:Input
    val solutionSpacePath: String? by project
    // Path to support information about dataset original code fragments.
    // Needed for only report.
    @get:Input
    val codeRepositoryPath: String? by project
    // Name of the task.
    @get:Input
    val taskName: String? by project
    // Directory to store the report.
    @get:Input
    val outputDir: String? by project

    init {
        jvmArgs = listOf("-Djava.awt.headless=true", "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
        .forEach { it.enabled = false }

    register<SolutionSpaceCliTask>("solutionSpaceCli") {
        dependsOn("build")
        args = listOfNotNull(
            "solution-space",
            input?.let { "--input_path=$it" },
            output?.let { "--output_path=$it" }
        )
    }

    register<HintGenerationCliTask>("hintGenerationCli") {
        dependsOn("build")
        args = listOfNotNull(
            "hint-generation",
            solutionSpacePath?.let { "--space_path=$it" },
            codeRepositoryPath?.let { "--code_repository_path=$it" },
            outputDir?.let { "--output_path=$it" },
            taskName?.let { "--task_name=$it" }
        )
    }
}
